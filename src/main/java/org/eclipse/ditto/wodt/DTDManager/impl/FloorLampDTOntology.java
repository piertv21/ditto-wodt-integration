package org.eclipse.ditto.wodt.DTDManager.impl;

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
 * Ontology for the FloorLamp Digital Twin.
*/
public final class FloorLampDTOntology implements DTOntology {
    private static final Map<String, Pair<String, String>> propertyMap = new HashMap<>();

    static {
        propertyMap.put("manufacturer", Pair.of(
            "https://flontology.com/ontology#manufacturer",
            "https://www.w3.org/2001/XMLSchema#string"
        ));
        propertyMap.put("serialNo", Pair.of(
            "https://flontology.com/ontology#serialNo",
            "https://www.w3.org/2001/XMLSchema#string"
        ));
        propertyMap.put("Spot1_dimmer-level", Pair.of(
            "https://dclontology.com/ontology#Spot1_dimmer-level",
            "http://www.ontology-of-units-of-measure.org/resource/om-2/Percentage"
        ));
        propertyMap.put("Spot1_color_r", Pair.of(
            "https://clontology.com/ontology#Spot1_color_r",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Spot1_color_g", Pair.of(
            "https://clontology.com/ontology#Spot1_color_g",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Spot1_color_b", Pair.of(
            "https://clontology.com/ontology#Spot1_color_b",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Spot1_on", Pair.of(
            "https://switchableontology.com/ontology#Spot1_on",
            "https://www.w3.org/2001/XMLSchema#boolean"
        ));
        propertyMap.put("Spot2_dimmer-level", Pair.of(
            "https://dclontology.com/ontology#Spot2_dimmer-level",
            "http://www.ontology-of-units-of-measure.org/resource/om-2/Percentage"
        ));
        propertyMap.put("Spot2_color_r", Pair.of(
            "https://clontology.com/ontology#Spot2_color_r",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Spot2_color_g", Pair.of(
            "https://clontology.com/ontology#Spot2_color_g",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Spot2_color_b", Pair.of(
            "https://clontology.com/ontology#Spot2_color_b",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Spot2_on", Pair.of(
            "https://switchableontology.com/ontology#Spot2_on",
            "https://www.w3.org/2001/XMLSchema#boolean"
        ));
        propertyMap.put("Spot3_dimmer-level", Pair.of(
            "https://dclontology.com/ontology#Spot3_dimmer-level",
            "http://www.ontology-of-units-of-measure.org/resource/om-2/Percentage"
        ));
        propertyMap.put("Spot3_color_r", Pair.of(
            "https://clontology.com/ontology#Spot3_color_r",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Spot3_color_g", Pair.of(
            "https://clontology.com/ontology#Spot3_color_g",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Spot3_color_b", Pair.of(
            "https://clontology.com/ontology#Spot3_color_b",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Spot3_on", Pair.of(
            "https://switchableontology.com/ontology#Spot3_on",
            "https://www.w3.org/2001/XMLSchema#boolean"
        ));
        propertyMap.put("ConnectionStatus_readySince", Pair.of(
            "https://csontology.com/ontology#readySince",
            "http://www.w3.org/2006/time#Instant"
        ));
        propertyMap.put("ConnectionStatus_readyUntil", Pair.of(
            "https://csontology.com/ontology#readyUntil",
            "http://www.w3.org/2006/time#Instant"
        ));
        propertyMap.put("PowerConsumptionAwareness_reportPowerConsumption_enabled", Pair.of(
            "https://pcaontology.com/ontology#reportPowerConsumption_enabled",
            "https://www.w3.org/2001/XMLSchema#boolean"
        ));
        propertyMap.put("PowerConsumptionAwareness_reportPowerConsumption_interval", Pair.of(
            "https://pcaontology.com/ontology#reportPowerConsumption_interval",
            "http://www.ontology-of-units-of-measure.org/resource/om-2/Duration"
        ));        
        propertyMap.put("Status-LED_color_r", Pair.of(
            "https://clontology.com/ontology#Status-LED_color_r",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Status-LED_color_g", Pair.of(
            "https://clontology.com/ontology#Status-LED_color_g",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Status-LED_color_b", Pair.of(
            "https://clontology.com/ontology#Status-LED_color_b",
            "https://www.w3.org/2001/XMLSchema#integer"
        ));
        propertyMap.put("Status-LED_on", Pair.of(
            "https://switchableontology.com/ontology#Status-LED_on",
            "https://www.w3.org/2001/XMLSchema#boolean"
        ));
    }
    
    private static final Map<String, Pair<String, String>> relationshipMap = Map.of(
        "located-inside", Pair.of(
                "https://bhontology/ontology#isLocatedInside",
                "https://homeontology/ontology#Room"
        )
    );

    private static final Map<String, String> actionMap = Map.of(
        "switch-all-spots", "https://flontology.com/ontology#SwitchAllSpots",
        "switch-all-spots-on-for-duration", "https://flontology.com/ontology#SwitchAllSpotsOnForDuration",
        "Spot1_toggle", "https://switchableontology.com/ontology#Spot1_toggle",
        "Spot1_switch-on-for-duration", "https://switchableontology.com/ontology#Spot1_switch-on-for-duration",
        "Spot2_toggle", "https://switchableontology.com/ontology#Spot2_toggle",
        "Spot2_switch-on-for-duration", "https://switchableontology.com/ontology#Spot2_switch-on-for-duration",
        "Spot3_toggle", "https://switchableontology.com/ontology#Spot3_toggle",
        "Spot3_switch-on-for-duration", "https://switchableontology.com/ontology#Spot3_switch-on-for-duration",
        "Status-LED_toggle", "https://switchableontology.com/ontology#Status-LED_toggle",
        "Status-LED_switch-on-for-duration", "https://switchableontology.com/ontology#Status-LED_switch-on-for-duration"
    );

    private static final Map<String, String> eventMap = Map.of(
        "PowerConsumptionAwareness_current-power-consumption", "http://www.ontology-of-units-of-measure.org/resource/om-2/Power",
        "SmokeDetection_smoke-detected", "object",
        "SmokeDetection_smoke-cleared", "object"
    );

    @Override
    public String getDigitalTwinType() {
        return "https://flontology.com/ontology#FloorLamp";
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
