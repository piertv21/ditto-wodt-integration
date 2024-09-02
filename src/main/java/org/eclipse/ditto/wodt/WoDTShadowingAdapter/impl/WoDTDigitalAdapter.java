package org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl;

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
import static org.eclipse.ditto.wodt.common.ThingUtils.convertStringToType;
import static org.eclipse.ditto.wodt.common.ThingUtils.extractSubPropertiesNames;
import static org.eclipse.ditto.wodt.common.ThingUtils.extractSubPropertyValue;

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
        this.syncWithDittoThing(thing);
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
        /*this.configuration.getPlatformToRegister().forEach(platform ->
                this.platformManagementInterface.registerToPlatform(platform, this.dtdManager.getDTD().toJson()));*/
        dittoClientThread.start();
    }

    public void stopAdapter() {
        this.platformManagementInterface.signalDigitalTwinDeletion();
        this.dittoClientThread.stopThread();
    }

    private void handleRelationship(String key, boolean isDeletion) {
        configuration.getOntology().obtainPropertyValueType(key).ifPresent(
                relType -> {
                    configuration.getOntology().convertRelationship(key, relType).ifPresent(triple -> {
                        if (isDeletion) {
                            this.dtkgEngine.removeRelationship(triple.getLeft(), triple.getRight());
                            this.dtdManager.removeRelationship(key);
                        } else {
                            this.dtkgEngine.addRelationship(triple.getLeft(), triple.getRight());
                            this.dtdManager.addRelationship(key);
                        }
                    });
                }
        );
    }
    
    private void handleProperty(String key, String value, boolean isFeatureProperty, boolean isDeletion, String featureId) {
        String fullPropertyName = (isFeatureProperty ? featureId + "_" : "") + key;
        configuration.getOntology().convertPropertyValue(fullPropertyName, convertStringToType(value)).ifPresent(triple -> {
            if (isDeletion) {
                this.dtkgEngine.removeProperty(triple.getLeft());
                this.dtdManager.removeProperty(fullPropertyName);
            } else {
                this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight());
                this.dtdManager.addProperty(fullPropertyName);
            }
        });
    }
    
    private void handleAction(String actionId, boolean isDeletion, String featureId) {
        String fullActionName = (featureId != null ? featureId + "_" : "") + actionId;
        if (isDeletion) {
            this.dtdManager.removeAction(fullActionName);
            this.dtkgEngine.removeActionId(fullActionName);
        } else {
            this.dtdManager.addAction(fullActionName);
            this.dtkgEngine.addActionId(fullActionName);
        }
    }
    
    private void handleEvent(String eventId, boolean isDeletion, String featureId) {
        String fullEventName = (featureId != null ? featureId + "_" : "") + eventId;
        if (isDeletion) {
            this.dtdManager.removeEvent(fullEventName);
        } else {
            this.dtdManager.addEvent(fullEventName);
        }
    }

    private void syncWithDittoThing(final Thing thing) {
        // Thing Attributes (Relationships and Properties)
        thing.getAttributes().ifPresent(attributes -> {
            attributes.forEach((attribute) -> {
                if (attribute.getKey().toString().contains("rel-")) {
                    handleRelationship(attribute.getKey().toString(), false);
                } else {
                    handleProperty(attribute.getKey().toString(), attribute.getValue().toString(), false, false, null);
                }
            });
        });

        // Thing Features (Properties, Actions, Events)
        thing.getFeatures().ifPresent(features -> {
            features.forEach((feature) -> {
                feature.getProperties().ifPresent(properties -> {
                    properties.forEach((property) -> {
                        List<String> subProperties = extractSubPropertiesNames(property.getValue().toString());
                        if (!subProperties.isEmpty()) {
                            subProperties.forEach(subProperty -> {
                                String fullKey = property.getKey().toString() + "_" + subProperty;
                                handleProperty(fullKey, extractSubPropertyValue(property.getValue().toString(), subProperty), true, false, feature.getId());
                            });
                        } else {
                            handleProperty(property.getKey().toString(), property.getValue().toString(), true, false, feature.getId());
                        }
                    });
                });

                this.dtdManager.getTMActions().stream()
                        .filter(action -> action.getValue().isPresent() && action.getValue().get().equals(feature.getId()))
                        .forEach(action -> handleAction(action.getElement(), false, feature.getId()));

                this.dtdManager.getTMEvents().stream()
                        .filter(event -> event.getValue().isPresent() && event.getValue().get().equals(feature.getId()))
                        .forEach(event -> handleEvent(event.getElement(), false, feature.getId()));
            });
        });

        // Thing Actions
        this.dtdManager.getTMActions().stream()
                .filter(action -> action.getValue().isEmpty())
                .forEach(action -> handleAction(action.getElement(), false, null));

        // Thing Events
        this.dtdManager.getTMEvents().stream()
                .filter(event -> event.getValue().isEmpty())
                .forEach(event -> handleEvent(event.getElement(), false, null));
    }

    public void onThingChange(ThingChange change) {
        switch (change.getAction()) {
            case CREATED:
            case UPDATED:
                if (change.getThing().get().getAttributes().isPresent()) {
                    change.getThing().get().getAttributes().get().forEach((attribute) -> {
                        if (attribute.getKey().toString().contains("rel-")) {
                            handleRelationship(attribute.getKey().toString(), false);
                        } else {
                            handleProperty(attribute.getKey().toString(), attribute.getValue().toString(), false, false, null);
                        }
                    });
                }
                if (change.getThing().get().getFeatures().isPresent()) {
                    change.getThing().get().getFeatures().get().forEach((feature) -> {
                        feature.getProperties().ifPresent(properties -> {
                            properties.forEach((property) -> {
                                List<String> subProperties = extractSubPropertiesNames(property.getValue().toString());
                                if (!subProperties.isEmpty()) {
                                    subProperties.forEach(subProperty -> {
                                        String fullKey = property.getKey().toString() + "_" + subProperty;
                                        handleProperty(fullKey, extractSubPropertyValue(property.getValue().toString(), subProperty), true, false, feature.getId());
                                    });
                                } else {
                                    handleProperty(property.getKey().toString(), property.getValue().toString(), true, false, feature.getId());
                                }
                            });
                        });
                        this.dtdManager.getTMActions().stream()
                                .filter(action -> action.getValue().isPresent() && action.getValue().get().equals(feature.getId()))
                                .forEach(action -> handleAction(action.getElement(), false, feature.getId()));

                        this.dtdManager.getTMEvents().stream()
                                .filter(event -> event.getValue().isPresent() && event.getValue().get().equals(feature.getId()))
                                .forEach(event -> handleEvent(event.getElement(), false, feature.getId()));
                    });
                }
                break;
            case DELETED:
                String elementToDelete = change.getPath().toString().split("/")[2];
                if (change.getPath().toString().contains("attributes")) {
                    if (elementToDelete.contains("rel-")) {
                        handleRelationship(elementToDelete, true);
                    } else {
                        handleProperty(elementToDelete, null, false, true, null);
                    }
                }
                if (change.getPath().toString().contains("features")) {
                    List<ThingModelElement> matchingProperties = this.dtdManager.getTMProperties().stream()
                            .filter(p -> p.getValue().isPresent() && p.getValue().get().equals(elementToDelete))
                            .collect(Collectors.toList());
                    matchingProperties.forEach(prop -> handleProperty(prop.getElement(), null, true, true, elementToDelete));

                    this.dtdManager.getTMActions().stream()
                            .filter(action -> action.getValue().isPresent() && action.getValue().get().equals(elementToDelete))
                            .forEach(action -> handleAction(action.getElement(), true, elementToDelete));

                    this.dtdManager.getTMEvents().stream()
                            .filter(event -> event.getValue().isPresent() && event.getValue().get().equals(elementToDelete))
                            .forEach(event -> handleEvent(event.getElement(), true, elementToDelete));
                }
                break;
            default:
                break;
        }
    }
}