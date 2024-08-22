package org.eclipse.ditto.wodt;

import java.net.URI;
import java.util.Set;

import org.eclipse.ditto.things.model.Thing;
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
        // Crea un'istanza del DittoClientThread
        DittoClientThread dittoClientRunnable = new DittoClientThread(client);
        Thread dittoClientThread = new Thread(dittoClientRunnable);
        dittoClientThread.start();

        // Ora puoi avviare il server Javalin o fare altre operazioni dipendenti dal client Ditto
        this.digitalAdapter = new WoDTDigitalAdapter(
            "wodt-dt-adapter",
            new WoDTDigitalAdapterConfiguration(
                "http://localhost:" + MODULE_PORT_NUMBER,
                new BulbHolderDTOntology(),
                MODULE_PORT_NUMBER,
                "bulbHolderPA",
                Set.of(URI.create("http://localhost:5000/"))
            )
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
        // ottiene tutti gli attributi di una thing
        thing.getAttributes().ifPresent(attributes -> {
            attributes.forEach((attribute) -> {
                System.out.println(attribute.getKey() + ": " + attribute.getValue().toString());
            });
        });

        // ottiene tutte le proprietà di tutte le features di una thing
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
        
        // registra cambiamenti
        client.twin().forId(ThingId.of("io.eclipseprojects.ditto:bulb-holder")).registerForThingChanges("my-changes", change -> {
            System.out.println("Change received: " + change);
        });
        
        // accesso a attributi e proprietà di features
        System.out.println(
            // get attributo
            //thing.getAttributes().get().getField("manufacturer").get().getValue().asString()

            // get proprietà featureName
            //thing.getFeatures().get().getFeature("Bulb").get().getProperties().get().getField("on").get().getValue().toString()            
        );
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