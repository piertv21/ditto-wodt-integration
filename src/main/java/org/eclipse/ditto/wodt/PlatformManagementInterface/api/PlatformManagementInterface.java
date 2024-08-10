package org.eclipse.ditto.wodt.PlatformManagementInterface.api;

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

import java.net.URI;

/**
 * This interface represent the PlatformManagementInterface component in the Abstract Architecture.
*/
public interface PlatformManagementInterface extends
        PlatformManagementInterfaceReader, PlatformManagementInterfaceNotifier {
    /**
     * This method allows the component to send the registration request to the passed WoDT Digital Twins Platform.
    * @param platformUrl the platformUrl to which register
    * @param currentDtd the current Digital Twin Descriptor
    * @return true if correctly registered, false instead
    */
    boolean registerToPlatform(URI platformUrl, String currentDtd);

    /**
     * Signal to the Platform Management Interface the deletion of the managed Digital Twin.
    * This will result in the deletion notification to be sent to all the WoDT Digital Twins Platform to
    * which it is registered.
    */
    void signalDigitalTwinDeletion();
}