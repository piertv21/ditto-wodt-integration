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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Presenter class to be able to deserialize platform registration data from the API.
* It contains the self field where the WoDT Digital Twins Platform send its URL.
*/
public final class PlatformRegistration {
    private final String self;

    /**
     * Default constructor.
    * @param self the field where the WoDT Platform send its URL
    */
    @JsonCreator
    public PlatformRegistration(@JsonProperty("self") final String self) {
        this.self = self;
    }

    /**
     * Obtain the url of the Platform that added the DT.
    * @return the Platform url
    */
    public String getSelf() {
        return this.self;
    }
}