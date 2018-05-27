package petit.duc;


/**
 * Pour effectuer les manipulations de fichier et répertoires.
 */
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;



/**
 * Pour créer un goal ("Mojo") maven
 */
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import petit.duc.os.support.OSDependentMavenGoal;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * Ce goal a pour vocation de permettre de réaliser le build d'une application web jee, dont le client est une SPA Angular 5, le
 * code source du client Angular 5 étant versionné dans le repository Git {@see BuildAngular5#uriRepoClientNG5}.
 * 
 * Il est par exemple possible de mapper ce goal maven, à la phase [generate-sources], avec une configuration de la forme:
 * 
 * <code>
 * 
 * 
 * 	<build>
 *		<!-- <finalName>${project.artifactId}-${project.version}</finalName> -->
 *		<plugins>
 *			<plugin>
 *				<groupId>aramitz</groupId>
 *				<artifactId>petit-duc</artifactId>
 *				<version>1.0.0</version>
 *				<executions>
 *					<execution>
 *						<phase>generate-sources</phase>
 *						<goals>
 *							<goal>build-ng5</goal>
 *						</goals>
 *						<configuration>
 *							<!-- L'URI du repository Git versionnant le code source de la partie cliente Angular 5 -->
 *							<uri-repo-client-ng5>https://github.com/Jean-Baptiste-Lasselle/petit-poivre-angular5</uri-repo-client-ng5>
 *							<version-client-ng5>1.0.9</version-client-ng5>
 *							<!-- Utilisateur Git utilisé par l'usine logicielle, ou le développeur -->
 *							<git-username>jlasselle</git-username>
 *							<!-- pas de mot de passe dans un fichier versionné, l'authentification doit se faire:
 *									¤ par demande interactive de mot de passe, lorsque le plugin est utilisé par un développeur déclenchant manuellement le build avec son IDE. Option securestore pour "se souvenir" du mot de passe aux authentifications suivantes
 *									¤ Pour l'usine logicielle, par intégration de la JRE exécutant le process maven, au système sous jacent :
 *											+ Java - usePAM   + free ipa server
 *											+ Java -> PKI Certificats SSL TLS + Lets Encrypt + free ipa server   
 *											+ OAUTH2 / saml Keycloak free ipa server   
 *											+ json web token / jwt server + free ipa server
 *							-->
 *							<!-- git-user-pwd>xxxx</git-user-pwd -->
 *
 *
 *						</configuration>
 *					</execution>
 *				</executions>
 *			</plugin>
 *			<!-- [...] -->
 *		</plugins>
 *		<!-- [...] -->
 * 	</build>
 * 
 * 
 * 
 * </code>
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Jean-Baptiste Lasselle
 *
 */
@Mojo( name = "install-angular-cli")
public class InstallAngularCLI extends OSDependentMavenGoal {

	/**
	 * <uri-repo-client-ng5></uri-repo-client-ng5>
	 * -------------------------------------------
	 * 
	 * 
	 */
	private String uriRepoClientNG5;
	
	/**
	 * <version-client-ng5></version-client-ng5>
	 * -----------------------------------------
	 * Ce paramètre permet de définir la version exacte (par exemple avec le HASH du commit)
	 * du code source du client Angular 5, que l'on souhaite embarquer dans le build du war.
	 * Ce paramètre est optionnel: par défaut, sa valeur est la chaîne de caractère vide, et la version
	 * du client Angular 5 embarquée, est aloirs la dernière version de la branche master, du repository
	 * {@see BuildAngular5#uriRepoClientNG5 }
	 */
	@Parameter(alias = "version-client-ng5", property = "version-client-ng5", defaultValue = "")
	private String versionClientNG5;
	
	/**
	 * <rep-temp-build-ng5></rep-temp-build-ng5>
	 * -----------------------------------------
	 * Le chemin absolu du répertoire temporaire dans lequel le build angular 5 sera fait.
	 * Le paramètre est optionnel, il a une valeur par défaut qui peut être surchargée
	 * 
	 */
	@Parameter(alias = "rep-temp-build-ng5", property = "rep-temp-build-ng5", defaultValue = "${project.build.directory}/petit-duc/temp-ng5-build")
	private String cheminRepertoireTempBuildNG5;
	
	/**
	 * Le répertoire temporaire dans lequel le build angular 5 sera fait.
	 */
	private File repertoireTempBuildNG5;
	
	
	/**
	 * <rep-mvn-jee-ng5></rep-mvn-jee-ng5>
	 * Le chemin absolu du répertoire maven dans lequel le résultat du build angular 5 sera copié, pour
	 * être inclut dans le build du war.
	 * Le paramètre est optionnel, il a une valeur par défaut qui peut être surchargée
	 * 
	 */
	@Parameter(alias = "rep-mvn-jee-ng5", property = "rep-mvn-jee-ng5", defaultValue = "${project.build.directory}/petit-duc/temp-ng5-build")
	private String cheminRepertoireMvnJeeNG5;
	/**
	 * Le répertoire maven dans lequel le résultat du build angular 5 sera copié, pour
	 * être inclut dans le build du war.
	 */
	
	private File repertoireMvnJeeNG5;

	
	
	private String GITusername;

	/**
	 * Demandé interactivement à l'utilisateur.
	 * Pour un déblocage projet urgent, j'ai laissé la possibilité de définir la valeur de ce champs par un paramètre du plugin maven:
	 *  - Afin que ce plugin puisse être utilsié dans un build exécuté par un Jenkins, 
	 *  - Avec une authentification (cf. {@see  org.eclipse.jgit.transport.CredentialsProvider}) de type Username / Password, parce que je ne sais pas ce qui est copnfiguré dans le socle d'intégration continue dans lequel ce plugin sera utilisé.
	 *  
	 *  Enfin, la valeur par défaut de ce paramètre est la chaîne de caractères vide, aussi:
	 *   - lorsque la valeur du mot de passe Git est la chîne de caractère vide, alors uen demande interactive est faite à l'utilsiateur, pour qu'il saisisse son mot de passe.
	 *   - ce plugin ne pourra être utilisé dans un build maven, au sein d'une instance Jenkins, par une authhentification git avec un mot de passe réduit à la chaîne de caractère vide. 
	 *
	 *
	 *		==>>> à vérifier : je fais en sorte que je puisse définir le mot de passe avec un -Dpetit.duc.git.pwd, alors qu'il n'y a PAS d'annotation @Parameter sur le champs 
	 *
	 */
	@Parameter(alias = "git-user-pwd", property = "git-user-pwd", defaultValue = "")
	private String GITuserpwd;
	
	
	
	
	
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		/**
		 * 0./ On initialise le Mojo
		 */
		this.initialiser();
		
		/**
		 * 1./ On fait le git clone de la bonne version 
		 */
		this.recupererCodeSourceAngular5();
		
		/**
		 * 2./ On fait le build Angular 5
		 */
		this.faireLeBuildAngular5("");
		
		/**
		 * 3./ On fait la copie du résultat du build, [{@see BuildAngular5#cheminRepertoireTempBuildNG5}/dist/] dans le répertoire {@see BuildAngular5#repertoireMvnJeeNG5}
		 */
		this.embarquerClientAngular5();
	}






	/**
	 * Réalise la copie du résultat du build, [{@see BuildAngular5#cheminRepertoireTempBuildNG5}/dist/] dans le répertoire {@see BuildAngular5#repertoireMvnJeeNG5}
	 */
	private void embarquerClientAngular5() {
		
	}


	private void installerAngularCLI() {
	    try {
	    	Process processNpmUpdate = new ProcessBuilder(COMMANDE_NPM_SPECIFIQUE_OS, "utpdate").directory(this.repertoireTempBuildNG5).start();
	    	Process processNpmInstall = new ProcessBuilder(COMMANDE_NPM_SPECIFIQUE_OS, "install -g @angular/cli ").directory(this.repertoireTempBuildNG5).start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}




	/**
	 * Réalise la commande :
	 * 
	 * ng build --prod --base-href="."
	 * 
	 * dans le répertoire {@see BuildAngular5#cheminRepertoireTempBuildNG5}
	 * 
	 * https://stackoverflow.com/questions/40503074/how-to-run-npm-command-in-java-using-process-builder
	 * 
	 * @param optsEtAgumentsNPM
	 */
	private void faireLeBuildAngular5(String valeurBaseHref) {
	    try {
//	    	Process process = new ProcessBuilder(COMMANDE_NPM_SPECIFIQUE_OS, optsEtAgumentsNPM).directory(this.repertoireTempBuildNG5).start();
	    	Process process = new ProcessBuilder("ng", "build --prod --base-href=\"" + valeurBaseHref + "\"").directory(this.repertoireTempBuildNG5).start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}





	private void recupererCodeSourceAngular5() {

		// je le détruis, et le re-créée
		try {
			if (this.repertoireTempBuildNG5.exists()) {
				FileUtils.forceDelete(this.repertoireTempBuildNG5);
			}
		} catch (IOException e2) {
			System.out.println(" PETIT-DUC + Un problème est survenu à la suppression du répertoire [" + this.cheminRepertoireTempBuildNG5 + "], avant la récupération du code source du client Angular 5.");
			e2.printStackTrace();
		}
		boolean AETECREE = this.repertoireTempBuildNG5.mkdirs();
		String msgINFOcreationDirRepo = "";
		if (AETECREE) {
			msgINFOcreationDirRepo = " PETIT-DUC + le Repertoire [" + this.cheminRepertoireTempBuildNG5 + "] a été détruis et re-créé.";
		} else {
			msgINFOcreationDirRepo = " PETIT-DUC + le Repertoire [" + this.cheminRepertoireTempBuildNG5 + "] n'a pas été re-créé.";
		}
		
		System.out.println(msgINFOcreationDirRepo );
		
		// CREATION DU REPO LOCAL GIT

		Git monrepogit = null;
		// GIT INIT // NON, UN GIT CLONE AU DEPART
		String URLduREPO = this.uriRepoClientNG5;
		try {
			CloneCommand cloneCommand = Git.cloneRepository();
			cloneCommand.setDirectory(this.repertoireTempBuildNG5);
			cloneCommand.setURI(URLduREPO);
			cloneCommand.setCredentialsProvider( new UsernamePasswordCredentialsProvider( this.GITusername, this.GITuserpwd ) );
			monrepogit = cloneCommand.call();
//			monrepogit = Git.init().setDirectory(repoDIR).call();
		} catch (IllegalStateException e) {
			System.out.println(" PETIT-DUC + ERREUR AU GIT INIT DANS  [" + this.cheminRepertoireTempBuildNG5 + "] ");
			e.printStackTrace();
		} catch (GitAPIException e) {
			System.out.println(" PETIT-DUC + ERREUR AU GIT INIT  DANS  [" + this.cheminRepertoireTempBuildNG5 + "] : ");
			System.out.println(" PETIT-DUC + [" + e.getMessage() + "] ");
			e.printStackTrace();
		}
	}

	/**
	 * Définit le nom de la propriété java qui permet de définir le mot de passe que petit-duc va utilsier
	 * pour récupérer le code source du client Angular 5.
	 * 
     * 
     * Par exemple, si PETIT_DUC_GIT_PWD_ENV_KEY="machin.truc.chouette"
	 * La propriété java [mvn clean petit-duc:build-ng5 deploy -Dmachin.truc.chouette=hibou ] peut être passée en paramètre de l'exécution maven, afin de définir 
	 * le mot de passe utilisé par le petit-duc aux yeux perçants, pour s'authentifier auprès de Git.
	 */
	public static final String PETIT_DUC_GIT_PWD_ENV_KEY = "petit.duc.git.pwd";
	/**
	 * Initialise les champs non statiques, qui ne sont pas des paramètres du goal Maven
	 */
	private void initialiser() {
		
		/**
		 * Pour les exécutions de petit-duc:
		 * La propriété java [ -Dpetit.duc.git.pwd=hibou ] peut être passée en paramètre de l'exécution maven, afin de définir
		 *  le mot de passe utilisé par le petit-duc aux yeux perçants.
		 * 
         * si je ne passe pas de [ -Dpetit.duc.git.pwd=hibou ] en paramètre, la valeur retournée par 
         * <code>System.getProperty(PETIT_DUC_GIT_PWD_ENV_KEY)</code> est-elle la chaîne de caractère vide, ou null ? 
		 */
		String CandidateGITuserpwd = System.getProperty(PETIT_DUC_GIT_PWD_ENV_KEY);
		if (this.GITuserpwd == null ) {
			if (CandidateGITuserpwd == null || CandidateGITuserpwd.equals("")) {
				
			} else {
				this.GITuserpwd = CandidateGITuserpwd;
			}
		}
		this.GITuserpwd = null;
		
		this.repertoireTempBuildNG5 = new File(this.cheminRepertoireTempBuildNG5);
		/**
		 * Pour l'instant: utilisant un sous répertoire du répertoire que maven utilsie lui-même pour faire
		 * son build, je considère qu'il n'y a aucun risuqe de conflit avec un autre plugin. 
		 * L'initialisation est donc terminée.
		 */

		/**
		 * Si ce répertoire n'existe pas, le build doit échouer: ce n'est pas à ce plugin de choisir et 
		 * créer le répertoire maven nécessaire pour embarquer le client web d'une application web jee 
		 */
		this.repertoireMvnJeeNG5 = new File(this.cheminRepertoireMvnJeeNG5);
		if(! (this.repertoireMvnJeeNG5.exists() && this.repertoireMvnJeeNG5.isDirectory()) ) {
			StringBuilder msgErr = new StringBuilder();
			String sautDeLigne = System.getProperty("line.separator");
			msgErr.append("PETIT-DUC: Le build Angualr 5 ne peut être réalisé, car Le répertoire [" + this.cheminRepertoireMvnJeeNG5 + "] n'existe pas, ou est un fichier. ");
			msgErr.append(sautDeLigne);
			msgErr.append("PETIT-DUC: + Vous avez deux possibilités: ");
			msgErr.append(sautDeLigne);
			msgErr.append("PETIT-DUC:  -->> Soit vous créez le répertoire [" + this.cheminRepertoireMvnJeeNG5 + "], si nécessaire en renomant préalablement le fichier qui porte ce nom.");
			msgErr.append(sautDeLigne);
			msgErr.append("PETIT-DUC: -->> Soit vous choisissez un autre répertoire dans votre projet maven, dans lequel vous souhaitez que le résultat du Build Angular 5 soit copié.");
			msgErr.append(sautDeLigne);
			msgErr.append("PETIT-DUC:      DAsn c cas, vous pouvez re-définir le chemin de ce répertoire dans le pom.xml de votre module web jee (packagé en *.war), avec le paramètre de configuration <rep-mvn-jee-ng5></rep-mvn-jee-ng5>  du petit-duc-maven-plugin");
			msgErr.append(sautDeLigne);
			
		}
		
	}
	
	
	
}
