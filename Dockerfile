# It generates a Docker image for a Ditto thing with its optional associated ontology
FROM openjdk:24-jdk-slim

WORKDIR /app

COPY target/dittoWodtIntegration-1.0.0-jar-with-dependencies.jar /app/dittoWodtIntegration-1.0.0-jar-with-dependencies.jar

COPY *.yaml /app/
COPY src/main/resources/ /app/

EXPOSE ${MODULE_PORT}

CMD ["java", "-jar", "/app/dittoWodtIntegration-1.0.0-jar-with-dependencies.jar"]
