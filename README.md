# Eclipse Ditto WoDT Integration
An integration for Eclipse Ditto that allows a Ditto Thing to be used as a Digital Twin in a WoDT Platform of the Web of Digital Twins.

## Requirements
In order to use this integration you will need:
- Ditto instance running in a Docker container
- Java

Note: some libraries such as "wot-servient" may have to be imported manually.

## Configuration

The module is preconfigured to work with a local Eclipse Ditto running in Docker. Find more information on
[GitHub](https://github.com/eclipse/ditto/tree/master/deployment/docker).

You can change the configuration to your liking by editing `resources/config.properties`.
The configured usernames and passwords must be added to the nginx.htpasswd of Eclipse Ditto.

```bash
htpasswd nginx.htpasswd user1
```
You can also specify the URI and port where the module will be exposed.

## To integrate a Ditto Thing:
You can integrate a Ditto Thing in two ways:
- by creating it with a modified Thing Model, which will contain some additional properties for ontology mapping ([example](https://gist.githubusercontent.com/piertv21/7555d9c936d9ce25db3a23ec4b0e580a/raw/551d2c7f08bdb539baa800908752e36b8a0e285f/ambulance-1.0.0.tm.jsonld))
- by creating it with a standard Thing Model, you will need to provide an ontology mapping in YAML format to the module ([example](https://github.com/piertv21/ditto_wodt_integration/blob/main/src/test/resources/BulbHolderDTOntology.yaml))

After creating the Ditto thing you can start the module. If you need to provide your ontology mapping in YAML indicate the path where it is saved when you instantiate the module.

Module instantiation example:
```java
WoDTAdapter woDTAdapter = WoDTAdapter.create(
  "io.eclipseprojects.ditto:ambulance",
  "AmbulanceDTOntology.yaml",
  "http://localhost:5000",
  "bulbHolderPhysicalAssetId"
);
```
Parameters:
- **Ditto thing id**: the id of your Ditto thing created in Eclipse Ditto.
- **Mapping in YAML**: path to your ontology mapping created in YAML.
- **WoDT platform URIs**: URI of the WoDT platforms to register to.
- **Physical asset id**: id of the physical asset associated with the Digital Twin.