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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/*
 * Utility class for Thing-related operations
 */
public class ThingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingUtils.class);
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Extract some information from a Thing model.
     * Output: List of lists.
     * [0] = Context extensions,
     * [1] = Properties,
     * [2] = Actions,
     * [3] = Events.
     */
    public static List<List<ThingModelElement>> extractPropertiesActionsEventsFromThing(Thing thing) {
        List<ThingModelElement> contextExtensionsList = new ArrayList<>();
        List<ThingModelElement> propertiesList = new ArrayList<>();
        List<ThingModelElement> actionsList = new ArrayList<>();
        List<ThingModelElement> eventsList = new ArrayList<>();

        // Current Thing
        extractPropertiesActionsEventsFromCurrentModel(
            thing.getDefinition().get().toString(), "",
            contextExtensionsList, propertiesList,
            actionsList, eventsList
        );

        // Submodels
        thing.getFeatures().ifPresent(features -> {
            features.forEach((feature) -> {
                String featureName = feature.getId();
                feature.getDefinition().ifPresent(def -> {
                    extractPropertiesActionsEventsFromCurrentModel(
                        def.getFirstIdentifier().toString(), featureName,
                        contextExtensionsList, propertiesList,
                        actionsList, eventsList
                    );
                });
            });
        });

        List<List<ThingModelElement>> result = new ArrayList<>();
        result.add(contextExtensionsList);
        result.add(propertiesList);
        result.add(actionsList);
        result.add(eventsList);
        return result;
    }

    /**
     * Extract some information from a Thing model.
     */
    private static void extractPropertiesActionsEventsFromCurrentModel(
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

            // Context extensions
            if (jsonObject.has("@context")) {
                JsonArray contextArray = jsonObject.getAsJsonArray("@context");
                for (JsonElement contextElement : contextArray) {
                    if (contextElement.isJsonObject()) {
                        JsonObject contextObj = contextElement.getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : contextObj.entrySet()) {
                            String alias = entry.getKey();
                            String contextUrl = entry.getValue().getAsString();
                            contextUrl = contextUrl.endsWith("#") ? contextUrl.substring(0, contextUrl.length() - 1) : contextUrl;
                            contextUrl = !contextUrl.endsWith("/") ? contextUrl + "/" : contextUrl;
                            addModelElement(contextExtensionsList, new ThingModelElement(alias, Optional.of(contextUrl), false));
                        }
                    }
                }
            }

            // Properties
            if (jsonObject.has("properties")) {
                JsonObject properties = jsonObject.getAsJsonObject("properties");
                for (String propertyKey : properties.keySet()) {
                    JsonObject property = properties.getAsJsonObject(propertyKey);
                    boolean isComplex = "object".equals(property.get("type").getAsString());
                    addModelElement(propertiesList, new ThingModelElement(propertyKey, Optional.of(featureName), isComplex));
                }
            }

            // Actions
            if (jsonObject.has("actions")) {
                JsonObject actions = jsonObject.getAsJsonObject("actions");
                for (String actionKey : actions.keySet()) {
                    addModelElement(actionsList, new ThingModelElement(actionKey, Optional.of(featureName), false));
                }
            }

            // Events
            if (jsonObject.has("events")) {
                JsonObject events = jsonObject.getAsJsonObject("events");
                for (String eventKey : events.keySet()) {
                    JsonObject event = events.getAsJsonObject(eventKey);
                    boolean isComplex = event.has("data") && "object".equals(event.getAsJsonObject("data").get("type").getAsString());
                    addModelElement(eventsList, new ThingModelElement(eventKey, Optional.of(featureName), isComplex));

                    // Payload for complex events
                    if (isComplex && event.has("data")) {
                        JsonObject data = event.getAsJsonObject("data");
                        if (data.has("properties")) {
                            JsonObject dataProperties = data.getAsJsonObject("properties");
                            for (String dataPropertyKey : dataProperties.keySet()) {
                                JsonObject dataProperty = dataProperties.getAsJsonObject(dataPropertyKey);
                                boolean dataIsComplex = "object".equals(dataProperty.get("type").getAsString());
                                String dataFullName = dataPropertyKey + " " + featureName;
                                addModelElement(eventsList, new ThingModelElement(dataFullName, Optional.of(featureName), dataIsComplex));
                            }
                        }
                    }
                }
            }

            // Submodels
            if (jsonObject.has("links")) {
                JsonArray links = jsonObject.getAsJsonArray("links");
                for (int i = 0; i < links.size(); i++) {
                    JsonObject link = links.get(i).getAsJsonObject();
                    if (link.has("rel")) {
                        String rel = link.get("rel").getAsString();
                        if ("tm:submodel".equals(rel)) {
                            String href = link.get("href").getAsString();
                            String instanceName = link.has("instanceName") ? link.get("instanceName").getAsString() : "";
                            extractPropertiesActionsEventsFromCurrentModel(href, instanceName, contextExtensionsList, propertiesList, actionsList, eventsList);
                        } else if ("tm:extends".equals(rel)) {
                            String href = link.get("href").getAsString();
                            extractPropertiesActionsEventsFromCurrentModel(href, featureName, contextExtensionsList, propertiesList, actionsList, eventsList);
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore durante il download o parsing del Thing Model: " + url);
            e.printStackTrace();
        }
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
