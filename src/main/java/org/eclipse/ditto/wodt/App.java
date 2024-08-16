package org.eclipse.ditto.wodt;

import java.net.URI;
import java.util.Set;

import org.eclipse.ditto.wodt.DTDManager.impl.BulbHolderDTOntology;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;
import org.eclipse.ditto.wodt.common.DittoBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Application entry point.
 */
public final class App extends DittoBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    
    private WoDTDigitalAdapterConfiguration configuration;
    private WoDTDigitalAdapter digitalAdapter;

    private static final int DITTO_PORT_NUMBER = 3000;

    private App() {
        super();
        prova();
        terminate();
    }    

    public void prova() {
        this.configuration = new WoDTDigitalAdapterConfiguration(
            "http://localhost:" + DITTO_PORT_NUMBER,
            new BulbHolderDTOntology(),
            DITTO_PORT_NUMBER,
            "bulbHolderPA",
            Set.of(URI.create("http://localhost:8979/api/1/things/bulbHolderPA"))
        );
        
        this.digitalAdapter = new WoDTDigitalAdapter(
            "wodt-dt-adapter",
            this.configuration
        );
    }

    public static void main(String[] args) {
        new App();
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
            features.forEach((feature) -> {
                System.out.println("Feature: " + feature.getId());
                feature.getProperties().ifPresent(properties -> {
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
            features.forEach((feature) -> {
                System.out.println(feature.getId() + ": " + feature.getDefinition().get().getFirstIdentifier() + "\n");                
            });
        });

        client.twin().forId(ThingId.of("io.eclipseprojects.ditto:bulb-holder")).registerForThingChanges("my-changes", change -> {
            System.out.println("Change received: " + change);
        });
        

        System.out.println(
            // get attributo
            //thing.getAttributes().get().getField("manufacturer").get().getValue().asString()

            // get proprietà feature
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