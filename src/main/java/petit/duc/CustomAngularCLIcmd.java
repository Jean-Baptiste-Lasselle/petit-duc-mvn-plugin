package petit.duc;


/**
 * Pour effectuer les manipulations de fichier et répertoires.
 */
import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
/**
 * Pour créer un goal ("Mojo") maven
 */
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;



/**
 * Ce goal permet d'exécuter une commande Angulmar CLI, dans un répertoire quelconque , avec une combinaison quelconque d'options et d'arguments telle que:	
 * 
 * <ul>
 * 	 <li> <code>ng open --serve</code> </li>
 * 	 <li> <code>ng generate component banner --inline-template --inline-style --module app</code> </li>
 * 	 <li> <code>ng test</code> </li>
 *   <li> etc...  </li>
 * </ul>
 * Par défaut, la commande exécutée est [ng --version] ou [ng --help]
 * 
 * 
 * Ne surtout pas hésiter à aller voir:
 * <ul>
 * 	<li>https://angular.io/guide/testing</li>
 *  <li>https://codecraft.tv/courses/angular/unit-testing/jasmine-and-karma/</li>
 * </ul>
 * 
 * Pour exploiter ce goal maven.
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
 *						<phase>process-sources</phase>
 *						<goals>
 *							<goal>build-ng5</goal>
 *						</goals>
 *						<configuration>
 *
 *
 *
 *							<!-- la combinaison d'options et d'arguments à passer à  la commande angular-cli  -->
 *							<ng-cmd-opts-args> generate component banner --inline-template --inline-style --module app</ng-cmd-opts-args>
 *							<!-- le répertoire dns lequel exécuter la commande custom -->
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
@Mojo( name = "ng-custom")
public class CustomAngularCLIcmd extends AbstractMojo {


	/**
	 * <version-client-ng5></version-client-ng5>
	 * -----------------------------------------
	 * Ce paramètre permet de définir une combinaison d'options et d'arguments pour une exécutiuon de commandes ng telles que:	
	 * 
	 * <ul>
	 * 	 <li> <code>ng open --serve</code> </li>
	 * 	 <li> <code>ng generate component banner --inline-template --inline-style --module app</code> </li>
	 * 	 <li> <code>ng test</code> </li>
	 *   <li> etc...  </li>
	 * </ul>
	 * Par défaut, la commande exécutée est [ng --version] ou [ng --help]
	 */
	@Parameter(alias = "ng-cmd-opts-args", property = "ng-cmd-opts-args", defaultValue = "--help")
	private String ngCmdOptsArgs;
	
	/**
	 * <rep-exec-custom-ng-cmd></rep-exec-custom-ng-cmd>
	 * -------------------------------------------------
	 * Le chemin absolu du répertoire dans lequel sera exécutée la commande angular-cli
	 * Le paramètre est obligatoire.
	 * 
	 */
	@Parameter(alias = "rep-exec-custom-ng-cmd", property = "rep-exec-custom-ng-cmd", required=true )
	private String cheminRepertoireExecNGcmd;
	
	/**
	 * Le répertoire temporaire dans lequel le build angular 5 sera fait.
	 */
	private File repertoireExecNGcmd;
	
	
	/**
	 * Initialise les champs non statiques, qui ne sont pas des paramètres du goal Maven
	 * @throws MojoFailureException  Lorsque le répertoire {@see CustomAngularCLIcmd#cheminRepertoireExecNGcmd} n'existe pas, ou est un fichier.
	 */
	private void initialiser() throws MojoExecutionException, MojoFailureException {
		
	
		this.repertoireExecNGcmd = new File(this.cheminRepertoireExecNGcmd);
		/**
		 * On ne peut exécuter une commande Angular CLI dans un répertoire qui n'existe pas !!
		 */
		if(! (this.repertoireExecNGcmd.exists() && this.repertoireExecNGcmd.isDirectory()) ) {
			StringBuilder msgErr = new StringBuilder();
			String sautDeLigne = System.getProperty("line.separator");
			msgErr.append("PETIT-DUC: La commande Angular CLI [ng " + this.ngCmdOptsArgs + "] ne peut être exécutée dans le répertoire [" + this.cheminRepertoireExecNGcmd + "], car il n'existe pas, ou il s'agit d'un fichier. ");
			msgErr.append(sautDeLigne);
			throw new MojoFailureException(msgErr.toString());
		}
		
	}
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		this.initialiser();
		this.executerCommandeAngularCLI();
	}

	/**
	 * Exécute la commande Angular CLI custom
	 * 
	 * @throws MojoFailureException Lors que la commande Angular CLI échoue.
	 */
	private void executerCommandeAngularCLI() throws MojoFailureException {
	    try {
	    	ProcessBuilder processBuilder =  new ProcessBuilder("ng", this.ngCmdOptsArgs);
	    	ProcessBuilder leMemeProcessBuilder = processBuilder.directory(this.repertoireExecNGcmd);
	    	// Branche automatiquement les canaux de la sortie standard et la sortie erreur du process, sur la sortie standard et le caanl de sortie d'erreurs de la JRE l'exécutant
	    	leMemeProcessBuilder.inheritIO();
	    	Process processsOScommandeAngularCLI = leMemeProcessBuilder.start();
		} catch (IOException e) {
			throw new MojoFailureException(" PETIT-DUC: La commande Angular CLI [ng " + this.ngCmdOptsArgs + "] a échoué. " + e);
		}
		
	}


	
	
}
