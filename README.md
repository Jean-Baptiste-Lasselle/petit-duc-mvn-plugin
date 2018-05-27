# System Requirements

Ce plugin suppose que NPM est déjà installé au moins pour l'utilisateur linux/windows qui exécute les processus maven.

# Cuisine

Ce repository versionne le code source d'un plugin maven, que j'ai baptisé `petit-duc-mvn-plugin`, en 
référence au majestueux Hibou Français, à la vue perçante.


Ce plugin est l'un des composants d'une expérimentation mettant en jeu:
* une application jee web, avec client angular 5, et base de données mongodb:
  * la partie Java de l'application est versionnée par le repository [petit-poivre-jee](https://github.com/Jean-Baptiste-Lasselle/petit-poivre-jee)
  * la partie Java de l'application est versionnée par le repository [petit-poivre-angular5](https://github.com/Jean-Baptiste-Lasselle/petit-poivre-angular5)
* un plugin maven à la vue perçante [petit-duc-mvn-plugin](https://github.com/Jean-Baptiste-Lasselle/petit-duc-mvn-plugin)
* une recette de provision d'une cible de déploiement tomcat / mongodb "dockerisée",  versionnée par le repository [petit-poivre-cible-deploiement](https://github.com/Jean-Baptiste-Lasselle/cible-deploiement-petit-poivre)


Le but de l'expérimentation est de proposer un processus de développement, et les outils qui permettent de le mettre en oeuvre, pour deux 
équipes séparées géographiquement et/ou technologiquement, et travaillant sur la même application.

L'intention est de chercher comment une équipe technique peut faire du pilote de projet 
un homme aux 5 sens augmentés.


# Le Petit Duc

## System Requirements 

Ce plugin suppose que NPM est déjà instalé dans le système exécutant les processus MAVEN

## Builder le Petit Duc à partir de son code source

Pour votre environnement local de développement:

```
mvn clean install site:site
```

Pour en plus, déployer dans le repository maven privé de votre projet:

```
mvn clean deploy site:site
```



## Ce que permet ce plugin 


Le contexte est le suivant.
On développe une application web jee, avec client Angular 5. Le code source est versionné avec deux repository Git distincts:
* Un repository Git versionne tout le code source, à l'exclusion de la partie cliente Angular 5 (exécutée dans le browser).
* Un repository Git versionne le code source de la partie cliente Angular 5

Ce plugin permet :
* de tirer la version `$ID_VERSION` du repository versionnant le code source de la partie cliente Angular 5
* de réaliser le "build" de la partie cliente Angular 5 (donc en "typescript") versionné sur un repositoy différent du repository
* de copier le résultat du build dans le répertoire maven `src/main/webapp` d'un module web jee (appli web)

Pour ensuite poursuivre et terminer le build du war complet, avec le reste du code source.

la configuration complète de ce plugin, surchargeant toutes les valeurs par défaut, est:

```
	<build>
		<!-- <finalName>petit-poivre-jee</finalName> -->
		<plugins>
			<plugin>
				<groupId>aramitz</groupId>
				<artifactId>petit-duc</artifactId>
				<version>1.0.0</version>
				<executions>
					<execution>
						<phase>generate-sources</phase>
						<goals>
							<goal>petit-duc-ng5</goal>
						</goals>
						<configuration>
							<!-- L'URI du repository Git versionnant le code source de la partie cliente Angular 5 -->
							<uri-repo-client-ng5>https://github.com/Jean-Baptiste-Lasselle/petit-poivre-angular5</uri-repo-client-ng5>
							<version-client-ng5>1.0.9</version-client-ng5>
							<!-- Utilisateur Git utilisé par l'usine logicielle, ou le développeur -->
							<git-user-name>jlasselle</git-user-name>
							<!-- pas de mot de passe dans un fichier versionné, l'authentification doit se faire:
									¤ par demande interactive de mot de passe, lorsque l'option java -Dpetit.duc.git.pwd=votremotdepasse n'est pas utilisée
									¤ Avec l'option java -Dpetit.duc.git.pwd=votremotdepasse  dans tous les autres cas. 
									¤ On pourra améliorer en proposant un mécanisme de configuration de l'authentification pour supporter les mécanismes d'autentification suivants: 
											+ Java - usePAM   + free ipa server
											+ Java -> PKI Certificats SSL TLS + Lets Encrypt + free ipa server   
											+ OAUTH2 / saml Keycloak free ipa server   
											+ json web token / jwt server + free ipa server
							-->
							<!-- la valeur qui sera donnée à l'option [--base-href], piur l'exécution de la commande [ng build --prod --base-href=./protail-des-hiboux] -->
							<ng-build-base-href>./portail-des-hiboux/</ng-build-base-href>

						</configuration>
					</execution>
				</executions>
			</plugin>

			
			
			
			
			
			
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.7.0</version>
				<configuration>
					<source>${source.java.version}</source>
					<target>${target.java.version}</target>
				</configuration>
			</plugin>

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<configuration>
					<includes>
<!-- 						<include>%regex[.*(Cat|Dog).*Test.*]</include> -->
						<!-- Avec cette configuration, tous les noms sont autorisés pour les classes de tests unitaires -->
						<include>**/*.java</include>
					</includes>
				</configuration>
			</plugin>


</plugins>
```
Avec cette configuration, il vous est alors possible de passer en paramètre de l'exécution du build maven 
de votre applciation web jee, le mot de passe de petit duc pour l'accès au repository Git versionnant le code source du client Angular 5:

mvn clean package -Dpetit.duc.git.pwd=votremotdepasse


Si vous utilsiez l'IDE Eclipse, vous pouvez passez ce paramètre dans les "Run Configurations" Maven de votre projet app web jee.






