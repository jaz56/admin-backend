FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

VOLUME /tmp

# Créer le dossier uploads dans le conteneur
RUN mkdir -p /app/uploads

COPY --from=build /workspace/target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]