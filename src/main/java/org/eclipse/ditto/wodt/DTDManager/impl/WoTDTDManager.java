package org.eclipse.ditto.wodt.DTDManager.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.ditto.wodt.DTDManager.api.DTDManager;
import org.eclipse.ditto.wodt.PlatformManagementInterface.api.PlatformManagementInterfaceReader;
import org.eclipse.ditto.wodt.common.ThingModelElement;
import static org.eclipse.ditto.wodt.common.ThingUtils.extractDataFromThing;
import org.eclipse.ditto.wodt.model.ontology.DTOntology;
import org.eclipse.ditto.wodt.model.ontology.Property;
import org.eclipse.ditto.wodt.model.ontology.WoDTVocabulary;

import io.github.sanecity.wot.DefaultWot;
import io.github.sanecity.wot.WotException;
import io.github.sanecity.wot.thing.Context;
import io.github.sanecity.wot.thing.ExposedThing;
import io.github.sanecity.wot.thing.Thing;
import io.github.sanecity.wot.thing.Type;
import io.github.sanecity.wot.thing.action.ExposedThingAction;
import io.github.sanecity.wot.thing.action.ThingAction;
import io.github.sanecity.wot.thing.event.ExposedThingEvent;
import io.github.sanecity.wot.thing.event.ThingEvent;
import io.github.sanecity.wot.thing.form.Form;
import io.github.sanecity.wot.thing.form.Operation;
import io.github.sanecity.wot.thing.property.ExposedThingProperty;
import io.github.sanecity.wot.thing.property.ThingProperty;
import io.github.sanecity.wot.thing.schema.VariableDataSchema;
import io.github.sanecity.wot.thing.security.BasicSecurityScheme;

/**
 * This class provide an implementation of the {@link io.github.webbasedwodt.application.component.DTDManager} using
* a WoT Thing Description to implement the Digital Twin Descriptor.
*/
public class WoTDTDManager implements DTDManager {

    private static final int DITTO_PORT_NUMBER = 8080;    
    private static final String BASE_URL = "http://localhost:" + DITTO_PORT_NUMBER + "/api/2/things/";
    private static final String ATTRIBUTE_URL = "/attributes/{attributePath}";
    private static final String FEATURE_URL = "/features/{featureId}";
    private static final String PROPERTY_URL = FEATURE_URL + "/properties/{propertyPath}";
    private static final String ACTION_URL = "/inbox/messages/";
    private static final String EVENT_URL = "/outbox/messages/";

    private static final String THING_DESCRIPTION_CONTEXT = "https://www.w3.org/2019/wot/td/v1";
    private static final String VERSION = "1.0.0";
    private static final String SNAPSHOT_DTD_PROPERTY = "snapshot";
    private final String digitalTwinUri;
    private final String physicalAssetId;
    private final DTOntology ontology;
    private final int portNumber;
    private final String dittoThingId;
    private final PlatformManagementInterfaceReader platformManagementInterfaceReader;

    private final Map<String, ThingProperty<Object>> properties;
    private final Map<String, ThingProperty<Object>> relationships;
    private final Map<String, ThingAction<Object, Object>> actions;
    private final Map<String, ThingEvent<Object>> events;
    
    private List<ThingModelElement> extractedContextExtensionsList;
    private List<ThingModelElement> extractedPropertiesList;
    private List<ThingModelElement> extractedActionsList;
    private List<ThingModelElement> extractedEventsList;

    /**
     * Default constructor.
    * @param digitalTwinUri the uri of the WoDT Digital Twin
    * @param ontology the ontology used to obtain the semantics
    * @param physicalAssetId the id of the associated physical asset
    * @param portNumber the port number where to offer the affordances
    * @param platformManagementInterfaceReader the platform management interface reader reference
    */
    public WoTDTDManager(
        final String digitalTwinUri,
        final DTOntology ontology,
        final String physicalAssetId,
        final int portNumber,
        final PlatformManagementInterfaceReader platformManagementInterfaceReader,
        final org.eclipse.ditto.things.model.Thing dittoThing
    ) {
        this.dittoThingId = dittoThing.getEntityId().get().toString();
        this.extractThingModelData(dittoThing);        
        this.digitalTwinUri = digitalTwinUri;
        this.ontology = ontology;
        this.physicalAssetId = physicalAssetId;
        this.portNumber = portNumber;
        this.platformManagementInterfaceReader = platformManagementInterfaceReader;
        this.properties = new HashMap<>();
        this.relationships = new HashMap<>();
        this.actions = new HashMap<>();
        this.events = new HashMap<>();
    }

    private void extractThingModelData(final org.eclipse.ditto.things.model.Thing dittoThing) {
        List<List<ThingModelElement>> extractDataFromThing = extractDataFromThing(dittoThing);
        this.extractedContextExtensionsList = extractDataFromThing.get(0);
        this.extractedPropertiesList = extractDataFromThing.get(1);
        this.extractedActionsList = extractDataFromThing.get(2);
        this.extractedEventsList = extractDataFromThing.get(3);
    }

    @Override
    public void addProperty(final String rawPropertyName) {
        this.createThingDescriptionProperty(rawPropertyName, true)
                .ifPresent(property -> this.properties.put(rawPropertyName, property));
    }

    @Override
    public boolean removeProperty(final String rawPropertyName) {
        return this.properties.remove(rawPropertyName) != null;
    }

    @Override
    public void addRelationship(final String rawRelationshipName) {
        this.createThingDescriptionProperty(rawRelationshipName, false)
                .ifPresent(relationship -> this.relationships.put(rawRelationshipName, relationship));
    }

    @Override
    public boolean removeRelationship(final String rawRelationshipName) {
        return this.relationships.remove(rawRelationshipName) != null;
    }

    @Override
    public void addAction(final String rawActionName) {
        this.createThingDescriptionAction(rawActionName).ifPresent(action -> this.actions.put(rawActionName, action));
    }

    @Override
    public boolean removeAction(final String rawActionName) {
        return this.actions.remove(rawActionName) != null;
    }

    @Override
    public Thing<?, ?, ?> getDTD() {
        try {
            Context context = new Context(THING_DESCRIPTION_CONTEXT);
            extractedContextExtensionsList.forEach(contextExtensions ->
                context.addContext(contextExtensions.getElement(), contextExtensions.getValue().get())
            );
            final ExposedThing thingDescription = new DefaultWot().produce(new Thing.Builder()
                    .setId(this.digitalTwinUri)
                    .setObjectContext(context)
                    .build()
            );
            this.initializeThingDescription(thingDescription);
            this.properties.forEach(thingDescription::addProperty);
            this.relationships.forEach(thingDescription::addProperty);
            this.actions.forEach((rawActionName, action) ->
                    thingDescription.addAction(rawActionName, action, () -> { }));
            this.events.forEach((rawEventName, event) -> thingDescription.addEvent(rawEventName, event));
            
            thingDescription.getProperties().forEach((name, property) -> 
                this.addPropertyAffordances(name, property)
            );
            thingDescription.getActions().forEach((name, action) -> 
                this.addActionAffordances(name, action)
            );
            thingDescription.getEvents().forEach((name, event) -> 
                this.addEventAffordances(name, event)
            );
            return thingDescription;
        } catch (WotException e) {
            throw new IllegalStateException("Impossible to create the WoT DTD Manager in the current state", e);
        }
    }

    private void addPropertyAffordances(String name, ExposedThingProperty<Object> property) {
        if (!name.equals("snapshot")) {
            if (name.contains("rel-")) {
                addRelAffordances(name, property);
            } else {
                addStandardPropertyAffordances(name, property);
            }
        }
    }

    private void addStandardPropertyAffordances(String name, ExposedThingProperty<Object> property) {
        String[] splitName = splitStringAtFirstCharOccurrence(name, '_');
        ThingModelElement prop = findThingModelElement(extractedPropertiesList, name, splitName);    
        String href = BASE_URL + this.dittoThingId;
        if (prop.getValue().isPresent()) {
            href += PROPERTY_URL.replace("{featureId}", prop.getValue().get())
                    .replace("{propertyPath}", splitName[1].replace("_", "/"));
        } else {
            href += ATTRIBUTE_URL.replace("{attributePath}", name);
        }    
        addFormsToProperty(property, href);
    }

    private void addRelAffordances(String name, ExposedThingProperty<Object> property) {
        String href = BASE_URL + this.dittoThingId + ATTRIBUTE_URL.replace("{attributePath}", name);
        addFormsToProperty(property, href);
    }

    private void addFormsToProperty(ExposedThingProperty<Object> property, String href) {
        property.addForm(new Form.Builder()
            .addOp(Operation.READ_PROPERTY)
            .setHref(href)
            .build());
        property.addForm(new Form.Builder()
            .addOp(Operation.OBSERVE_PROPERTY)
            .setHref(href)
            .setSubprotocol("sse")
            .build());
    }

    private void addActionAffordances(String name, ExposedThingAction<Object, Object> action) {
        String[] splitName = splitStringAtFirstCharOccurrence(name, '_');
        ThingModelElement act = findThingModelElement(extractedActionsList, name, splitName);    
        String href = BASE_URL + this.dittoThingId;
        if (act.getValue().isPresent()) {
            href += FEATURE_URL.replace("{featureId}", act.getValue().get()) + ACTION_URL + splitName[1];
        } else {
            href += ACTION_URL + name;
        }    
        action.addForm(new Form.Builder()
            .addOp(Operation.INVOKE_ACTION)
            .setHref(href)
            .build());
    }

    private void addEventAffordances(String name, ExposedThingEvent<Object> event) {
        String[] splitName = splitStringAtFirstCharOccurrence(name, '_');
        ThingModelElement evt = findThingModelElement(extractedEventsList, name, splitName);    
        String href = BASE_URL + this.dittoThingId;
        if (evt.getValue().isPresent()) {
            href += FEATURE_URL.replace("{featureId}", evt.getValue().get()) + EVENT_URL + splitName[1];
        } else {
            href += EVENT_URL + name;
        }    
        event.addForm(new Form.Builder()
            .addOp(Operation.SUBSCRIBE_EVENT)
            .setHref(href)
            .setSubprotocol("sse")
            .build());
    }

    private ThingModelElement findThingModelElement(List<ThingModelElement> list, String name, String[] splitName) {
        return list.stream()
            .filter(p -> splitName != null 
                    ? p.getElement().equals(splitName[1]) && p.getValue().get().equals(splitName[0])
                    : p.getElement().equals(name))
            .findFirst()
            .orElse(null);
    }    

    private String[] splitStringAtFirstCharOccurrence(String input, char character) {
        int underscoreIndex = input.indexOf(character);
        if (underscoreIndex != -1) {
            String firstPart = input.substring(0, underscoreIndex);
            String secondPart = input.substring(underscoreIndex + 1);
            return new String[]{firstPart, secondPart};
        } else {
            return null;
        }
    }

    private void initializeThingDescription(final ExposedThing thingDescription) {
        thingDescription.setObjectType(new Type(this.ontology.getDigitalTwinType()));
        thingDescription.addProperty(SNAPSHOT_DTD_PROPERTY, new ExposedThingProperty.Builder()
                .setReadOnly(true)
                .setObservable(true)
                .build());
        // Necessary to add the form afterward considering that the wot-servient library when adding a property
        // to an ExposedThing resets in an unexpected way the forms.
        final ThingProperty<?> snapshotProperty = thingDescription.getProperty(SNAPSHOT_DTD_PROPERTY);
        snapshotProperty.addForm(new Form.Builder()
                .addOp(Operation.OBSERVE_PROPERTY)
                .setHref("ws://host.docker.internal:" + this.portNumber + "/dtkg")
                .setSubprotocol("websocket")
                .build());
        thingDescription.getMetadata()
                        .put("links",
                            this.platformManagementInterfaceReader
                                .getRegisteredPlatformUrls()
                                .stream().map(uri -> new WoDTDigitalTwinsPlatformLink(uri.toString()))
                                .collect(Collectors.toList())
                        );
        thingDescription.getMetadata().put(WoDTVocabulary.PHYSICAL_ASSET_ID.getUri(), this.physicalAssetId);
        thingDescription.getMetadata().put(WoDTVocabulary.VERSION.getUri(), VERSION);
        
        thingDescription.setSecurityDefinitions(Map.of("basic_sc", new BasicSecurityScheme("header")));
        thingDescription.setSecurity(List.of("basic_sc"));
    }

    private Optional<ThingProperty<Object>> createThingDescriptionProperty(
            final String rawPropertyName,
            final boolean indicateAugmentation
    ) {
        final Optional<String> propertyValueType = this.ontology.obtainPropertyValueType(rawPropertyName);
        final Optional<String> domainPredicateUri = this.ontology.obtainProperty(rawPropertyName).flatMap(Property::getUri);

        if (propertyValueType.isPresent() && domainPredicateUri.isPresent()) {
            final Map<String, Object> metadata = new HashMap<>();
            metadata.put(WoDTVocabulary.DOMAIN_PREDICATE.getUri(), domainPredicateUri.get());
            if (indicateAugmentation) {
                metadata.put(WoDTVocabulary.AUGMENTED_INTERACTION.getUri(), false);
            }
            return Optional.of(new ThingProperty.Builder()
                    .setObjectType(propertyValueType.get())
                    .setType("") // Empty auto-added type: "string"
                    .setReadOnly(true)
                    .setObservable(true)
                    .setOptionalProperties(metadata)
                    .build());
        } else {
            return Optional.empty();
        }
    }

    private Optional<ThingAction<Object, Object>> createThingDescriptionAction(final String rawActionName) {
        return this.ontology.obtainActionType(rawActionName).map(actionType -> new ThingAction.Builder()
                .setObjectType(actionType)
                .setInput(null)
                .setOutput(null)
                .build());
    }

    private Optional<ThingEvent<Object>> createThingDescriptionEvent(final String rawEventName) {
        return this.ontology.obtainEventType(rawEventName).map(eventType -> new ThingEvent.Builder()
                .setData(new VariableDataSchema.Builder().setType(eventType).build())
                .build());
    }

    @Override
    public void addEvent(String rawEventName) {
        this.createThingDescriptionEvent(rawEventName).ifPresent(event -> this.events.put(rawEventName, event));
    }

    @Override
    public boolean removeEvent(String rawEventName) {
        return this.events.remove(rawEventName) != null;
    }

    @Override
    public List<ThingModelElement> getTMProperties() {
        return List.copyOf(this.extractedPropertiesList);
    }

    @Override
    public List<ThingModelElement> getTMActions() {
        return List.copyOf(this.extractedActionsList);
    }

    @Override
    public List<ThingModelElement> getTMEvents() {
        return List.copyOf(this.extractedEventsList);
    }

    /**
     * Class to describe a link to a WoDT Digital Twins Platform within the DTD.
    */
    private static class WoDTDigitalTwinsPlatformLink {
        private final String href;
        private final String rel;

        /**
         * Default constructor.
        * @param href the url of the WoDT Digital Twins Platform
        */
        WoDTDigitalTwinsPlatformLink(final String href) {
            this.href = href;
            this.rel = WoDTVocabulary.REGISTERED_TO_PLATFORM.getUri();
        }

        /**
         * Obtain the url of the WoDT Digital Twins Platform.
        * @return the url
        */
        @SuppressWarnings("unused")
        public String getHref() {
            return this.href;
        }

        /**
         * Obtain the relationship of the link following the Web Linking Specification.
        * @return the relation of the link
        */
        @SuppressWarnings("unused")
        public String getRel() {
            return this.rel;
        }
    }
}