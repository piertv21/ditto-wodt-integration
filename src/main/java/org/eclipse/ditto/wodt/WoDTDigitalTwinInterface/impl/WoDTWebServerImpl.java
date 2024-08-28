package org.eclipse.ditto.wodt.WoDTDigitalTwinInterface.impl;

/*
 * Copyright (c) 2023. Andrea Giulianelli
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

import org.eclipse.ditto.wodt.DTDManager.api.DTDManagerReader;
import org.eclipse.ditto.wodt.DTKGEngine.api.DTKGEngine;
import org.eclipse.ditto.wodt.PlatformManagementInterface.api.PlatformManagementInterfaceNotifier;
import org.eclipse.ditto.wodt.PlatformManagementInterface.impl.PlatformManagementInterfaceAPIControllerImpl;
import org.eclipse.ditto.wodt.WoDTDigitalTwinInterface.api.WoDTWebServer;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.JsonParseException;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

/**
 * This class implement the WoDT Web server that host the WoDT Digital Twin Interface component
* of the Abstract Architecture.
*/
public class WoDTWebServerImpl implements WoDTWebServer {
    private final int portNumber;
    private final WoDTDigitalTwinInterfaceControllerImpl wodtDigitalTwinInterfaceController;
    private final PlatformManagementInterfaceAPIControllerImpl platformManagementInterfaceAPIController;

    /**
     * Default constructor.
    * @param portNumber the port number where to expose the API
    * @param dtkgEngine the DTKGEngine
    * @param dtdManager the DTDManager
    * @param platformManagementInterfaceNotifier the Platform Management Interface Notifier component
    */
    public WoDTWebServerImpl(
            final int portNumber,
            final DTKGEngine dtkgEngine,
            final DTDManagerReader dtdManager,
            final PlatformManagementInterfaceNotifier platformManagementInterfaceNotifier
            ) {
        this.portNumber = portNumber;
        this.wodtDigitalTwinInterfaceController = new WoDTDigitalTwinInterfaceControllerImpl(
                dtkgEngine, dtdManager);
        dtkgEngine.addDTKGObserver(this.wodtDigitalTwinInterfaceController::notifyNewDTKG);
        this.platformManagementInterfaceAPIController = new PlatformManagementInterfaceAPIControllerImpl(
                platformManagementInterfaceNotifier
        );
    }

    @Override
    public void start() {
        final Javalin app = Javalin.create().start(this.portNumber);
        app.exception(JsonMappingException.class, (e, context) -> context.status(HttpStatus.BAD_REQUEST));
        app.exception(JsonParseException.class, (e, context) -> context.status(HttpStatus.BAD_REQUEST));
        this.wodtDigitalTwinInterfaceController.registerRoutes(app);
        this.platformManagementInterfaceAPIController.registerRoutes(app);
    }
}