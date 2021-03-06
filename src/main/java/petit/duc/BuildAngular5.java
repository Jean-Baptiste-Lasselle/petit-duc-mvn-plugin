package petit.duc;


/**
 * Pour effectuer les manipulations de fichier et répertoires.
 */
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

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
 * Attention!! : >> le numéro de version souhaité pour le code source angualr 5 à récupérer, n'estpas encore pris en cahrge dans le code du plugin maven.
 * 
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
 *									¤ par demande interactive de mot de passe, lorsque l'option java -Dpetit.duc.git.pwd=votremotdepasse n'est pas utilisée
 *									¤ Avec l'option java -Dpetit.duc.git.pwd=votremotdepasse  dans tous les autres cas. 
 *									¤ On pourra améliorer en proposant un mécanisme de configuration de l'authentification pour supporter les mécanismes d'autentification suivants: 
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
@Mojo( name = "build-ng5")
public class BuildAngular5 extends OSDependentMavenGoal {

	/**
	 * <uri-repo-client-ng5></uri-repo-client-ng5>
	 * -------------------------------------------
	 * L'URI du repo Git versionnant le code source du client Angular 5 de votre applciation Web Jee.
	 * 
	 */
	@Parameter(alias = "uri-repo-client-ng5", property = "uri-repo-client-ng5", required = true)
	private String uriRepoClientNG5;
	
	/**
	 * <version-client-ng5></version-client-ng5>
	 * -----------------------------------------
	 * Ce paramètre permet de définir la version exacte (par exemple avec le HASH du commit)
	 * du code source du client Angular 5, que l'on souhaite embarquer dans le build du war.
	 * 
	 * Ce paramètre est optionnel: par défaut, sa valeur est la chaîne de caractère vide. La version
	 * du client Angular 5 embarquée, est alors la dernière version de la
	 *  branche master du repository {@see BuildAngular5#uriRepoClientNG5 }
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
	 * -----------------------------------
	 * Le chemin absolu du répertoire maven dans lequel le résultat du build angular 5 sera copié, pour
	 * être inclut dans le build du war.
	 * Le paramètre est optionnel, il a une valeur par défaut qui peut être surchargée
	 * 
	 */
	@Parameter(alias = "rep-mvn-jee-ng5", property = "rep-mvn-jee-ng5", defaultValue = "${project.basedir}/src/main/webapp/hibou")
	private String cheminRepertoireMvnJeeNG5;
	/**
	 * Le répertoire maven dans lequel le résultat du build angular 5 sera copié, pour
	 * être inclut dans le build du war.
	 */
	
	private File repertoireMvnJeeNG5;

	
	/**
	 * <git-user-name></git-user-name>
	 * -------------------------------
	 * Le nom d'utilisateur que le Petit Duc va utilsier pour s'authentifier auprès de Git pour récupérer le code source du client Anguilar 5. 
	 */
	@Parameter(alias = "git-user-name", property = "git-user-name", required = true)
	private String GITusername;

	/**
	 * Demandé interactivement à l'utilisateur, s'il n'est pas définit par la propriété java {@see BuildAngular5#PETIT_DUC_GIT_PWD_ENV_KEY}
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
//	@Parameter(alias = "git-user-pwd", property = "git-user-pwd", defaultValue = "")
	private String GITuserpwd = null;
	
	/**
	 * 
	 * <code><ng-build-base-href>./nomrepertoire</ng-build-base-href></code>
	 * --------------------------------------------------------------
	 * 
	 * Permet de définir la valeur utilisée pour l'option [--base-href] de la commande
	 * 
	 * 			<code>ng build --prod --base-href="valeurBaseHref"</code>
	 * 
	 */
	@Parameter(alias = "ng-build-base-href", property = "ng-build-base-href", defaultValue = ".")
	private String ngBuildBaseHref;
	
	
	
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
		this.installerAngularCore();
		this.npmInstall();
		this.npmUpdate();
		System.out.println(" PETIT-DUC: + >>>>>>>>>>>>>>>>>>>>>>>>>  TETE CHERCHEUSE NULL POINTER  " );
		this.faireLeBuildAngular5(this.ngBuildBaseHref);
		
		/**
		 * 3./ On fait la copie du résultat du build, [{@see BuildAngular5#cheminRepertoireTempBuildNG5}/dist/] dans le répertoire {@see BuildAngular5#repertoireMvnJeeNG5}
		 */

		
		this.embarquerClientAngular5();
	}






	/**
	 * Réalise la copie du résultat du build, [{@see BuildAngular5#cheminRepertoireTempBuildNG5}/dist/] dans le répertoire {@see BuildAngular5#repertoireMvnJeeNG5}
	 * @throws MojoFailureException 
	 */
	private void embarquerClientAngular5() throws MojoFailureException {
//		throw new MojoFailureException("la méthode {@see BuildAngular5#embarquerClientAngular5() } n'a pas encore été implémentée");
		File repertoireDist = new File(this.cheminRepertoireTempBuildNG5 + "/dist/");
		try {
			org.apache.commons.io.FileUtils.copyDirectoryToDirectory(repertoireDist, this.repertoireMvnJeeNG5);
		} catch (IOException e) {
			StringBuilder msgErr = new StringBuilder();
			String sautDeLigne = System.getProperty("line.separator");
			msgErr.append("PETIT-DUC: + Le build Angualr 5 ne peut être réalisé, car la copie du contenu du répertoire [" + this.cheminRepertoireTempBuildNG5 + "]  ");
			msgErr.append(sautDeLigne);
			msgErr.append("PETIT-DUC: + vers le répertoire [" + this.cheminRepertoireMvnJeeNG5 + "]  a echouée:");
			msgErr.append(sautDeLigne);
			msgErr.append("PETIT-DUC: + message de l'erreur Java: [" + e.getMessage() + "] ");
			msgErr.append(sautDeLigne);
			throw new MojoFailureException(msgErr.toString(), e);
			
		}
		
	}

	
	/**
	 * J'ai vérifié qu'il faut absolument faire un "npm install" , après avoir fait le "npm install -g  @angular/cli", pour
	 * pouvoir faire des commandes "ng-build ensuite".
	 * 
	 */
	private void npmInstall() {
		String[] listeInvocation = new String[2];
		listeInvocation[0] = COMMANDE_NPM_SPECIFIQUE_OS;
		listeInvocation[1] = "install";
		
	    try {
	    	ProcessBuilder processBuilder = new ProcessBuilder(listeInvocation);
	    	ProcessBuilder leMemeProcessBuilder = processBuilder.directory(this.repertoireTempBuildNG5);
	    	// Branche automatiquement les canaux de la sortie standard et la sortie erreur du process, sur la sortie standard et le caanl de sortie d'erreurs de la JRE l'exécutant
	    	leMemeProcessBuilder.inheritIO();
	    	Process processNpmInstall = leMemeProcessBuilder.start();
	    	processNpmInstall.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * Et un NPM update
	 */
	private void npmUpdate() {
		String[] listeInvocation = new String[2];
		listeInvocation[0] = COMMANDE_NPM_SPECIFIQUE_OS;
		listeInvocation[1] = "update";
		
	    try {
	    	ProcessBuilder processBuilder = new ProcessBuilder(listeInvocation);
	    	ProcessBuilder leMemeProcessBuilder = processBuilder.directory(this.repertoireTempBuildNG5);
	    	// Branche automatiquement les canaux de la sortie standard et la sortie erreur du process, sur la sortie standard et le caanl de sortie d'erreurs de la JRE l'exécutant
	    	leMemeProcessBuilder.inheritIO();
	    	Process processNpmInstall = leMemeProcessBuilder.start();
	    	processNpmInstall.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally   {
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> NPM UPDATE EN COURS  " );
		}
	}
	/**
	 * Parce qu'apparrment Angular Core Manquait
	 */
	private void installerAngularCore() {
		String[] listeInvocation = new String[3];
		listeInvocation[0] = COMMANDE_NPM_SPECIFIQUE_OS;
		listeInvocation[1] = "install";
		listeInvocation[2] = "@angular/core";
		
	    try {
	    	ProcessBuilder processBuilder = new ProcessBuilder(listeInvocation);
	    	ProcessBuilder leMemeProcessBuilder = processBuilder.directory(this.repertoireTempBuildNG5);
	    	// Branche automatiquement les canaux de la sortie standard et la sortie erreur du process, sur la sortie standard et le caanl de sortie d'erreurs de la JRE l'exécutant
	    	leMemeProcessBuilder.inheritIO();
	    	Process processNpmInstall = leMemeProcessBuilder.start();
	    	processNpmInstall.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally   {
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> L'INSTALLATION + ANGULAR CORE A COMMENCE " );
		}
	}
	/**
	 * Réalise la commande :
	 * 
	 * 			ng build --prod --base-href="valeurBaseHref"
	 * 
	 * dans le répertoire {@see BuildAngular5#cheminRepertoireTempBuildNG5}
	 * 
	 * https://stackoverflow.com/questions/40503074/how-to-run-npm-command-in-java-using-process-builder
	 * 
	 * @param valeurBaseHref
	 */
	private void faireLeBuildAngular5(String valeurBaseHref) {
		
		String[] listeInvocation = new String[4];
		listeInvocation[0] = COMMANDE_NG_SPECIFIQUE_OS;
		listeInvocation[1] = "build";
		listeInvocation[2] = "--prod";
		listeInvocation[3] = "--base-href=\"" + valeurBaseHref + "\"";
	    try {
//	    	Process process = new ProcessBuilder(COMMANDE_NPM_SPECIFIQUE_OS, optsEtAgumentsNPM).directory(this.repertoireTempBuildNG5).start();
	
	    	ProcessBuilder processBuilder = new ProcessBuilder(listeInvocation);
	    	ProcessBuilder leMemeProcessBuilder = processBuilder.directory(this.repertoireTempBuildNG5);
	    	// Branche automatiquement les canaux de la sortie standard et la sortie erreur du process, sur la sortie standard et le caanl de sortie d'erreurs de la JRE l'exécutant
	    	leMemeProcessBuilder.inheritIO();
	    	Process processNGbuild = leMemeProcessBuilder.start();
	    	processNGbuild.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	finally   {
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE BUILD DU CLIENT ANGULAR 5 EST EN COURS... " );
			
		}
		
	}





	/**
	 * Exécute le git clone du code source de l'applciation Angular 5, dans le répertoire{@see BuildAngular5#uriRepoClientNG5 }
	 * 
	 * @throws MojoFailureException
	 */
	private void recupererCodeSourceAngular5() throws MojoFailureException {

		// je le détruis, et le re-créée
		try {
			if (this.repertoireTempBuildNG5.exists()) {
				FileUtils.forceDelete(this.repertoireTempBuildNG5);
			}
		} catch (IOException e2) {
			throw new MojoFailureException(" PETIT-DUC: + Un problème est survenu à la suppression du répertoire [" + this.cheminRepertoireTempBuildNG5 + "], avant la récupération du code source du client Angular 5.", e2);
		}
		boolean AETECREE = this.repertoireTempBuildNG5.mkdirs();
		String msgINFOcreationDirRepo = "";
		if (AETECREE) {
			msgINFOcreationDirRepo = " PETIT-DUC: + le Repertoire [" + this.cheminRepertoireTempBuildNG5 + "] a été détruis et re-créé.";
		} else {
			msgINFOcreationDirRepo = " PETIT-DUC: + le Repertoire [" + this.cheminRepertoireTempBuildNG5 + "] n'a pas été re-créé.";
		}
		
		System.out.println(msgINFOcreationDirRepo );
		
		// CREATION DU REPO LOCAL GIT

		Git monrepogit = null;
		// GIT INIT // NON, UN GIT CLONE AU DEPART
		String URLduREPO = this.uriRepoClientNG5;
		CloneCommand cloneCommand = null;
		try {
			cloneCommand = Git.cloneRepository();
			cloneCommand.setDirectory(this.repertoireTempBuildNG5);
			cloneCommand.setURI(URLduREPO);
			cloneCommand.setCredentialsProvider( new UsernamePasswordCredentialsProvider( this.GITusername, this.GITuserpwd ) );
			monrepogit = cloneCommand.call();
//			monrepogit = Git.init().setDirectory(repoDIR).call();
		} catch (IllegalStateException e) {
			throw new MojoFailureException("  PETIT-DUC: +  Le git clone du repo [" + this.uriRepoClientNG5 + "] a échoué.", e);
		} catch (GitAPIException e3) {

			throw new MojoFailureException(" PETIT-DUC: +  ERREUR AU GIT INIT  DANS  [" + this.cheminRepertoireTempBuildNG5 + "]", e3);
		} finally {
			monrepogit.close();
			/**
			 * Enfin, je fais d'emblée une chose: supprimer le répertoire ".git/", pour ne pas avori de blocage I/O avec eclipse, et ne pas copier ce "./git/" dans le répertoire [src/main/webapp/hibou/]
			 */
			File repertoirePointGit = new File(this.cheminRepertoireTempBuildNG5 + "/.git/");
			try {
				FileUtils.forceDelete(repertoirePointGit);
			} catch (IOException e5) {
				throw new MojoFailureException(" PETIT-DUC: + Un problème est survenu à la suppression du répertoire [" + repertoirePointGit + "], avant l'exécution du build du client angular 5.", e5);
			}
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
	 * @throws MojoExecutionException  Lorsque le mot de passe saisit par l'utilisateur est null ou réduit à la chaîne de caractères vide, PetitDuc n'acceotant pas de tels mots de passes
	 * @throws MojoFailureException dans le cas où le répertoire {@see BuildAngular5#cheminRepertoireTempBuildNG5 } n'existe pas ou soit un fichier.
	 */
	private void initialiser() throws MojoExecutionException, MojoFailureException {
		
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
				// Dans ce cas là, il faut faire la demande interactive
				this.GITuserpwd = demanderMotDePassePrRepoGit(this.GITusername, this.uriRepoClientNG5);
			} else {
				this.GITuserpwd = CandidateGITuserpwd;
			}
		}
//		this.GITuserpwd = null;
		
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
			msgErr.append("PETIT-DUC:      Dans ce cas, vous pouvez re-définir le chemin de ce répertoire dans le pom.xml de votre module web jee (packagé en *.war), avec le paramètre de configuration <rep-mvn-jee-ng5></rep-mvn-jee-ng5>  du petit-duc-maven-plugin");
			msgErr.append(sautDeLigne);
			throw new MojoFailureException(msgErr.toString());
		}
		
	}
	
	
	/**
	 * Cette méthode st appelée afin de demander interactivement à l'utilisateur un nom d'utilisateur et un mot de passe pour l'authentificiation à un repo Git.
	 * @param username
	 * @param URL_DU_REPO
	 * @return
	 * @throws MojoExecutionException
	 */
	private String demanderMotDePassePrRepoGit(String username, String URL_DU_REPO) throws MojoExecutionException {
		String motdepasse = null;
		
		motdepasse = JOptionPane.showInputDialog(" PETIT-DUC: Quel est le mot de passe de " + "[" + username + "]" + " pour accéder au repository Git " + "[" + URL_DU_REPO + "]" + " ?",
				null);
		if (!(motdepasse != null && motdepasse.length() >= 1)) {
			StringBuilder message1 = new StringBuilder();
			String sautLigne = System.getProperty("line.separator");
			
			message1.append("Pour versionner (clone, commit & push) le code source édité");
			message1.append(sautLigne);
			message1.append("dans le repository Git " + "[" + URL_DU_REPO + "]");
			message1.append(sautLigne);
			message1.append(" L'utilisateur Git: "+ "[" + username + "]" +") ");
			message1.append(sautLigne);
			message1.append(" a saisit un mot de passe null ou de longueur strictement inférieure à 1 ");
			message1.append(sautLigne);
			message1.append("La chaîne de caractère vide et null ne sont pas acceptées par le PETIT-DUC ");
			message1.append(sautLigne);
			message1.append("en tant que mot de passe pour une authentification.");
			message1.append(sautLigne);
			
			throw new MojoExecutionException(message1.toString());
		}
		
		return motdepasse;
		
	}
	
	
}
