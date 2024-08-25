package org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.wodt.DTDManager.api.DTDManager;
import org.eclipse.ditto.wodt.DTDManager.impl.WoTDTDManager;
import org.eclipse.ditto.wodt.DTKGEngine.api.DTKGEngine;
import org.eclipse.ditto.wodt.DTKGEngine.impl.JenaDTKGEngine;
import org.eclipse.ditto.wodt.PlatformManagementInterface.api.PlatformManagementInterface;
import org.eclipse.ditto.wodt.PlatformManagementInterface.impl.BasePlatformManagementInterface;
import org.eclipse.ditto.wodt.WoDTDigitalTwinInterface.api.WoDTWebServer;
import org.eclipse.ditto.wodt.WoDTDigitalTwinInterface.impl.WoDTWebServerImpl;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.common.DittoBase;
import org.eclipse.ditto.wodt.common.ThingModelElement;
import static org.eclipse.ditto.wodt.common.ThingUtils.extractDataFromThing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents the Eclipse Ditto Adapter that allows to implement the WoDT Digital Twin layer
* implementing the components of the Abstract Architecture.
*/
public final class WoDTDigitalAdapter {

    private static final int DITTO_PORT_NUMBER = 8080;    
    private static final String BASE_URL = "http://localhost:" + DITTO_PORT_NUMBER + "/api/2/things/";

    private final DTKGEngine dtkgEngine;
    private final DTDManager dtdManager;
    private final WoDTWebServer woDTWebServer;
    private final PlatformManagementInterface platformManagementInterface;
    private final WoDTDigitalAdapterConfiguration configuration;

    private DittoBase dittoBase;
    private DittoThingListener dittoClientThread;
    private List<ThingModelElement> propertiesList;
    private List<ThingModelElement> actionsList;
    private List<ThingModelElement> eventsList;

    // TO DO: ottimizzare al meglio il codice evitando ripetizioni

    /**
     * Default constructor.
    * @param digitalAdapterId the id of the Digital Adapter
    * @param configuration the configuration of the Digital Adapter
    */
    public WoDTDigitalAdapter(
        final String digitalAdapterId,
        final WoDTDigitalAdapterConfiguration configuration,
        final String dittoThingId
    ) {
        this.configuration = configuration;
        this.dittoBase = new DittoBase(); // TO DO: rimuovi se non usato sotto
        Thing thing = dittoBase.getClient().twin()
            .forId(ThingId.of(dittoThingId))
            .retrieve()
            .toCompletableFuture()
            .join();

        List<List<ThingModelElement>> result = extractDataFromThing(thing);
        this.propertiesList = result.get(1);
        this.actionsList = result.get(2);
        this.eventsList = result.get(3);

        this.platformManagementInterface = new BasePlatformManagementInterface(
                configuration.getDigitalTwinUri());
        this.dtkgEngine = new JenaDTKGEngine(configuration.getDigitalTwinUri());
        this.dtdManager = new WoTDTDManager(
                configuration.getDigitalTwinUri(),
                configuration.getOntology(),
                configuration.getPhysicalAssetId(),
                configuration.getPortNumber(),
                this.platformManagementInterface,
                result.get(0),
                this.propertiesList,
                this.actionsList,
                this.eventsList,
                BASE_URL + dittoThingId
        );

        this.syncWithDittoThing(thing, configuration);

        this.woDTWebServer = new WoDTWebServerImpl(
                configuration.getPortNumber(),
                this.dtkgEngine,
                this.dtdManager,
                (actionName, body) -> {
                    try {
                        //publishDigitalActionWldtEvent(actionName, body); // TO DO: cambia qui, inviare msg in base all'actionName e body
                        return true;
                    } catch (Exception e) {
                        Logger.getLogger(WoDTDigitalAdapter.class.getName())
                                .log(Level.INFO, "Impossible to forward action: {0}", e);
                        return false;
                    }
                },
                this.platformManagementInterface
        );
        this.woDTWebServer.start();
        /* TO DO: configuration.getPlatformToRegister().forEach(platform ->
                this.platformManagementInterface.registerToPlatform(platform, this.dtdManager.getDTD().toJson()));*/
        
        this.dittoClientThread = new DittoThingListener(this);
        dittoClientThread.start();
    }

    private List<String> extractSubPropertiesNames(String jsonProperty) {
        List<String> subProperties = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonProperty);
            if (rootNode.isObject()) {
                Iterator<String> fieldNames = rootNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    subProperties.add(fieldName);
                }
            }
        } catch (Exception e) {
            System.out.println("Error");
        }
        return subProperties;
    }

    public static String extractSubPropertyValue(String jsonValue, String key) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonValue);
            if (rootNode.isObject() && rootNode.has(key)) {
                JsonNode subPropertyNode = rootNode.get(key);
                return subPropertyNode.asText();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void syncWithDittoThing(Thing thing, WoDTDigitalAdapterConfiguration configuration) {
        // PROPERTIES (Thing Attributes)
        thing.getAttributes().ifPresent(attributes -> {
            attributes.forEach((attribute) -> {
                ThingModelElement property = this.propertiesList.stream()
                    .filter(p -> p.getElement().equals(attribute.getKey().toString()))
                    .findFirst()
                    .orElse(null);
                configuration.getOntology().convertPropertyValue(
                    property.getElement(),
                    attribute.getValue().asString()
                ).ifPresent(triple ->
                        this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                );
                this.dtdManager.addProperty(property.getElement());
            });
        });

        // PROPERTIES, ACTIONS, EVENTS (from Thing Features)
        thing.getFeatures().ifPresent(features -> {
            features.forEach((featureName) -> {
                featureName.getProperties().ifPresent(properties -> {
                    properties.forEach((property) -> {
                        List<String> subProperties = extractSubPropertiesNames(property.getValue().toString()); // Check for subproperties
                        if(!subProperties.isEmpty()) {
                            subProperties.forEach(subProperty -> {                                
                                ThingModelElement featureProperty = this.propertiesList.stream()
                                    .filter(p -> p.getElement().equals(property.getKey().toString() + "_" + subProperty))
                                    .filter(p -> p.getValue().get().equals(featureName.getId()))
                                    .findFirst()
                                    .orElse(null);
                                String fullPropertyName = featureProperty.getValue().get() + "_" + property.getKey() + "_" + subProperty;
                                configuration.getOntology().convertPropertyValue(
                                    fullPropertyName,
                                    extractSubPropertyValue(property.getValue().toString(), subProperty)
                                ).ifPresent(triple ->
                                    this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                                );
                                this.dtdManager.addProperty(fullPropertyName);
                            });
                        } else {
                            ThingModelElement featureProperty = this.propertiesList.stream()
                                .filter(p -> p.getElement().equals(property.getKey().toString()))
                                .filter(p -> p.getValue().get().equals(featureName.getId()))
                                .findFirst()
                                .orElse(null);
                            String fullPropertyName = featureProperty.getValue().get() + "_" + property.getKey().toString();
                            configuration.getOntology().convertPropertyValue(
                                fullPropertyName,
                                property.getValue().toString()
                            ).ifPresent(triple ->
                                this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                            );
                            this.dtdManager.addProperty(fullPropertyName);
                        }
                    });
                });
            });
        });

        // ACTIONS (Thing Actions)
        this.actionsList.stream()
            .filter(action -> action.getValue().isEmpty()) // Thing Action = no associated value
            .forEach(action -> {                
                this.dtdManager.addAction(action.getElement());
                this.dtkgEngine.addActionId(action.getElement());
            });

        // EVENTS (Thing Events)
        // TO DO
    }

    public void stop() {
        this.platformManagementInterface.signalDigitalTwinDeletion();
        this.dittoClientThread.stopThread();
    }

    public void onThingChange(ThingChange change) {
        System.out.println(change);

        // CUD Attributi
        // CUD Features

        switch (change.getAction()) {
            case CREATED:
                // TO DO
                break;
            case DELETED:
                // TO DO
                break;
            case UPDATED:
                if (change.getThing().get().getAttributes().isPresent()) {  // Update Thing Attributes
                    change.getThing().get().getAttributes().get().forEach((attribute) -> {
                        ThingModelElement prop = this.propertiesList.stream()
                            .filter(p -> p.getElement().equals(attribute.getKey().toString()))
                            .findFirst()
                            .orElse(null);
                        configuration.getOntology().convertPropertyValue(
                            attribute.getKey().toString(),
                            attribute.getValue().asString()
                        ).ifPresent(triple ->
                                this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                        );
                        this.dtdManager.addProperty(attribute.getKey().toString());
                    });
                }
                if (change.getThing().get().getFeatures().isPresent()) {    // Update Thing Features (Properties)
                    change.getThing().get().getFeatures().get().forEach((featureName) -> {
                        featureName.getProperties().ifPresent(properties -> {
                            properties.forEach((property) -> {
                                ThingModelElement featureProp = this.propertiesList.stream()
                                    .filter(p -> p.getElement().equals(property.getKey().toString()))
                                    .filter(p -> p.getValue().get().equals(featureName.getId()))
                                    .findFirst()
                                    .orElse(null);
                                configuration.getOntology().convertPropertyValue(
                                    property.getKey().toString(),
                                    property.getValue().toString()
                                ).ifPresent(triple ->
                                    this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                                );
                                this.dtdManager.addProperty(property.getKey().toString());
                            });
                        });
                    });
                }
                break;
            default:
                break;
        }
    }
}