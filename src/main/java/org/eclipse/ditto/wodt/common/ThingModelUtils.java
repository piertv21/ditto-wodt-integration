package org.eclipse.ditto.wodt.common;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.wodt.model.ontology.WoDTVocabulary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/*
 * Utility class for Thing-related operations
 */
public final class ThingModelUtils {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static Optional<String> digitalTwinType;
    private static List<ThingModelElement> contextExtensionsList;
    private static List<ThingModelElement> propertiesList;
    private static List<ThingModelElement> actionsList;
    private static List<ThingModelElement> eventsList;

    public ThingModelUtils(Thing thing) {
        digitalTwinType = Optional.empty();
        this.extractDataFromThing(thing);
    }

    /**
     * Extract some information exploring a Thing model hierarchically.
     * Output: List of lists.
     * [0] = Context extensions list with element:              (alias, URL)
     * [1] = Properties and Relationships list with element:    (name, featureName?, type?, domainPredicate?)
     * [2] = Actions list with element:                         (name, featureName?, type?)
     * [3] = Events list with element:                          (name, featureName?, type?)
     */
    private void extractDataFromThing(Thing thing) {
        contextExtensionsList = new ArrayList<>();
        propertiesList = new ArrayList<>();
        actionsList = new ArrayList<>();
        eventsList = new ArrayList<>();

        // Current Thing
        extractDataFromCurrentModel(
            thing.getDefinition().get().toString(), "",
            contextExtensionsList, propertiesList,
            actionsList, eventsList
        );

        // Submodels
        thing.getFeatures().ifPresent(features -> {
            features.forEach((feature) -> {
                String featureName = feature.getId();
                feature.getDefinition().ifPresent(def -> {
                    extractDataFromCurrentModel(
                        def.getFirstIdentifier().toString(), featureName,
                        contextExtensionsList, propertiesList,
                        actionsList, eventsList
                    );
                });
            });
        });
    }

    private static void extractDataFromCurrentModel(
        String url,
        String featureName,
        List<ThingModelElement> contextExtensionsList,
        List<ThingModelElement> propertiesList,
        List<ThingModelElement> actionsList,
        List<ThingModelElement> eventsList
    ) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

            // Digital twin type
            if (jsonObject.has("@type")) {
                try {
                    JsonArray typeArray = jsonObject.getAsJsonArray("@type");
                    if (typeArray.size() > 1) {
                        digitalTwinType = Optional.of(typeArray.get(1).getAsString());
                    }
                } catch (Exception e) {
                    // Continue
                }
            }

            // Context extensions
            if (jsonObject.has("@context")) {
                JsonArray contextArray = jsonObject.getAsJsonArray("@context");
                for (JsonElement contextElement : contextArray) {
                    if (contextElement.isJsonObject()) {
                        JsonObject contextObj = contextElement.getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : contextObj.entrySet()) {
                            String alias = entry.getKey();
                            String contextUrl = entry.getValue().getAsString();
                            addModelElement(contextExtensionsList, new ThingModelElement(alias, Optional.of(contextUrl), Optional.empty(), Optional.empty()));
                        }
                    }
                }
            }

            // Properties
            if (jsonObject.has("properties")) {
                JsonObject properties = jsonObject.getAsJsonObject("properties");
                for (String propertyKey : properties.keySet()) {
                    JsonObject property = properties.getAsJsonObject(propertyKey);        
                    Optional<String> type = property.has("@type") ? Optional.of(property.get("@type").getAsString()) : Optional.empty();
                    Optional<String> domainPredicate = property.has(WoDTVocabulary.DOMAIN_PREDICATE.getUri())
                        ? Optional.of(property.get(WoDTVocabulary.DOMAIN_PREDICATE.getUri()).getAsString()) : Optional.empty();
                    if (property.has("properties")) {
                        // Complex properties
                        JsonObject subProperties = property.getAsJsonObject("properties");
                        for (String subPropertyKey : subProperties.keySet()) {
                            JsonObject subProperty = subProperties.getAsJsonObject(subPropertyKey);
                            Optional<String> subType = subProperty.has("@type") ? Optional.of(subProperty.get("@type").getAsString()) : Optional.empty();
                            Optional<String> subDomainPredicate = subProperty.has(WoDTVocabulary.DOMAIN_PREDICATE.getUri())
                                ? Optional.of(subProperty.get(WoDTVocabulary.DOMAIN_PREDICATE.getUri()).getAsString()) : Optional.empty();
                            addModelElement(propertiesList, new ThingModelElement(propertyKey + "_" + subPropertyKey, Optional.of(featureName), subType, subDomainPredicate));
                        }
                    } else {
                        // Simple properties
                        addModelElement(propertiesList, new ThingModelElement(propertyKey, Optional.of(featureName), type, domainPredicate));
                    }
                }
            }

            // Actions
            if (jsonObject.has("actions")) {
                JsonObject actions = jsonObject.getAsJsonObject("actions");
                for (String actionKey : actions.keySet()) {
                    JsonObject action = actions.getAsJsonObject(actionKey);
                    Optional<String> type = action.has("@type") ? Optional.of(action.get("@type").getAsString()) : Optional.empty();
                    addModelElement(actionsList, new ThingModelElement(actionKey, Optional.of(featureName), type, Optional.empty()));
                }
            }

            // Events
            if (jsonObject.has("events")) {
                JsonObject events = jsonObject.getAsJsonObject("events");
                for (String eventKey : events.keySet()) {
                    JsonObject event = events.getAsJsonObject(eventKey);
                    Optional<String> type = Optional.empty();
                    if (event.has("data") && event.getAsJsonObject("data").has("type")) {
                        type = Optional.of(event.getAsJsonObject("data").get("type").getAsString());
                    }
                    addModelElement(eventsList, new ThingModelElement(eventKey, Optional.of(featureName), type, Optional.empty()));
                }
            }

            // Submodels
            if (jsonObject.has("links")) {
                JsonArray links = jsonObject.getAsJsonArray("links");
                links.forEach(linkElement -> {
                    JsonObject link = linkElement.getAsJsonObject();
                    if (link.has("rel")) {
                        String rel = link.get("rel").getAsString();
                        if ("tm:submodel".equals(rel)) {
                            String href = link.get("href").getAsString();
                            String instanceName = link.has("instanceName") ? link.get("instanceName").getAsString() : "";
                            extractDataFromCurrentModel(href, instanceName, contextExtensionsList, propertiesList, actionsList, eventsList);
                        } else if ("tm:extends".equals(rel)) {
                            String href = link.get("href").getAsString();
                            extractDataFromCurrentModel(href, featureName, contextExtensionsList, propertiesList, actionsList, eventsList);
                        }
                    }
                });
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during thing model parsing: " + url);
        }
    }

    /*
     * Convert a string to its corresponding type.
     */
    public static Object convertStringToType(String input) {
        // null
        if (input == null || input.equalsIgnoreCase("null")) {
            return null;
        }
        // Boolean
        if (input.equalsIgnoreCase("true")
            || input.equalsIgnoreCase("false")) {
            return Boolean.valueOf(input);
        }
        // Integer
        try {
            BigInteger intValue = BigInteger.valueOf(Long.parseLong(input));
            return intValue;
        } catch (NumberFormatException e) {
            // Continue
        }
        // Long
        try {
            Long longValue = Long.valueOf(input);
            return longValue;
        } catch (NumberFormatException e) {
            // Continue
        }
        // Double
        try {
            Double doubleValue = Double.valueOf(input);
            return doubleValue;
        } catch (NumberFormatException e) {
            // Continue
        }
        // JSON
        try {
            JsonElement jsonElement = JsonParser.parseString(input);
            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();
            } else if (jsonElement.isJsonArray()) {
                return jsonElement.getAsJsonArray();
            }
        } catch (JsonSyntaxException e) {
            // Continue
        }
        // Otherwise return the input as String
        return input.replace("\"", "");
    }

    /*
     * Extract the names of the sub-properties of a JSON property.
     */
    public static List<String> extractSubPropertiesNames(final String jsonProperty) {
        List<String> subProperties = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonProperty);
            if (rootNode.isObject()) {
                Iterator<String> fieldNames = rootNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    subProperties.add(fieldName);
                }
            }
        } catch (JsonProcessingException e) {
            System.out.println("Error");
        }
        return subProperties;
    }

    /*
     * Extract the value of a sub-property of a JSON property.
     */
    public static String extractSubPropertyValue(final String jsonValue, final String key) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonValue);
            if (rootNode.isObject() && rootNode.has(key)) {
                JsonNode subPropertyNode = rootNode.get(key);
                return subPropertyNode.asText();
            }
        } catch (JsonProcessingException e) {
            return null;
        }
        return null;
    }

    private static void addModelElement(List<ThingModelElement> list, ThingModelElement element) {
        if (!list.contains(element)) {
            list.add(element);
        }
    }

    public Optional<String> getDigitalTwinType() {
        return digitalTwinType;
    }    

    public List<ThingModelElement> getTMContextExtensions() {
        return List.copyOf(contextExtensionsList);
    }

    public List<ThingModelElement> getTMProperties() {
        return List.copyOf(propertiesList);
    }

    public List<ThingModelElement> getTMActions() {
        return List.copyOf(actionsList);
    }

    public List<ThingModelElement> getTMEvents() {
        return List.copyOf(eventsList);
    }
}
