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

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * It models the concept of RDF Blank Node in the context of Digital Twin Knowledge Graph.
 * A Blank Node could have an associated list of predicates.
 */
public final class BlankNode implements Resource {
    private final List<Pair<Property, Node>> predicates;

    /**
     * Default constructor.
     * It creates a Blank Node without any predicates.
     */
    public BlankNode() {
        this(new ArrayList<>());
    }

    /**
     * Constructor that allows you to configure the Blank Node with existing predicates.
     * @param predicates the predicates to add
     */
    public BlankNode(final List<Pair<Property, Node>> predicates) {
        this.predicates = new ArrayList<>(predicates);
    }

    /**
     * Add a predicate to the BlankNode.
     * Note that this is an immutable data structure, so it returns a new [BlankNode].
     * @param predicate the predicate to add
     * @return the modified version of the Blank Node
     */
    public BlankNode addPredicate(final Pair<Property, Node> predicate) {
        final List<Pair<Property, Node>> resultingPredicates = new ArrayList<>(this.predicates);
        resultingPredicates.add(predicate);
        return new BlankNode(resultingPredicates);
    }

    /**
     * Get the predicates inside the Blank node.
     * @return the list of predicates
     */
    public List<Pair<Property, Node>> getPredicates() {
        return new ArrayList<>(this.predicates);
    }

    @Override
    public Optional<String> getUri() {
        return Optional.empty();
    }
}
