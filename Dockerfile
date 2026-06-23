FROM eclipse-temurin:17-jdk-alpine

VOLUME /tmp

# Créer le dossier uploads dans le conteneur
RUN mkdir -p /app/uploads

COPY target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]