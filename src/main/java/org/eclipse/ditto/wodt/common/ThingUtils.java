package org.eclipse.ditto.wodt.common;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /*
     * Extract properties, actions, and events from a Thing and its submodels.
     * Output: List of Sets. [0] = properties, [1] = actions, [2] = events
     */
    public static List<Set<Pair<String, String>>> extractPropertiesActionsEvents(Thing thing) {
        Set<Pair<String, String>> propertiesSet = new HashSet<>();
        Set<Pair<String, String>> actionsSet = new HashSet<>();
        Set<Pair<String, String>> eventsSet = new HashSet<>();

        // Current Thing properties, actions, and events
        extractPropertiesActionsEventsFromModel(thing.getDefinition().get().toString(), "", propertiesSet, actionsSet, eventsSet);

        // Submodels from Thing's features
        thing.getFeatures().ifPresent(features -> {
            features.forEach((feature) -> {
                String subThingName = feature.getId();
                feature.getDefinition().ifPresent(def -> {
                    extractPropertiesActionsEventsFromModel(def.getFirstIdentifier().toString(), subThingName, propertiesSet, actionsSet, eventsSet);
                });
            });
        });
        
        List<Set<Pair<String, String>>> result = new ArrayList<>();
        result.add(propertiesSet);
        result.add(actionsSet);
        result.add(eventsSet);
        return result;
    }

    /*
     * Extract properties, actions, and events from a Thing Model
     */
    private static void extractPropertiesActionsEventsFromModel(
        String url, String subThingName,
        Set<Pair<String, String>> propertiesSet,
        Set<Pair<String, String>> actionsSet,
        Set<Pair<String, String>> eventsSet
    ) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

            // Properties
            if (jsonObject.has("properties")) {
                JsonObject properties = jsonObject.getAsJsonObject("properties");
                for (String propertyKey : properties.keySet()) {
                    propertiesSet.add(new Pair<>(propertyKey, subThingName));
                }
            }

            // Actions
            if (jsonObject.has("actions")) {
                JsonObject actions = jsonObject.getAsJsonObject("actions");
                for (String actionKey : actions.keySet()) {
                    actionsSet.add(new Pair<>(actionKey, subThingName));
                }
            }

            // Events
            if (jsonObject.has("events")) {
                JsonObject events = jsonObject.getAsJsonObject("events");
                for (String eventKey : events.keySet()) {
                    eventsSet.add(new Pair<>(eventKey, subThingName));
                }
            }

            // Submodels
            if (jsonObject.has("links")) {
                JsonArray links = jsonObject.getAsJsonArray("links");
                for (int i = 0; i < links.size(); i++) {
                    JsonObject link = links.get(i).getAsJsonObject();
                    if (link.has("rel") && "tm:submodel".equals(link.get("rel").getAsString())) {
                        String href = link.get("href").getAsString();
                        String instanceName = link.has("instanceName") ? link.get("instanceName").getAsString() : "";
                        extractPropertiesActionsEventsFromModel(href, instanceName, propertiesSet, actionsSet, eventsSet);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore durante il download o parsing del Thing Model: " + url);
            e.printStackTrace();
        }
    }

    /*
     * Get all Thing Model URLs from a Thing and its submodels
     */
    public static Set<String> getThingModelUrls(Thing thing) { // TO DO: actually only 1° and 2° level thing definitions are retrieved
        Set<String> urlSet = new HashSet<>();

        // Current Thing definition
        urlSet.add(thing.getDefinition().get().toString());

        // Thing features definition
        thing.getFeatures().ifPresent(features -> {
            features.forEach((feature) -> {
                feature.getDefinition().ifPresent(def -> urlSet.add(def.getFirstIdentifier().toString()));
            });
        });
        
        // Every feature definition
        /*Set<String> expandedUrls = new HashSet<>(urlSet);
        for (String url : urlSet) {
            expandThingModelUrls(url, expandedUrls);
        }*/

        return urlSet;
    }

    /*
     * Expand Thing Model URLs recursively
     */
    private static void expandThingModelUrls(String url, Set<String> urlSet) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();    
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            urlSet.add(url);            
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            
            if (jsonObject.has("links")) {
                JsonArray links = jsonObject.getAsJsonArray("links");
                for (int i = 0; i < links.size(); i++) {
                    JsonObject link = links.get(i).getAsJsonObject();
                    if (link.has("rel") && "tm:submodel".equals(link.get("rel").getAsString())) {
                        String href = link.get("href").getAsString();
                        urlSet.add(href);
                        expandThingModelUrls(href, urlSet);
                    }
                }
            }

            if (jsonObject.has("actions")) {
                JsonObject actions = jsonObject.getAsJsonObject("actions");
                for (String actionKey : actions.keySet()) {
                    JsonObject action = actions.getAsJsonObject(actionKey);
                    if (action.has("tm:ref")) {
                        String refUrl = action.get("tm:ref").getAsString();
                        expandThingModelUrls(refUrl, urlSet);
                    }
                }
            }    
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Errore durante il download o parsing del Thing Model: {}", url, e);
        }
    }
}
