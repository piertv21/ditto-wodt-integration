package org.eclipse.ditto.wodt.common;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ditto.things.model.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/*
 * Utility class for Thing-related operations
 */
public class ThingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingUtils.class);
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Estrae proprietà, azioni ed eventi da una Thing e dai suoi submodels.
     * Output: Lista di liste. [0] = properties, [1] = actions, [2] = events.
     */
    public static List<List<ThingModelElement>> extractPropertiesActionsEvents(Thing thing) {
        List<ThingModelElement> propertiesList = new ArrayList<>();
        List<ThingModelElement> actionsList = new ArrayList<>();
        List<ThingModelElement> eventsList = new ArrayList<>();

        // Proprietà, azioni ed eventi della Thing corrente
        extractPropertiesActionsEventsFromModel(thing.getDefinition().get().toString(), "", propertiesList, actionsList, eventsList);

        // Submodels dalle feature della Thing
        thing.getFeatures().ifPresent(features -> {
            features.forEach((feature) -> {
                String featureName = feature.getId();
                feature.getDefinition().ifPresent(def -> {
                    extractPropertiesActionsEventsFromModel(def.getFirstIdentifier().toString(), featureName, propertiesList, actionsList, eventsList);
                });
            });
        });

        List<List<ThingModelElement>> result = new ArrayList<>();
        result.add(propertiesList);
        result.add(actionsList);
        result.add(eventsList);
        return result;
    }

    /**
     * Estrae proprietà, azioni ed eventi da un Thing Model.
     */
    private static void extractPropertiesActionsEventsFromModel(
            String url, String featureName,
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

            // Proprietà
            if (jsonObject.has("properties")) {
                JsonObject properties = jsonObject.getAsJsonObject("properties");
                for (String propertyKey : properties.keySet()) {
                    JsonObject property = properties.getAsJsonObject(propertyKey);
                    boolean isComplex = "object".equals(property.get("type").getAsString());
                    addModelElement(propertiesList, new ThingModelElement(propertyKey, featureName, isComplex));
                }
            }

            // Azioni
            if (jsonObject.has("actions")) {
                JsonObject actions = jsonObject.getAsJsonObject("actions");
                for (String actionKey : actions.keySet()) {
                    addModelElement(actionsList, new ThingModelElement(actionKey, featureName, false));
                }
            }

            // Eventi
            if (jsonObject.has("events")) {
                JsonObject events = jsonObject.getAsJsonObject("events");
                for (String eventKey : events.keySet()) {
                    JsonObject event = events.getAsJsonObject(eventKey);
                    boolean isComplex = event.has("data") && "object".equals(event.getAsJsonObject("data").get("type").getAsString());
                    addModelElement(eventsList, new ThingModelElement(eventKey, featureName, isComplex));

                    // Gestione del payload dell'evento se complesso
                    if (isComplex && event.has("data")) {
                        JsonObject data = event.getAsJsonObject("data");
                        if (data.has("properties")) {
                            JsonObject dataProperties = data.getAsJsonObject("properties");
                            for (String dataPropertyKey : dataProperties.keySet()) {
                                JsonObject dataProperty = dataProperties.getAsJsonObject(dataPropertyKey);
                                boolean dataIsComplex = "object".equals(dataProperty.get("type").getAsString());
                                String dataFullName = dataPropertyKey + " " + featureName;
                                addModelElement(eventsList, new ThingModelElement(dataFullName, featureName, dataIsComplex));
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
                            extractPropertiesActionsEventsFromModel(href, instanceName, propertiesList, actionsList, eventsList);
                        } else if ("tm:extends".equals(rel)) {
                            String href = link.get("href").getAsString();
                            extractPropertiesActionsEventsFromModel(href, featureName, propertiesList, actionsList, eventsList);
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
     * Add a model element to the list if it is not already present, so that duplicates are avoided
     * and the order is preserved.
     */
    private static void addModelElement(List<ThingModelElement> list, ThingModelElement element) {
        if (!list.contains(element)) {
            list.add(element);
        }
    }
}
