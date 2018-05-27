package petit.duc.os.support;

import org.apache.maven.plugin.AbstractMojo;

/**
 * Une classe qui sera implémentée par tous les goals maven de PetitDuc qui exécutent des commande spécifiques à l'OS 
 * au sein duquel on exécute ce goal maven.
 * 
 * @author JEan-Baptiste Lasselle
 *
 */
public abstract class OSDependentMavenGoal  extends AbstractMojo {
	/**
	 * -------------------------------------------------------------------------------------------------------------------------------------------
	 */
	static boolean isWindows() {
	    return System.getProperty("os.name").toLowerCase().contains("win");
	}

	/**
	 * Permet de définir la commande à exécuter pour invoquer NPM, spécifiquement à l'OS sous jacent
	 */
	public static final String COMMANDE_NPM_SPECIFIQUE_OS = isWindows() ? "npm.cmd" : "npm";


/**
 * -------------------------------------------------------------------------------------------------------------------------------------------
 */
}
