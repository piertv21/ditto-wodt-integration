package org.eclipse.ditto.wodt;

import java.util.List;

import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;
import org.eclipse.ditto.wodt.common.DittoBase;
import org.eclipse.ditto.wodt.common.ThingModelElement;
import static org.eclipse.ditto.wodt.common.ThingUtils.extractPropertiesActionsEventsFromThing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Application entry point.
 */
public final class App extends DittoBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    
    private WoDTDigitalAdapterConfiguration configuration;
    private WoDTDigitalAdapter digitalAdapter;

    private static final int MODULE_PORT_NUMBER = 3000;
    private static final String DITTO_THING_ID = "io.eclipseprojects.ditto:floor-lamp-0815";
    private Thing thing;

    private App() {
        super();
        init();
        terminate();
    }    

    public void init() {
        this.thing = client.twin().forId(ThingId.of(DITTO_THING_ID))
            .retrieve().toCompletableFuture().join();

        /*System.out.println(
            this.thing.getFeatures().get().getFeature("Spot1")
            .get().getProperties().get().getField("color").get().getValue().toString()
        );*/

        List<List<ThingModelElement>> res = extractPropertiesActionsEventsFromThing(this.thing);

        System.out.println("Properties:");
        res.get(0).forEach(System.out::println);
        System.out.println("\nActions:");
        res.get(1).forEach(System.out::println);
        System.out.println("\nEvents:");
        res.get(2).forEach(System.out::println);

        // valutare threadizzazione

        /*syncThing(this.thing);

        client.twin().forId(ThingId.of(DITTO_THING_ID)).registerForThingChanges("my-changes", change -> {
            System.out.println("Change received: " + change);
        });

        System.out.println("sdf");
        startConsumeChanges(client);

        this.configuration = new WoDTDigitalAdapterConfiguration(
            "http://localhost:" + MODULE_PORT_NUMBER,
            new BulbHolderDTOntology(),
            MODULE_PORT_NUMBER,
            "bulbHolderPA",
            Set.of(URI.create("http://localhost:5000/"))
        );
        
        this.digitalAdapter = new WoDTDigitalAdapter(
            "wodt-dt-adapter",
            this.configuration
        );*/
    }

    public static void main(String[] args) {
        new App();
    }

    private void syncThing(Thing thing) {
        // Stampa la definizione della Thing
        LOGGER.info("Thing definition: {}", thing.getDefinition().get().toString());

        // Itera su tutte le features e stampa la loro definizione
        thing.getFeatures().ifPresent(features -> {
            features.forEach((feature) -> {
                LOGGER.info("Feature: {}", feature.getId());
                LOGGER.info("Feature definition: {}", feature.getDefinition().get().getFirstIdentifier());
            });
        });        
    }


    

    /*
    private App() {
        super();
        this.countDownLatch = new CountDownLatch(2);

        try {
            registerForThingChanges(client);
            startConsumeChanges(client);
        } finally {
            destroy();
        }
    }

    public static void main(final String... args) {
        new App();
    }

    private void registerForThingChanges(final DittoClient client) {
        Thing thing = client.twin().forId(
            ThingId.of("io.eclipseprojects.ditto:floor-lamp-0815")
        ).retrieve().toCompletableFuture().join();

        thing.getAttributes().ifPresent(attributes -> {
            attributes.forEach((attribute) -> {
                System.out.println(attribute.getKey() + ": " + attribute.getValue().toString());
            });
        });

        thing.getFeatures().ifPresent(features -> {
            features.forEach((featureName) -> {
                System.out.println("Feature: " + featureName.getId());
                featureName.getProperties().ifPresent(properties -> {
                    properties.forEach((property) -> {
                        System.out.println(property.getKey() + ": " + property.getValue().toString());
                    });
                });
                System.out.println("\n");
            });
        });

        
        // Stampa la definizione della Thing
        System.out.println("Definition" + thing.getDefinition().get().toString());

        // Itera su tutte le features e stampa la loro definizione
        thing.getFeatures().ifPresent(features -> {
            features.forEach((featureName) -> {
                System.out.println(featureName.getId() + ": " + featureName.getDefinition().get().getFirstIdentifier() + "\n");                
            });
        });

        client.twin().forId(ThingId.of("io.eclipseprojects.ditto:bulb-holder")).registerForThingChanges("my-changes", change -> {
            System.out.println("Change received: " + change);
        });
        

        System.out.println(
            // get attributo
            //thing.getAttributes().get().getField("manufacturer").get().getValue().asString()

            // get proprietà featureName
            //thing.getFeatures().get().getFeature("Bulb").get().getProperties().get().getField("on").get().getValue().toString()

            // get intera thing
            // Recupera tutte le features
            
        );

        final HttpClient httpClient = HttpClient.newHttpClient();

        String auth = "ditto:ditto";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://gist.githubusercontent.com/piertv21/2418ee82f6d6266be9b1aee537ee05e1/raw/3e46816a89267b599d2009d038977ebdd715bec1/bulb-1.0.0.tm.jsonld"))
            .header("Accept", "application/td+json")
            .header("Authorization", "Basic " + encodedAuth)
            .build();
            
        // Invia la richiesta in modo asincrono e stampa la risposta
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(System.out::println)
            .join();  // Attende il completamento della richiesta
            
        //getThingModelUrls(thing).forEach(System.out::println);
        extractPropertiesActionsEvents(thing).get(2).forEach(System.out::println);
            
        
    }

    private void destroy() {
        final boolean allMessagesReceived;
        try {
            allMessagesReceived = countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.info("All changes received: {}", allMessagesReceived);
        terminate();
    }*/
    
}