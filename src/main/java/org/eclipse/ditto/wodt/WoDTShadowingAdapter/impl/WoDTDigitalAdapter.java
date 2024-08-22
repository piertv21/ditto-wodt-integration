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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.ditto.wodt.DTDManager.api.DTDManager;
import org.eclipse.ditto.wodt.DTDManager.impl.WoTDTDManager;
import org.eclipse.ditto.wodt.DTKGEngine.api.DTKGEngine;
import org.eclipse.ditto.wodt.DTKGEngine.impl.JenaDTKGEngine;
import org.eclipse.ditto.wodt.PlatformManagementInterface.api.PlatformManagementInterface;
import org.eclipse.ditto.wodt.PlatformManagementInterface.impl.BasePlatformManagementInterface;
import org.eclipse.ditto.wodt.WoDTDigitalTwinInterface.api.WoDTWebServer;
import org.eclipse.ditto.wodt.WoDTDigitalTwinInterface.impl.WoDTWebServerImpl;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;

/**
 * This class represents the Eclipse Ditto Adapter that allows to implement the WoDT Digital Twin layer
* implementing the components of the Abstract Architecture.
*/
public final class WoDTDigitalAdapter {
    private final DTKGEngine dtkgEngine;
    private final DTDManager dtdManager;
    private final WoDTWebServer woDTWebServer;
    private final PlatformManagementInterface platformManagementInterface;
    private final String digitalTwinId;

    /**
     * Default constructor.
    * @param digitalAdapterId the id of the Digital Adapter
    * @param configuration the configuration of the Digital Adapter
    */
    public WoDTDigitalAdapter(
        final String digitalAdapterId,
        final WoDTDigitalAdapterConfiguration configuration,
        final String digitalTwinId
    ) {
        this.digitalTwinId = digitalTwinId;
        this.platformManagementInterface = new BasePlatformManagementInterface(
                configuration.getDigitalTwinUri());
        this.dtkgEngine = new JenaDTKGEngine(configuration.getDigitalTwinUri());
        this.dtdManager = new WoTDTDManager(
                configuration.getDigitalTwinUri(),
                configuration.getOntology(),
                configuration.getPhysicalAssetId(),
                configuration.getPortNumber(),
                this.platformManagementInterface
        );
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
    }
}