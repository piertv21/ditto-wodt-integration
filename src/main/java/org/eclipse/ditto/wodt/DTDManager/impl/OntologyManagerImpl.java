package org.eclipse.ditto.wodt.DTDManager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.wodt.DTDManager.api.OntologyManager;
import org.eclipse.ditto.wodt.common.ThingModelElement;
import org.eclipse.ditto.wodt.common.ThingModelUtils;
import org.eclipse.ditto.wodt.model.ontology.DTOntology;
import org.eclipse.ditto.wodt.model.ontology.Individual;
import org.eclipse.ditto.wodt.model.ontology.Literal;
import org.eclipse.ditto.wodt.model.ontology.Node;
import org.eclipse.ditto.wodt.model.ontology.Property;

public final class OntologyManagerImpl implements DTOntology, OntologyManager {
    
    private final ThingModelUtils thingModelUtils;
    private final YamlOntologyHandler yamlOntologyHandler;

    public OntologyManagerImpl(
        Thing dittoThing,
        String yamlOntologyPath
    ) {
        this.thingModelUtils = new ThingModelUtils(dittoThing);
        this.yamlOntologyHandler = new YamlOntologyHandler(yamlOntologyPath);
    }

    @Override
    public String getDigitalTwinType() {
        return yamlOntologyHandler.getDigitalTwinType().orElse(
            thingModelUtils.getDigitalTwinType().orElse("UnknownDigitalTwinType")
        );
    }

    @Override
    public Optional<Property> obtainProperty(String rawProperty) {
        Map<String, Pair<String, String>> predicates = getMergedPropertiesAndRelationships();
        if (predicates.containsKey(rawProperty)) {
            return Optional.of(new Property(predicates.get(rawProperty).getLeft()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> obtainPropertyValueType(String rawProperty) {
        Map<String, Pair<String, String>> predicates = getMergedPropertiesAndRelationships();
        if (predicates.containsKey(rawProperty)) {
            return Optional.of(predicates.get(rawProperty).getRight());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<Pair<Property, Node>> convertPropertyValue(String rawProperty, T value) {
        Map<String, Pair<String, String>> propertiesMap = getMergedPropertiesAndRelationships();
        if (propertiesMap.containsKey(rawProperty)) {
            return Optional.of(Pair.of(new Property(propertiesMap.get(rawProperty).getLeft()), new Literal<>(value)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Pair<Property, Individual>> convertRelationship(String rawRelationship, String targetUri) {
        Map<String, Pair<String, String>> relationshipsMap = getMergedPropertiesAndRelationships();
        if (relationshipsMap.containsKey(rawRelationship)) {
            return Optional.of(Pair.of(new Property(relationshipsMap.get(rawRelationship).getLeft()), new Individual(targetUri)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> obtainActionType(String rawAction) {
        Map<String, String> mergedActions = getMergedActions();
        if (mergedActions.containsKey(rawAction)) {
            return Optional.of(mergedActions.get(rawAction));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> obtainEventType(String rawEvent) {
        Map<String, String> mergedEvents = getMergedEvents();
        if (mergedEvents.containsKey(rawEvent)) {
            return Optional.of(mergedEvents.get(rawEvent));
        } else {
            return Optional.empty();
        }
    }
    
    private Map<String, Pair<String, String>> getMergedPropertiesAndRelationships() {
        Map<String, Pair<String, String>> mergedMap = new HashMap<>();
        thingModelUtils.getTMProperties().forEach(element -> {
            mergedMap.put(
                element.getField(),
                Pair.of(element.getDomainPredicate().orElse(""), element.getType().orElse(""))
            );
        });
        
        yamlOntologyHandler.getProperties().stream().forEach(properties -> {
            properties.ifPresent(prop -> {
                String name = prop.get("name");
                String domainPredicate = prop.get("domainPredicate");
                String type = prop.get("type");
                mergedMap.merge(name, Pair.of(domainPredicate, type), (existing, newValue) -> {
                    String mergedDomainPredicate = newValue.getLeft() != null ? newValue.getLeft() : existing.getLeft();
                    String mergedType = newValue.getRight() != null ? newValue.getRight() : existing.getRight();
                    return Pair.of(mergedDomainPredicate, mergedType);
                });
            });
        });
        return mergedMap;
    }
    
    private Map<String, String> getMergedActions() {
        Map<String, String> mergedActions = new HashMap<>();        
        thingModelUtils.getTMActions().forEach(element -> {
            mergedActions.put(element.getField(), element.getType().orElse(""));
        });

        yamlOntologyHandler.getActions().stream().forEach(actions -> {
            actions.ifPresent(action -> {
                String name = action.get("name");
                String type = action.get("type");
                mergedActions.merge(name, type, (existing, newValue) ->
                    newValue != null ? newValue : existing);
            });
        });
        return mergedActions;
    }
    
    private Map<String, String> getMergedEvents() {
        Map<String, String> mergedEvents = new HashMap<>();
        thingModelUtils.getTMEvents().forEach(element -> {
            mergedEvents.put(element.getField(), element.getType().orElse(""));
        });

        yamlOntologyHandler.getEvents().stream().forEach(events -> {
            events.ifPresent(event -> {
                String name = event.get("name");
                String type = event.get("type");
                mergedEvents.merge(name, type, (existing, newValue) -> 
                    newValue != null ? newValue : existing);
                });
        });
        return mergedEvents;
    }

    @Override
    public List<ThingModelElement> getAvailableContextExtensions() {
        return thingModelUtils.getTMContextExtensions();
    }

    @Override
    public List<ThingModelElement> getAvailableProperties() {
        Map<String, Pair<String, String>> mergedPropertiesAndRelationships = getMergedPropertiesAndRelationships();
        List<ThingModelElement> propertiesList = new ArrayList<>();

        thingModelUtils.getTMProperties().forEach(element -> {
            String field = element.getField();
            String featureName = element.getFeature().orElse("");
            
            if (mergedPropertiesAndRelationships.containsKey(field)) {
                String domainPredicate = mergedPropertiesAndRelationships.get(field).getLeft();
                String type = mergedPropertiesAndRelationships.get(field).getRight();
                propertiesList.add(new ThingModelElement(field, Optional.of(featureName), Optional.of(type), Optional.of(domainPredicate)));
            } else {
                propertiesList.add(element);
            }
        });

        return propertiesList;
    }

    @Override
    public List<ThingModelElement> getAvailableRelationships() {
        List<ThingModelElement> relationshipsList = new ArrayList<>();

        thingModelUtils.getTMProperties().forEach(element -> {
            String field = element.getField();
            if (field.startsWith("rel-")) {
                relationshipsList.add(element);
            }
        });

        yamlOntologyHandler.getProperties().stream().forEach(optionalMap -> {
            optionalMap.ifPresent(property -> {
                String name = property.get("name");
                if (name.startsWith("rel-")) {
                    String domainPredicate = property.get("domainPredicate");
                    String type = property.get("type");
                    relationshipsList.add(new ThingModelElement(name, Optional.empty(), Optional.ofNullable(type), Optional.ofNullable(domainPredicate)));
                }
            });
        });

        return relationshipsList;
    }

    @Override
    public List<ThingModelElement> getAvailableActions() {
        Map<String, String> mergedActions = getMergedActions();
        List<ThingModelElement> actionsList = new ArrayList<>();

        thingModelUtils.getTMActions().forEach(element -> {
            String field = element.getField();
            String featureName = element.getFeature().orElse("");
            
            if (mergedActions.containsKey(field)) {
                String type = mergedActions.get(field);
                actionsList.add(new ThingModelElement(field, Optional.of(featureName), Optional.of(type), Optional.empty()));
            } else {
                actionsList.add(element);
            }
        });

        return actionsList;
    }

    @Override
    public List<ThingModelElement> getAvailableEvents() {
        Map<String, String> mergedEvents = getMergedEvents();
        List<ThingModelElement> eventsList = new ArrayList<>();

        thingModelUtils.getTMEvents().forEach(element -> {
            String field = element.getField();
            String featureName = element.getFeature().orElse("");

            if (mergedEvents.containsKey(field)) {
                String type = mergedEvents.get(field);
                eventsList.add(new ThingModelElement(field, Optional.of(featureName), Optional.of(type), Optional.empty()));
            } else {
                eventsList.add(element);
            }
        });

        return eventsList;
    }

}
