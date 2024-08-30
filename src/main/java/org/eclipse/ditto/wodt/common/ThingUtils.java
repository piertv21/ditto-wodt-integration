package org.eclipse.ditto.wodt.common;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.ditto.things.model.Thing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/*
 * Utility class for Thing-related operations
 */
public class ThingUtils {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // TO DO: rimuovi l'estrazione di additionalData dagli eventi e dal ThingModelElement

    /**
     * Extract some information exploring a Thing model hierarchically.
     * Output: List of lists.
     * [0] = Context extensions list with element: (alias, URL)
     * [1] = Properties list with element: (name, featureName?, additionalData?)
     * [2] = Actions list with element: (name, featureName?, additionalData?)
     * [3] = Events list with element: (name, featureName?, additionalData?)
     * [4] = Relationships list with element: (name, featureName?, additionalData?)
     */
    public static List<List<ThingModelElement>> extractDataFromThing(Thing thing) {
        List<ThingModelElement> contextExtensionsList = new ArrayList<>();
        List<ThingModelElement> propertiesList = new ArrayList<>();
        List<ThingModelElement> actionsList = new ArrayList<>();
        List<ThingModelElement> eventsList = new ArrayList<>();
        List<ThingModelElement> relationshipsList = new ArrayList<>();

        // Current Thing
        extractDataFromCurrentModel(
            thing.getDefinition().get().toString(), "",
            contextExtensionsList, propertiesList,
            actionsList, eventsList, relationshipsList
        );

        // Submodels
        thing.getFeatures().ifPresent(features -> {
            features.forEach((feature) -> {
                String featureName = feature.getId();
                feature.getDefinition().ifPresent(def -> {
                    extractDataFromCurrentModel(
                        def.getFirstIdentifier().toString(), featureName,
                        contextExtensionsList, propertiesList,
                        actionsList, eventsList, relationshipsList
                    );
                });
            });
        });

        List<List<ThingModelElement>> result = new ArrayList<>();
        result.add(contextExtensionsList);
        result.add(propertiesList);
        result.add(actionsList);
        result.add(eventsList);
        result.add(relationshipsList);
        return result;
    }

    /**
     * Extract some information from a Thing model.
     */
    private static void extractDataFromCurrentModel(
            String url,
            String featureName,
            List<ThingModelElement> contextExtensionsList,
            List<ThingModelElement> propertiesList,
            List<ThingModelElement> actionsList,
            List<ThingModelElement> eventsList,
            List<ThingModelElement> relationshipsList
    ) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

            // Context extensions
            if (jsonObject.has("@context")) {
                JsonArray contextArray = jsonObject.getAsJsonArray("@context");
                for (JsonElement contextElement : contextArray) {
                    if (contextElement.isJsonObject()) {
                        JsonObject contextObj = contextElement.getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : contextObj.entrySet()) {
                            String alias = entry.getKey();
                            String contextUrl = entry.getValue().getAsString();
                            addModelElement(contextExtensionsList, new ThingModelElement(alias, Optional.of(contextUrl), Optional.empty()));
                        }
                    }
                }
            }

            // Properties and Relationships
            if (jsonObject.has("properties")) {
                JsonObject properties = jsonObject.getAsJsonObject("properties");
                for (String propertyKey : properties.keySet()) {
                    JsonObject property = properties.getAsJsonObject(propertyKey);
                    boolean isComplex = "object".equals(property.get("type").getAsString());

                    if (isComplex && property.has("properties")) {
                        JsonObject subProperties = property.getAsJsonObject("properties");
                        for (String subPropertyKey : subProperties.keySet()) {
                            String complexPropertyKey = propertyKey + "_" + subPropertyKey;
                            addModelElement(propertiesList, new ThingModelElement(complexPropertyKey, Optional.of(featureName), Optional.empty()));
                        }
                    } else {
                        if (propertyKey.startsWith("rel-")) {
                            addModelElement(relationshipsList, new ThingModelElement(propertyKey, Optional.of(featureName), Optional.empty()));
                        } else {
                            addModelElement(propertiesList, new ThingModelElement(propertyKey, Optional.of(featureName), Optional.empty()));
                        }
                    }
                }
            }

            // Actions
            if (jsonObject.has("actions")) {
                JsonObject actions = jsonObject.getAsJsonObject("actions");
                for (String actionKey : actions.keySet()) {
                    addModelElement(actionsList, new ThingModelElement(actionKey, Optional.of(featureName), Optional.empty()));
                }
            }

            // Events
            if (jsonObject.has("events")) {
                JsonObject events = jsonObject.getAsJsonObject("events");
                for (String eventKey : events.keySet()) {
                    JsonObject event = events.getAsJsonObject(eventKey);
                    Optional<String> additionalData = Optional.empty();
                    if (event.has("data")) {
                        additionalData = Optional.of(event.getAsJsonObject("data").toString());
                    }
                    addModelElement(eventsList, new ThingModelElement(
                        eventKey,
                        Optional.of(featureName),
                        additionalData
                    ));
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
                            extractDataFromCurrentModel(href, instanceName, contextExtensionsList, propertiesList, actionsList, eventsList, relationshipsList);
                        } else if ("tm:extends".equals(rel)) {
                            String href = link.get("href").getAsString();
                            extractDataFromCurrentModel(href, featureName, contextExtensionsList, propertiesList, actionsList, eventsList, relationshipsList);
                        }
                    }
                });                
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during thing model parsing: " + url);
        }
    }

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
            Integer intValue = Integer.valueOf(input);
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

    /**
     * Add a model element to the list if it is not already present,
     * so that duplicates are avoided but the order is preserved.
     */
    private static void addModelElement(List<ThingModelElement> list, ThingModelElement element) {
        if (!list.contains(element)) {
            list.add(element);
        }
    }
}
