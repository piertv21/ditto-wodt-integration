# It generates a Docker image for a Ditto thing with its optional associated ontology
FROM openjdk:24-jdk-slim

WORKDIR /app

COPY target/dittoWodtIntegration-1.0.0-jar-with-dependencies.jar /app/dittoWodtIntegration-1.0.0-jar-with-dependencies.jar

RUN rm -f src/main/resources/*.yaml
COPY ${YAML_ONTOLOGY_PATH} /app/resources/
COPY src/main/resources/ /app/resources/

EXPOSE ${MODULE_PORT}

CMD ["java", "-jar", "/app/dittoWodtIntegration-1.0.0-jar-with-dependencies.jar"]
