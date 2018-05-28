package petit.duc;


/**
 * Pour effectuer les manipulations de fichier et répertoires.
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
 * Ce goal permet d'installer Angular CLI en exécutant un simple goal maven une seule fois, sans le lier uà une phase particulière du build.
 * 
 * Par défaut, il ne nécessite donc auccune configuration spécifique, car il utilise un seul paramètre de la configuration du pom.xml
 * le paramètre [<rep-temp-build-ng5></rep-temp-build-ng5>], qui a une valeur par défaut.
 * 
 * cf. {@see InstallAngularCLI#cheminRepertoireTempBuildNG5 }
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
 *						<phase>initialize</phase>
 *						<goals>
 *							<goal>install-angular-cli</goal>
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
 *							<!-- rep-temp-build-ng5></rep-temp-build-ng5 -->
 *
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
 * @author Jean-Baptiste Lasselle
 *
 */
@Mojo( name = "install-angular-cli")
public class InstallAngularCLI extends OSDependentMavenGoal {

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
	 * Initialise les champs non statiques, qui ne sont pas des paramètres du goal Maven
	 */
	private void initialiser() throws MojoExecutionException, MojoFailureException {
		
	
		this.repertoireTempBuildNG5 = new File(this.cheminRepertoireTempBuildNG5);
		/**
		 * Pour l'instant: utilisant un sous répertoire du répertoire que maven utilsie lui-même pour faire
		 * son build, je considère qu'il n'y a aucun risuqe de conflit avec un autre plugin. 
		 * L'initialisation est donc terminée.
		 */

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
	}
	
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		this.initialiser();
		this.installerAngularCLI();
	}

	private void installerAngularCLI() {
		String[] listeInvocation = new String[4];
		listeInvocation[0] = COMMANDE_NPM_SPECIFIQUE_OS;
		listeInvocation[1] = "install";
		listeInvocation[2] = "-g";
		listeInvocation[3] = "@angular/cli";
		
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
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> L'INSTALLATION + ANGULAR CLI A COMMENCE " );
		}
	}

	
	

	
	
}
