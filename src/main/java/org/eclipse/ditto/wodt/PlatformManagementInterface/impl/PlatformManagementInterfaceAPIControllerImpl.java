package org.eclipse.ditto.wodt.PlatformManagementInterface.impl;

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

import io.github.webbasedwodt.application.component.PlatformManagementInterfaceAPIController;
import io.github.webbasedwodt.application.component.PlatformManagementInterfaceNotifier;
import io.github.webbasedwodt.application.presenter.api.PlatformRegistration;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.net.URI;

/**
 * Implementation of the controller for the Platform Management Interface API.
*/
final class PlatformManagementInterfaceAPIControllerImpl implements PlatformManagementInterfaceAPIController {
    private final PlatformManagementInterfaceNotifier platformManagementInterfaceNotifier;

    /**
     * Default constructor.
    * @param platformManagementInterfaceNotifier the platform management interface notifier that handle registrations
    */
    PlatformManagementInterfaceAPIControllerImpl(
            final PlatformManagementInterfaceNotifier platformManagementInterfaceNotifier
    ) {
        this.platformManagementInterfaceNotifier = platformManagementInterfaceNotifier;
    }


    @Override
    public void routeNewRegistration(final Context context) {
        try {
            final PlatformRegistration platformRegistration = context.bodyAsClass(PlatformRegistration.class);
            if (platformRegistration.getSelf() != null
                && this.platformManagementInterfaceNotifier.notifyNewRegistration(
                        URI.create(platformRegistration.getSelf()))
            ) {
                context.status(HttpStatus.OK);
            } else {
                context.status(HttpStatus.BAD_REQUEST);
            }
        } catch (final IllegalArgumentException exception) {
            context.status(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void registerRoutes(final Javalin app) {
        app.post("/platform", this::routeNewRegistration);
    }
}