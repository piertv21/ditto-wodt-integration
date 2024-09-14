package org.eclipse.ditto.wodt.DTDManager.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * YamlOntologyHandler reads a YAML file containing Ontology information
 * and provides methods to access the parsed content.
 */
public final class YamlOntologyProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlOntologyProvider.class);

    private final Optional<String> digitalTwinType;
    private final List<Optional<Map<String, String>>> properties;
    private final List<Optional<Map<String, String>>> actions;
    private final List<Optional<Map<String, String>>> events;

    /**
     * Constructor that takes the name of a YAML file and parses its content.
     */
    @SuppressWarnings("unchecked")
    public YamlOntologyProvider(String yamlFileName) {
        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(yamlFileName);
            
            if (inputStream == null) {
                LOGGER.info("File not found in the classpath, try to get it from filesystem: " + yamlFileName);
                inputStream = new FileInputStream(yamlFileName);
            }
            
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);
            
            this.digitalTwinType = Optional.ofNullable((String) data.get("digitalTwinType"));

            List<Optional<Map<String, String>>> combinedProperties = new ArrayList<>();
            List<Map<String, String>> props = (List<Map<String, String>>) data.get("properties");
            if (props != null) {
                for (Map<String, String> prop : props) {
                    combinedProperties.add(Optional.of(prop));
                }
            }

            List<Map<String, String>> rels = (List<Map<String, String>>) data.get("relationships");
            if (rels != null) {
                for (Map<String, String> rel : rels) {
                    combinedProperties.add(Optional.of(rel));
                }
            }
            this.properties = combinedProperties;
            
            List<Optional<Map<String, String>>> tempActions = new ArrayList<>();
            List<Map<String, String>> actns = (List<Map<String, String>>) data.get("actions");
            if (actns != null) {
                for (Map<String, String> act : actns) {
                    tempActions.add(Optional.of(act));
                }
            }
            this.actions = tempActions;
            
            List<Optional<Map<String, String>>> tempEvents = new ArrayList<>();
            List<Map<String, String>> evnts = (List<Map<String, String>>) data.get("events");
            if (evnts != null) {
                for (Map<String, String> evt : evnts) {
                    tempEvents.add(Optional.of(evt));
                }
            }
            this.events = tempEvents;

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error during YAML file loading: " + e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }


    /**
     * Returns the Digital Twin Type from the YAML file.
     */
    public Optional<String> getDigitalTwinType() {
        return digitalTwinType;
    }

    /**
     * Returns the properties and relationships defined in the YAML file.
     */
    public List<Optional<Map<String, String>>> getProperties() {
        return properties;
    }

    /**
     * Returns the actions defined in the YAML file.
     */
    public List<Optional<Map<String, String>>> getActions() {
        return actions;
    }

    /**
     * Returns the events defined in the YAML file.
     */
    public List<Optional<Map<String, String>>> getEvents() {
        return events;
    }
}