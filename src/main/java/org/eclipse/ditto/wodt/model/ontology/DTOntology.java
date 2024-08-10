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

import java.util.Optional;

/**
 * It models the ontology followed by the Digital Twin.
 * This will be used to convert raw data to semantic data, following the domain ontology.
 * This interface is the one that DT Developer must implement.
 */
public interface DTOntology {
    /**
     * This represents the type of the Digital Twin.
     * @return the type of the Digital Twin
     */
    String getDigitalTwinType();

    /**
     * Obtain the ontology property from the rawProperty in input.
     * If the mapping cannot be done it will return an empty optional.
     * It is valid both for dt properties and dt relationships.
     * @param rawProperty the input raw property
     * @return the Property instance
     */
    Optional<Property> obtainProperty(String rawProperty);

    /**
     * Obtain the semantic type of the value for a rawProperty.
     * It is valid both for dt properties and dt relationships.
     * @param rawProperty the input raw property
     * @return an optional for the type of the value of the property
     */
    Optional<String> obtainPropertyValueType(String rawProperty);

    /**
     * Convert a raw property and its value to the ontology model.
     * If the mapping cannot be done it will return an empty optional
     * @param rawProperty the input raw property
     * @param value the value of the property
     * @return an optional that is filled with the Pair of the mapped Property and its mapped value if possible
     * @param <T> the type of the value
     */
    <T> Optional<Pair<Property, Node>> convertPropertyValue(String rawProperty, T value);

    /**
     * Convert a raw relationship and its target uri to the ontology model.
     * If the mapping cannot be done it will return an empty optional.
     * @param rawRelationship the input raw relationship
     * @param targetUri the target uri of the relationship
     * @return an optional that is filled with the Pair of the mapped Property and its mapped target uri if possible
     */
    Optional<Pair<Property, Individual>> convertRelationship(String rawRelationship, String targetUri);

    /**
     * Obtain the semantic type that describe the action.
     * @param rawAction the input rawAction to get the semantic type
     * @return an optional with the type of the action
     */
    Optional<String> obtainActionType(String rawAction);
}
