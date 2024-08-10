package org.eclipse.ditto.wodt.PlatformManagementInterface.api;

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

import java.net.URI;

/**
 * This interface models the notifier part of the Platform Management Interface that get notified when a
* Platform has added the DT to its ecosystem.
*/
public interface PlatformManagementInterfaceNotifier {
    /**
     * Notify the registration to a new Platform.
    * @param platformUrl the url of the platform that has added the DT.
    * @return true if the DT platform was not already registered, false instead.
    */
    boolean notifyNewRegistration(URI platformUrl);
}