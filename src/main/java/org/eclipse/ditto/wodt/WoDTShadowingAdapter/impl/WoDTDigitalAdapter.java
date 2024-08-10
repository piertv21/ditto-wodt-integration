package org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl;

/*
 * Copyright (c) 2024. Andrea Giulianelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.github.webbasedwodt.application.component.DTDManager;
import io.github.webbasedwodt.application.component.DTKGEngine;
import io.github.webbasedwodt.application.component.PlatformManagementInterface;
import io.github.webbasedwodt.application.component.WoDTWebServer;
import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.core.state.DigitalTwinStateResource;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtDigitalTwinStateActionException;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This class represents the WLDT Framework Digital Adapter that allows to implement the WoDT Digital Twin layer
* implementing the components of the Abstract Architecture.
*/
public final class WoDTDigitalAdapter extends DigitalAdapter<WoDTDigitalAdapterConfiguration> {
    private final DTKGEngine dtkgEngine;
    private final DTDManager dtdManager;
    private final WoDTWebServer woDTWebServer;
    private final PlatformManagementInterface platformManagementInterface;

    /**
     * Default constructor.
    * @param digitalAdapterId the id of the Digital Adapter
    * @param configuration the configuration of the Digital Adapter
    */
    public WoDTDigitalAdapter(final String digitalAdapterId, final WoDTDigitalAdapterConfiguration configuration) {
        super(digitalAdapterId, configuration);
        this.platformManagementInterface = new BasePlatformManagementInterface(
                this.getConfiguration().getDigitalTwinUri());
        this.dtkgEngine = new JenaDTKGEngine(this.getConfiguration().getDigitalTwinUri());
        this.dtdManager = new WoTDTDManager(
                this.getConfiguration().getDigitalTwinUri(),
                this.getConfiguration().getOntology(),
                this.getConfiguration().getPhysicalAssetId(),
                this.getConfiguration().getPortNumber(),
                this.platformManagementInterface
        );
        this.woDTWebServer = new WoDTWebServerImpl(
                this.getConfiguration().getPortNumber(),
                this.dtkgEngine,
                this.dtdManager,
                (actionName, body) -> {
                    try {
                        publishDigitalActionWldtEvent(actionName, body);
                        return true;
                    } catch (EventBusException e) {
                        Logger.getLogger(WoDTDigitalAdapter.class.getName())
                                .info("Impossible to forward action: " + e);
                        return false;
                    }
                },
                this.platformManagementInterface
        );
    }

    @Override
    protected void onEventNotificationReceived(
            final DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) { }

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
                this.getConfiguration()
                    .getOntology()
                    .obtainProperty(updatedProperty.getKey())
                    .ifPresent(this.dtkgEngine::removeProperty);
                this.dtdManager.removeProperty(updatedProperty.getKey());
                break;
            case OPERATION_UPDATE:
            case OPERATION_UPDATE_VALUE:
                this.getConfiguration().getOntology().convertPropertyValue(
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
                this.getConfiguration().getOntology().convertRelationship(
                    updatedRelationshipInstance.getRelationshipName(),
                    updatedRelationshipInstance.getTargetId().toString()
                ).ifPresent(triple ->
                    this.dtkgEngine.addRelationship(triple.getLeft(), triple.getRight())
                );
                break;
            case OPERATION_REMOVE:
                this.getConfiguration().getOntology().convertRelationship(
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
        this.getConfiguration().getPlatformToRegister().forEach(platform ->
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
                        this.getConfiguration().getOntology().convertPropertyValue(
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
    }

    @Override
    public void onDigitalTwinUnSync(final DigitalTwinState digitalTwinState) { }

    @Override
    public void onDigitalTwinCreate() { }

    @Override
    public void onDigitalTwinStart() { }

    @Override
    public void onDigitalTwinStop() { }

    @Override
    public void onDigitalTwinDestroy() { }
}