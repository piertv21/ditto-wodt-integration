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
package org.eclipse.ditto.wodt.ontologies;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.ditto.wodt.model.ontology.DTOntology;
import org.eclipse.ditto.wodt.model.ontology.Individual;
import org.eclipse.ditto.wodt.model.ontology.Literal;
import org.eclipse.ditto.wodt.model.ontology.Node;
import org.eclipse.ditto.wodt.model.ontology.Property;

/**
 * Ontology for the BulbHolder Digital Twin.
 */
public final class AmbulanceDTOntology implements DTOntology {
    private static final Map<String, Pair<String, String>> propertyMap = Map.of(
        "busy", Pair.of(
                "https://ambulanceontology.com/ontology#busy",
                "https://www.w3.org/2001/XMLSchema#boolean"
        ),
        "fuelLevel", Pair.of(
                "https://ambulanceontology.com/ontology#fuelLevel",
                "https://www.w3.org/2001/XMLSchema#integer"
        )
    );

    private static final Map<String, Pair<String, String>> relationshipMap = Map.of(
        "rel-is_part_of_mission", Pair.of(
                "https://ambulanceontology.com/ontology#isPartOfMission",
                "https://missionontology.com/ontology#Mission"
        ),
        "rel-is_approaching", Pair.of(
                "https://ambulanceontology.com/ontology#isApproachingIntersection",
                "https://intersectionontology.com/ontology#Intersection"
        )
    );

    private static final Map<String, String> actionMap = Map.of(
        "toggle-plug-cord-attachment", "https://ambulanceontology.com/ontology#TogglePlugCordAttachment",
        "Bulb_toggle", "https://bulbontology.com/ontology#Toggle"
    );

    private static final Map<String, String> eventMap = Map.of(
        "Bulb_burnt-out-bulb", "boolean",
        "overheating", "boolean"
    );

    @Override
    public String getDigitalTwinType() {
        return "https://ambulanceontology.com/ontology#BulbHolder";
    }

    @Override
    public Optional<Property> obtainProperty(final String rawProperty) {
        final Map<String, Pair<String, String>> predicates = new HashMap<>(propertyMap);
        relationshipMap.forEach((key, value) -> predicates.merge(key, value, (oldValue, newValue) -> newValue));
        if (predicates.containsKey(rawProperty)) {
            return Optional.of(new Property(predicates.get(rawProperty).getLeft()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> obtainPropertyValueType(final String rawProperty) {
        final Map<String, Pair<String, String>> predicates = new HashMap<>(propertyMap);
        relationshipMap.forEach((key, value) -> predicates.merge(key, value, (oldValue, newValue) -> newValue));
        if (predicates.containsKey(rawProperty)) {
            return Optional.of(predicates.get(rawProperty).getRight());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<Pair<Property, Node>> convertPropertyValue(final String rawProperty, final T value) {
        if (propertyMap.containsKey(rawProperty)) {
            return Optional.of(Pair.of(new Property(propertyMap.get(rawProperty).getLeft()), new Literal<>(value)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Pair<Property, Individual>> convertRelationship(
            final String rawRelationship,
            final String targetUri
    ) {
        if (relationshipMap.containsKey(rawRelationship)) {
            return Optional.of(
                    Pair.of(new Property(relationshipMap.get(rawRelationship).getLeft()), new Individual(targetUri))
            );
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> obtainActionType(final String rawAction) {
        if (actionMap.containsKey(rawAction)) {
            return Optional.of(actionMap.get(rawAction));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> obtainEventType(String rawEvent) {
        if (eventMap.containsKey(rawEvent)) {
            return Optional.of(eventMap.get(rawEvent));
        } else {
            return Optional.empty();
        }
    }
}
