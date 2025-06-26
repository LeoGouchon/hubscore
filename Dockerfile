# Étape 1 : Construction avec Maven + Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Image d'exécution avec Java 21 JDK
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/hubscore.jar app.jar

# Port utilisé par Render (sera mappé dynamiquement)
EXPOSE 8080

# Commande pour démarrer Spring Boot
CMD ["java", "-jar", "app.jar"]
