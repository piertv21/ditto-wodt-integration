package org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents the Eclipse Ditto Adapter that allows to implement the WoDT Digital Twin layer
* implementing the components of the Abstract Architecture.
*/
public final class WoDTDigitalAdapter {

    private final DTKGEngine dtkgEngine;
    private final DTDManager dtdManager;
    private final WoDTWebServer woDTWebServer;
    private final PlatformManagementInterface platformManagementInterface;
    private final WoDTDigitalAdapterConfiguration configuration;    
    private final DittoThingListener dittoClientThread;

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
        Thing thing = getDittoThing(dittoThingId);

        this.configuration = configuration;
        this.platformManagementInterface = new BasePlatformManagementInterface(
                configuration.getDigitalTwinUri());
        this.dtkgEngine = new JenaDTKGEngine(configuration.getDigitalTwinUri());
        this.dtdManager = new WoTDTDManager(
                configuration.getDigitalTwinUri(),
                configuration.getOntology(),
                configuration.getPhysicalAssetId(),
                configuration.getPortNumber(),
                this.platformManagementInterface,
                thing
        );
        this.syncWithDittoThing(thing, configuration);
        this.woDTWebServer = new WoDTWebServerImpl(
                configuration.getPortNumber(),
                this.dtkgEngine,
                this.dtdManager,
                this.platformManagementInterface
        );
        this.dittoClientThread = new DittoThingListener(this);
        this.startAdapter();
    }

    private Thing getDittoThing(String dittoThingId) {
        return new DittoBase().getClient().twin()
            .forId(ThingId.of(dittoThingId))
            .retrieve()
            .toCompletableFuture()
            .join();
    }

    private void startAdapter() {
        this.woDTWebServer.start();
        /* TO DO: this.configuration.getPlatformToRegister().forEach(platform ->
                this.platformManagementInterface.registerToPlatform(platform, this.dtdManager.getDTD().toJson())); */
        dittoClientThread.start();
    }

    /*
     * Extract the names of the subproperties of a given property.
     * e.g. {"subproperty1": "value1", "subproperty2": "value2"}
     * returns ["subproperty1", "subproperty2"]
     */
    private List<String> extractSubPropertiesNames(final String jsonProperty) {
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
        } catch (JsonProcessingException e) {
            System.out.println("Error");
        }
        return subProperties;
    }

    /*
     * Extract the value of a subproperty of a given property.
     * e.g. {"subproperty1": "value1", "subproperty2": "value2"}
     * returns "value1" if key = "subproperty1"
     */
    public static String extractSubPropertyValue(final String jsonValue, final String key) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonValue);
            if (rootNode.isObject() && rootNode.has(key)) {
                JsonNode subPropertyNode = rootNode.get(key);
                return subPropertyNode.asText();
            }
        } catch (JsonProcessingException e) {
            return null;
        }
        return null;
    }

    private void syncWithDittoThing(final Thing thing, final WoDTDigitalAdapterConfiguration configuration) {
        // PROPERTIES (Thing Attributes)
        thing.getAttributes().ifPresent(attributes -> { // - finished
            attributes.forEach((attribute) -> {
                if(attribute.getKey().toString().contains("rel-")) { // Relationships
                    ThingModelElement rel = this.dtdManager.getTMRelationships().stream()
                        .filter(r -> r.getElement().equals(attribute.getKey().toString()))
                        .findFirst()
                        .orElse(null);
                    if(rel != null) {
                        configuration.getOntology().obtainPropertyValueType(attribute.getKey().toString()).ifPresent(
                            relType -> {
                                configuration.getOntology().convertRelationship(
                                    attribute.getKey().toString(),
                                    relType
                                ).ifPresent(triple ->
                                    this.dtkgEngine.addRelationship(triple.getLeft(), triple.getRight())
                                );
                                this.dtdManager.addRelationship(attribute.getKey().toString());
                            }
                        );
                    }                    
                } else { // Attributes
                    ThingModelElement property = this.dtdManager.getTMProperties().stream()
                        .filter(p -> p.getElement().equals(attribute.getKey().toString()))
                        .findFirst()
                        .orElse(null);
                    if(property != null) {
                        configuration.getOntology().convertPropertyValue(
                            property.getElement(),
                            attribute.getValue().asString() // TO DO: edit
                        ).ifPresent(triple ->
                                this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                        );
                        this.dtdManager.addProperty(property.getElement());
                    }
                }
            });
        });

        // PROPERTIES, ACTIONS, EVENTS (from Thing Features)
        thing.getFeatures().ifPresent(features -> {
            features.forEach((featureName) -> {
                featureName.getProperties().ifPresent(properties -> { // - finished
                    // Feature Properties
                    properties.forEach((property) -> {
                        List<String> subProperties = extractSubPropertiesNames(property.getValue().toString()); // Check for subproperties
                        if(!subProperties.isEmpty()) {
                            subProperties.forEach(subProperty -> {                                
                                ThingModelElement featureProperty = this.dtdManager.getTMProperties().stream()
                                    .filter(p -> p.getElement().equals(property.getKey().toString() + "_" + subProperty))
                                    .filter(p -> p.getValue().get().equals(featureName.getId()))
                                    .findFirst()
                                    .orElse(null);
                                String fullPropertyName = featureProperty.getValue().get() + "_" + property.getKey() + "_" + subProperty;
                                configuration.getOntology().convertPropertyValue(
                                    fullPropertyName,
                                    extractSubPropertyValue(property.getValue().toString(), subProperty) // TO DO: edit
                                ).ifPresent(triple ->
                                    this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                                );
                                this.dtdManager.addProperty(fullPropertyName);
                            });
                        } else {
                            ThingModelElement featureProperty = this.dtdManager.getTMProperties().stream()
                                .filter(p -> p.getElement().equals(property.getKey().toString()))
                                .filter(p -> p.getValue().get().equals(featureName.getId()))
                                .findFirst()
                                .orElse(null);
                            String fullPropertyName = featureProperty.getValue().get() + "_" + property.getKey().toString();
                            configuration.getOntology().convertPropertyValue(
                                fullPropertyName,
                                property.getValue().toString() // TO DO: edit
                            ).ifPresent(triple ->
                                this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                            );
                            this.dtdManager.addProperty(fullPropertyName);
                        }
                    });

                    // Feature Actions
                    this.dtdManager.getTMActions().stream()
                        .filter(action -> action.getValue().isPresent() && action.getValue().get().equals(featureName.getId()))
                        .forEach(action -> {
                            String fullActionName = featureName.getId() + "_" + action.getElement();
                            this.dtdManager.addAction(fullActionName);
                            this.dtkgEngine.addActionId(fullActionName);
                        });

                    // Feature Events
                    this.dtdManager.getTMEvents().stream()
                        .filter(event -> event.getValue().isPresent() && event.getValue().get().equals(featureName.getId()))
                        .forEach(event -> {
                            String fullEventName = featureName.getId() + "_" + event.getElement();
                            this.dtdManager.addEvent(fullEventName, event.getAdditionalData().orElse(""));
                            //this.dtkgEngine.addEvent(fullEventName);
                        });
                });
            });
        });

        // ACTIONS (Thing Actions)
        this.dtdManager.getTMActions().stream()
            .filter(action -> action.getValue().isEmpty())
            .forEach(action -> {                
                this.dtdManager.addAction(action.getElement());
                this.dtkgEngine.addActionId(action.getElement());
            });

        // TO DO: EVENTS (Thing Events)
        this.dtdManager.getTMEvents().stream()
            .filter(event -> event.getValue().isEmpty())
            .forEach(event -> {
                this.dtdManager.addEvent(event.getElement(), event.getAdditionalData().orElse(""));
                //this.dtkgEngine.addEvent(event.getElement());
            });
    }

    public void stopAdapter() {
        this.platformManagementInterface.signalDigitalTwinDeletion();
        this.dittoClientThread.stopThread();
    }

    public void onThingChange(ThingChange change) {
        System.out.println(change); // TO DO: sostituisci con logging

        switch (change.getAction()) {
            case CREATED:
                if(change.getThing().get().getAttributes().isPresent()) { // Attributes creation - finished
                    change.getThing().get().getAttributes().get().forEach((attribute) -> {
                        if(attribute.getKey().toString().contains("rel-")) { // Relationship
                            ThingModelElement rel = this.dtdManager.getTMRelationships().stream()
                                .filter(r -> r.getElement().equals(attribute.getKey().toString()))
                                .findFirst()
                                .orElse(null);
                            if(rel != null) {
                                configuration.getOntology().obtainPropertyValueType(attribute.getKey().toString()).ifPresent(
                                    relType -> {
                                        configuration.getOntology().convertRelationship(
                                            attribute.getKey().toString(),
                                            relType
                                        ).ifPresent(triple ->
                                            this.dtkgEngine.addRelationship(triple.getLeft(), triple.getRight())
                                        );
                                        this.dtdManager.addRelationship(attribute.getKey().toString());
                                    }
                                );
                            }
                        } else { // Attribute
                            ThingModelElement prop = this.dtdManager.getTMProperties().stream()
                                .filter(p -> p.getElement().equals(attribute.getKey().toString()))
                                .findFirst()
                                .orElse(null);
                            if(prop != null) {
                                configuration.getOntology().convertPropertyValue(
                                    prop.getElement(),
                                    attribute.getValue().asString() // TO DO: edit
                                ).ifPresent(triple ->
                                        this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                                );
                                this.dtdManager.addProperty(prop.getElement());
                            }
                        }
                    });
                }
                if(change.getThing().get().getFeatures().isPresent()) { // Features creation (create all feature properties) - finished
                    change.getThing().get().getFeatures().get().forEach((featureName) -> {
                        featureName.getProperties().ifPresent(properties -> {
                            properties.forEach((property) -> {
                                List<String> subProperties = extractSubPropertiesNames(property.getValue().toString()); // Check for subproperties
                                if(!subProperties.isEmpty()) {
                                    subProperties.forEach(subProperty -> {
                                        ThingModelElement featureProp = this.dtdManager.getTMProperties().stream()
                                            .filter(p -> p.getElement().equals(property.getKey().toString() + "_" + subProperty))
                                            .filter(p -> p.getValue().get().equals(featureName.getId()))
                                            .findFirst()
                                            .orElse(null);
                                        String fullPropertyName = featureProp.getValue().get() + "_" + property.getKey() + "_" + subProperty;
                                        configuration.getOntology().convertPropertyValue(
                                            fullPropertyName,
                                            extractSubPropertyValue(property.getValue().toString(), subProperty) // TO DO: edit
                                        ).ifPresent(triple ->
                                            this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                                        );
                                        this.dtdManager.addProperty(fullPropertyName);
                                    });
                                } else {
                                    ThingModelElement featureProp = this.dtdManager.getTMProperties().stream()
                                        .filter(p -> p.getElement().equals(property.getKey().toString()))
                                        .filter(p -> p.getValue().get().equals(featureName.getId()))
                                        .findFirst()
                                        .orElse(null);
                                    String fullPropertyName = featureProp.getValue().get() + "_" + property.getKey().toString();
                                    configuration.getOntology().convertPropertyValue(
                                        fullPropertyName,
                                        property.getValue().toString() // TO DO: edit
                                    ).ifPresent(triple ->
                                        this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                                    );
                                    this.dtdManager.addProperty(fullPropertyName);
                                }
                            });
                        });

                        // TO DO: Add feature actions
                        this.dtdManager.getTMActions().stream()
                            .filter(action -> action.getValue().isPresent() && action.getValue().get().equals(featureName.getId()))
                            .forEach(action -> {
                                String fullActionName = featureName.getId() + "_" + action.getElement();
                                this.dtdManager.addAction(fullActionName);
                                this.dtkgEngine.addActionId(fullActionName);
                            });

                        // TO DO: Add feature events
                        this.dtdManager.getTMEvents().stream()
                            .filter(event -> event.getValue().isPresent() && event.getValue().get().equals(featureName.getId()))
                            .forEach(event -> {
                                String fullEventName = featureName.getId() + "_" + event.getElement();
                                this.dtdManager.addEvent(fullEventName, event.getAdditionalData().orElse(""));
                                //this.dtkgEngine.addEvent(fullEventName);
                            });
                    });
                }
                break;
            case DELETED:
                String elementToDelete = change.getPath().toString().split("/")[2];
                if(change.getPath().toString().contains("attributes")) { // Attributes deletion - finished
                    if(elementToDelete.contains("rel-")) { // Relationship
                        ThingModelElement rel = this.dtdManager.getTMRelationships().stream()
                            .filter(r -> r.getElement().equals(elementToDelete))
                            .findFirst()
                            .orElse(null);
                        if(rel != null) {
                            configuration.getOntology().obtainPropertyValueType(elementToDelete).ifPresent(
                                relType -> {
                                    configuration.getOntology().convertRelationship(
                                        elementToDelete,
                                        relType
                                    ).ifPresent(triple ->
                                        this.dtkgEngine.removeRelationship(triple.getLeft(), triple.getRight())
                                    );
                                    this.dtdManager.removeRelationship(elementToDelete);
                                }
                            );
                        }
                    } else { // Attribute
                        this.configuration
                            .getOntology()
                            .obtainProperty(elementToDelete)
                            .ifPresent(this.dtkgEngine::removeProperty);
                        this.dtdManager.removeProperty(elementToDelete);
                    }
                }
                if(change.getPath().toString().contains("features")) { // Entire feature deletion (all feature properties) - not finished
                    List<ThingModelElement> matchingProperties = this.dtdManager.getTMProperties().stream()
                        .filter(p -> p.getValue().isPresent() && p.getValue().get().equals(elementToDelete))
                        .collect(Collectors.toList());
                    matchingProperties.forEach(
                        prop -> {
                            String fullPorpertyName = prop.getValue().get() + "_" + prop.getElement();
                            this.configuration
                                .getOntology()
                                .obtainProperty(fullPorpertyName)
                                .ifPresent(this.dtkgEngine::removeProperty);
                            this.dtdManager.removeProperty(fullPorpertyName);
                        }
                    );

                    // TO DO: Remove feature actions
                    List<ThingModelElement> matchingActions = this.dtdManager.getTMActions().stream()
                        .filter(action -> action.getValue().isPresent() && action.getValue().get().equals(elementToDelete))
                        .collect(Collectors.toList());
                    matchingActions.forEach(
                        action -> {
                            String fullActionName = action.getValue().get() + "_" + action.getElement();
                            this.dtdManager.removeAction(fullActionName);
                            this.dtkgEngine.removeActionId(fullActionName);
                        }
                    );

                    // TO DO: Remove feature events
                    List<ThingModelElement> matchingEvents = this.dtdManager.getTMEvents().stream()
                        .filter(event -> event.getValue().isPresent() && event.getValue().get().equals(elementToDelete))
                        .collect(Collectors.toList());
                    matchingEvents.forEach(
                        event -> {
                            String fullEventName = event.getValue().get() + "_" + event.getElement();
                            this.dtdManager.removeEvent(fullEventName);
                            //this.dtkgEngine.removeEvent(fullEventName);
                        }
                    );
                }
                break;
            case UPDATED: // Aggiornamento valori attributi e features
                if (change.getThing().get().getAttributes().isPresent()) {  // Update Thing Attributes - finished
                    change.getThing().get().getAttributes().get().forEach((attribute) -> {
                        ThingModelElement prop = this.dtdManager.getTMProperties().stream()
                            .filter(p -> p.getElement().equals(attribute.getKey().toString()))
                            .findFirst()
                            .orElse(null);
                        configuration.getOntology().convertPropertyValue(
                            prop.getElement(),
                            attribute.getValue().asString() // TO DO: edit
                        ).ifPresent(triple ->
                                this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                        );
                        this.dtdManager.addProperty(prop.getElement());
                    });
                }
                if (change.getThing().get().getFeatures().isPresent()) {    // Update Thing Features (Properties) - finished
                    change.getThing().get().getFeatures().get().forEach((featureName) -> {
                        featureName.getProperties().ifPresent(properties -> {
                            properties.forEach((property) -> {
                                List<String> subProperties = extractSubPropertiesNames(property.getValue().toString()); // Check for subproperties
                                if(!subProperties.isEmpty()) {
                                    subProperties.forEach(subProperty -> {
                                        ThingModelElement featureProp = this.dtdManager.getTMProperties().stream()
                                            .filter(p -> p.getElement().equals(property.getKey().toString() + "_" + subProperty))
                                            .filter(p -> p.getValue().get().equals(featureName.getId()))
                                            .findFirst()
                                            .orElse(null);
                                        String fullPropertyName = featureProp.getValue().get() + "_" + property.getKey() + "_" + subProperty;
                                        configuration.getOntology().convertPropertyValue(
                                            fullPropertyName,
                                            extractSubPropertyValue(property.getValue().toString(), subProperty) // TO DO: edit
                                        ).ifPresent(triple ->
                                            this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                                        );
                                        this.dtdManager.addProperty(fullPropertyName);
                                    });
                                } else {
                                    ThingModelElement featureProp = this.dtdManager.getTMProperties().stream()
                                        .filter(p -> p.getElement().equals(property.getKey().toString()))
                                        .filter(p -> p.getValue().get().equals(featureName.getId()))
                                        .findFirst()
                                        .orElse(null);
                                    String fullPropertyName = featureProp.getValue().get() + "_" + property.getKey().toString();
                                    configuration.getOntology().convertPropertyValue(
                                        fullPropertyName,
                                        property.getValue().toString() // TO DO: edit
                                    ).ifPresent(triple ->
                                        this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                                    );
                                    this.dtdManager.addProperty(fullPropertyName);
                                }
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