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
 * Ce goal permet de faire un <code>npm update</code> dans le répertoire {@see .
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
 *							<goal>npm-update</goal>
 *						</goals>
 *						<configuration>
 *							<rep-temp-build-ng5>${project.build.directory}/mon/repertoire</rep-temp-build-ng5>
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
@Mojo( name = "npm-update")
public class NPMupdate extends OSDependentMavenGoal {

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

		
	}
	
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		this.initialiser();
		this.npmUpdate();
	}

	/**
	 * Exécute la commande <code>npm update</code>
	 * @throws MojoFailureException Lorsque la commande <code>npm update</code> échoue.
	 */
	private void npmUpdate() throws MojoFailureException {
		String[] listeInvocation = new String[2];
		listeInvocation[0] = COMMANDE_NPM_SPECIFIQUE_OS;
		listeInvocation[1] = "update";
	    try {
	    	
	    	ProcessBuilder processBuilder = new ProcessBuilder(listeInvocation);
	    	ProcessBuilder leMemeProcessBuilder = processBuilder.directory(this.repertoireTempBuildNG5);
	    	// Branche automatiquement les canaux de la sortie standard et la sortie erreur du process, sur la sortie standard et le caanl de sortie d'erreurs de la JRE l'exécutant
	    	leMemeProcessBuilder.inheritIO();
	    	Process processNpmUpdate = leMemeProcessBuilder.start();
	    	processNpmUpdate.waitFor();
	    	
		} catch (IOException e) {
			throw new MojoFailureException(" PETIT-DUC: La commande [npm update] a échoué. " + e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	finally {
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
			System.out.println(" PETIT-DUC: + ATTENTION! ==>> LE  [npm update]  SE FAIT DANS UN PROCESS EN TÂCHE DE FOND, APRES LE BUILD SUCCESS " );
		}
		
		
	}


	
	
}
