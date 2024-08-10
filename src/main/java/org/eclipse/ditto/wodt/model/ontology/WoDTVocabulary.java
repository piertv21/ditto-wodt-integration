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

package io.github.webbasedwodt.model.ontology;

/**
 * This enum contains the needed elements of the WoDT vocabulary.
 */
public enum WoDTVocabulary {

    /**
     * Base uri of the vocabulary.
     */
    BASE_URI("https://purl.org/wodt/"),

    /**
     * Current status predicate.
     */
    CURRENT_STATUS(BASE_URI.uri + "currentStatus"),

    /**
     * Domain predicate predicate.
     */
    DOMAIN_PREDICATE(BASE_URI.uri + "domainPredicate"),

    /**
     * Has descriptor predicate.
     */
    HAS_DESCRIPTOR(BASE_URI.uri + "hasDescriptor"),

    /**
     * Registered to platform predicate.
     */
    REGISTERED_TO_PLATFORM(BASE_URI.uri + "registeredToPlatform"),

    /**
     * Physical asset id predicate.
     */
    PHYSICAL_ASSET_ID(BASE_URI.uri + "physicalAssetId"),

    /**
     * Augmented interaction predicate.
     */
    AUGMENTED_INTERACTION(BASE_URI.uri + "augmentedInteraction"),

    /**
     * Available action id predicate.
     */
    AVAILABLE_ACTION_ID(BASE_URI.uri + "availableActionId"),

    /**
     * Version predicate.
     */
    VERSION(BASE_URI.uri + "version");

    private final String uri;

    /**
     * Default constructor.
     * @param uri the uri of the vocabulary item.
     */
    WoDTVocabulary(final String uri) {
        this.uri = uri;
    }

    /**
     * Get the URI of the vocabulary item.
     * @return the URI.
     */
    public String getUri() {
        return this.uri;
    }


    @Override
    public String toString() {
        return this.getUri();
    }
}
