package org.eclipse.ditto.wodt.WoDTShadowingAdapter.api;

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

import io.github.webbasedwodt.model.ontology.DTOntology;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for the {@link WoDTDigitalAdapter}.
*/
public final class WoDTDigitalAdapterConfiguration {
    private final DTOntology ontology;
    private final String digitalTwinUri;
    private final int portNumber;
    private final String physicalAssetId;
    private final Set<URI> platformToRegister;

    /**
     * Default constructor.
    * @param digitalTwinUri the uri of the WoDT Digital Twin
    * @param ontology the ontology to use for the semantics
    * @param portNumber the port number where to expose services
    * @param physicalAssetId the id of the associated physical asset
    * @param platformToRegister the platforms to which register
    */
    public WoDTDigitalAdapterConfiguration(
            final String digitalTwinUri,
            final DTOntology ontology,
            final int portNumber,
            final String physicalAssetId,
            final Set<URI> platformToRegister) {
        this.digitalTwinUri = digitalTwinUri;
        this.ontology = ontology;
        this.portNumber = portNumber;
        this.physicalAssetId = physicalAssetId;
        this.platformToRegister = new HashSet<>(platformToRegister);
    }

    /**
     * Obtain the WoDT Digital Twin URI.
    * @return the URI.
    */
    public String getDigitalTwinUri() {
        return this.digitalTwinUri;
    }

    /**
     * Obtain the ontology to describe the Digital Twin data.
    * @return the ontology.
    */
    public DTOntology getOntology() {
        return this.ontology;
    }

    /**
     * Obtain the port number where to expose services.
    * @return the port number
    */
    public int getPortNumber() {
        return this.portNumber;
    }

    /**
     * Obtain the associated physical asset id.
    * @return the id of the associated physical asset
    */
    public String getPhysicalAssetId() {
        return this.physicalAssetId;
    }

    /**
     * Obtain the platform to which register.
    * @return the platforms urls.
    */
    public Set<URI> getPlatformToRegister() {
        return new HashSet<>(this.platformToRegister);
    }
}