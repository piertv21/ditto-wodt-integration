package org.eclipse.ditto.wodt.WoDTDigitalTwinInterface.api;

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

import io.javalin.http.Context;
import io.javalin.websocket.WsConfig;

/**
 * This interface represent the WoDT Digital Twins Interface controller.
*/
public interface WoDTDigitalTwinInterfaceController extends WebServerController {
    /**
     * Get Digital Twin controller.
    * @param context the javalin context
    */
    void routeGetDigitalTwin(Context context);

    /**
     * Get Digital Twin Knowledge Graph controller.
    * @param context the javalin context
    */
    void routeGetDigitalTwinKnowledgeGraph(Context context);

    /**
     * Get Digital Twin Knowledge Graph controller.
    * @param wsContext the javalin context
    */
    void routeGetDigitalTwinKnowledgeGraphEvents(WsConfig wsContext);

    /**
     * Get Digital Twin Descriptor controller.
    * @param context the javalin context
    */
    void routeGetDigitalTwinDescriptor(Context context);

    /**
     * Handle an action invocation.
    * @param context the javalin context.
    */
    void routeHandleActionInvocation(Context context);

    /**
     * Notify the presence of a new Digital Twin Knowledge Graph.
    * @param newDtkg the new DTKG.
    */
    void notifyNewDTKG(String newDtkg);
}