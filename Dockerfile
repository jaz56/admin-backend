# On part d'une base Java 17 (modifie si tu utilises Java 21)
FROM eclipse-temurin:17-jdk-alpine

# On crée un volume temporaire
VOLUME /tmp

# On copie le fichier .jar qu'on a généré à l'étape 1 dans le conteneur
COPY target/*.jar app.jar

# On dit à Docker quelle commande lancer au démarrage
ENTRYPOINT ["java","-jar","/app.jar"]