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

import java.util.Optional;

/**
 * It models the concept of Individual in the context of Digital Twin Knowledge Graph.
 * So an individual here is another Digital Twin, identified by its URI.
 */
public final class Individual implements Resource {
    private final String individualUri;

    /**
     * Default constructor.
     * @param individualUri the uri of the individual.
     */
    public Individual(final String individualUri) {
        this.individualUri = individualUri;
    }

    @Override
    public Optional<String> getUri() {
        return Optional.of(this.individualUri);
    }
}
