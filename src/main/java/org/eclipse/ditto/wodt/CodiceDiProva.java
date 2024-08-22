package org.eclipse.ditto.wodt;

import java.util.logging.Logger;

import org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;

public class CodiceDiProva {
    
    // (EVENTI) SUBSCRIBE PER RICEZIONE NUOVI MESSAGGI (DENTRO TRY CATCH) USARE STESSO SUBJECT PER RISPONDERE
    /*client.live().startConsumption().toCompletableFuture().get(); // this will block the thread! work asynchronously whenever possible!
    System.out.println("Subscribed for live messages/commands/events");

    client.live().registerForMessage("globalMessageHandler", "hello.world", message -> {
        System.out.println("Received Message with subject " +  message.getSubject());
        message.reply()
            .httpStatus(HttpStatus.IM_A_TEAPOT)
            .payload("Hello, I'm just a Teapot!")
            .send();
        });
    
    latch.await();*/
    
    // (AZIONI) INVIO MESSAGGI AD UNA THING
    /*client.live().forId("org.eclipse.ditto:new-thing")
        .message()
        .from()
        .subject("hello.world")
        .payload("I am a Teapot")
        .send(String.class, (response, throwable) ->
            System.out.println("Got response: " + response.getPayload().orElse(null))
        );*/



        /*// ottiene tutti gli attributi di una thing
        thing.getAttributes().ifPresent(attributes -> {
            attributes.forEach((attribute) -> {
                System.out.println(attribute.getKey() + ": " + attribute.getValue().toString());
            });
        });

        // ottiene tutte le proprietà di tutte le features di una thing
        thing.getFeatures().ifPresent(features -> {
            features.forEach((featureName) -> {
                System.out.println("Feature: " + featureName.getId());
                featureName.getProperties().ifPresent(properties -> {
                    properties.forEach((property) -> {
                        System.out.println(property.getKey() + ": " + property.getValue().toString());
                    });
                });
                System.out.println("\n");
            });
        });
        
        // registra cambiamenti
        client.twin().forId(ThingId.of("io.eclipseprojects.ditto:bulb-holder")).registerForThingChanges("my-changes", change -> {
            System.out.println("Change received: " + change);
        });
        
        // accesso a attributi e proprietà di features
        System.out.println(
            // get attributo
            //thing.getAttributes().get().getField("manufacturer").get().getValue().asString()

            // get proprietà featureName
            //thing.getFeatures().get().getFeature("Bulb").get().getProperties().get().getField("on").get().getValue().toString()            
        );*/


    /*
    //SHADOWING ADAPTER
    @Override
    protected void onStateUpdate(
            final DigitalTwinState newDigitalTwinState,
            final DigitalTwinState previousDigitalTwinState,
            final ArrayList<DigitalTwinStateChange> digitalTwinStateChanges
    ) {
        if (digitalTwinStateChanges != null && !digitalTwinStateChanges.isEmpty()) {
            for (final DigitalTwinStateChange change : digitalTwinStateChanges) {
                final DigitalTwinStateChange.Operation operationPerformed = change.getOperation();
                final DigitalTwinStateChange.ResourceType changeResourceType = change.getResourceType();
                final DigitalTwinStateResource changedResource = change.getResource();

                switch (changeResourceType) {
                    case PROPERTY:
                    case PROPERTY_VALUE:
                        if (changedResource instanceof DigitalTwinStateProperty<?>) {
                            this.handlePropertyUpdate((DigitalTwinStateProperty<?>) changedResource, operationPerformed);
                        }
                        break;
                    case RELATIONSHIP:
                        if (changedResource instanceof DigitalTwinStateRelationship<?>) {
                            this.handleRelationshipUpdate(
                                    (DigitalTwinStateRelationship<?>) changedResource, operationPerformed);
                        }
                        break;
                    case RELATIONSHIP_INSTANCE:
                        if (changedResource instanceof DigitalTwinStateRelationshipInstance<?>) {
                            this.handleRelationshipInstanceUpdate(
                                    (DigitalTwinStateRelationshipInstance<?>) changedResource, operationPerformed);
                        }
                        break;
                    case ACTION:
                        if (changedResource instanceof DigitalTwinStateAction) {
                            this.handleActionUpdate((DigitalTwinStateAction) changedResource, operationPerformed);
                        }
                        break;
                    case EVENT:
                        Logger.getLogger(WoDTDigitalAdapter.class.getName()).info("Events are not currently supported");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void handlePropertyUpdate(
            final DigitalTwinStateProperty<?> updatedProperty,
            final DigitalTwinStateChange.Operation operationPerformed
    ) {
        switch (operationPerformed) {
            case OPERATION_ADD:
                this.dtdManager.addProperty(updatedProperty.getKey());
                break;
            case OPERATION_REMOVE:
                configuration
                    .getOntology()
                    .obtainProperty(updatedProperty.getKey())
                    .ifPresent(this.dtkgEngine::removeProperty);
                this.dtdManager.removeProperty(updatedProperty.getKey());
                break;
            case OPERATION_UPDATE:
            case OPERATION_UPDATE_VALUE:
                configuration.getOntology().convertPropertyValue(
                    updatedProperty.getKey(),
                    updatedProperty.getValue()
                ).ifPresent(triple ->
                    this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                );
                break;
            default:
                break;
        }
    }

    private void handleRelationshipUpdate(
            final DigitalTwinStateRelationship<?> updatedRelationship,
            final DigitalTwinStateChange.Operation operationPerformed
    ) {
        switch (operationPerformed) {
            case OPERATION_ADD:
                this.dtdManager.addRelationship(updatedRelationship.getName());
                break;
            case OPERATION_REMOVE:
                this.dtdManager.removeRelationship(updatedRelationship.getName());
                break;
            default:
                break;
        }
    }

    private void handleRelationshipInstanceUpdate(
            final DigitalTwinStateRelationshipInstance<?> updatedRelationshipInstance,
            final DigitalTwinStateChange.Operation operationPerformed
    ) {
        switch (operationPerformed) {
            case OPERATION_ADD:
                configuration.getOntology().convertRelationship(
                    updatedRelationshipInstance.getRelationshipName(),
                    updatedRelationshipInstance.getTargetId().toString()
                ).ifPresent(triple ->
                    this.dtkgEngine.addRelationship(triple.getLeft(), triple.getRight())
                );
                break;
            case OPERATION_REMOVE:
                configuration.getOntology().convertRelationship(
                    updatedRelationshipInstance.getRelationshipName(),
                    updatedRelationshipInstance.getTargetId().toString()
                ).ifPresent(triple ->
                    this.dtkgEngine.removeRelationship(triple.getLeft(), triple.getRight())
                );
                break;
            default:
                break;
        }
    }

    private void handleActionUpdate(
            final DigitalTwinStateAction updatedAction,
            final DigitalTwinStateChange.Operation operationPerformed
    ) {
        switch (operationPerformed) {
            case OPERATION_ADD: // adds and enables the action
                this.dtdManager.addAction(updatedAction.getKey());
                this.dtkgEngine.addActionId(updatedAction.getKey());
                break;
            case OPERATION_REMOVE: // only disables the action
                this.dtkgEngine.removeActionId(updatedAction.getKey());
                break;
            case OPERATION_UPDATE: // enables the action
                this.dtkgEngine.addActionId(updatedAction.getKey());
                break;
            default:
                break;
        }
    }

    @Override
    public void onAdapterStart() {
        this.woDTWebServer.start();
        configuration.getPlatformToRegister().forEach(platform ->
                this.platformManagementInterface.registerToPlatform(platform, this.dtdManager.getDTD().toJson()));
    }

    @Override
    public void onAdapterStop() {
        this.platformManagementInterface.signalDigitalTwinDeletion();
    }

    @Override
    public void onDigitalTwinSync(final DigitalTwinState digitalTwinState) {
        try {
            digitalTwinState.getPropertyList().ifPresent(properties ->
                    properties.forEach(property -> {
                        configuration.getOntology().convertPropertyValue(
                                property.getKey(),
                                property.getValue()
                        ).ifPresent(triple ->
                                this.dtkgEngine.addDigitalTwinPropertyUpdate(triple.getLeft(), triple.getRight())
                        );
                        this.dtdManager.addProperty(property.getKey());
                    }));
            digitalTwinState.getRelationshipList().ifPresent(relationships ->
                    relationships.forEach(relationship -> this.dtdManager.addRelationship(relationship.getName())));
            digitalTwinState.getActionList().ifPresent(actions ->
                    actions.forEach(action -> {
                        this.dtdManager.addAction(action.getKey());
                        this.dtkgEngine.addActionId(action.getKey());
                    }));
        } catch (WldtDigitalTwinStatePropertyException | WldtDigitalTwinStateActionException e) {
            Logger.getLogger(WoDTDigitalAdapter.class.getName()).info("Error during loading: " + e);
        }
    }*/
    
}