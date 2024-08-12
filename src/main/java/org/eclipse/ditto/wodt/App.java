package org.eclipse.ditto.wodt;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.wodt.common.ExamplesBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Module entrypoint
 */
public class App extends ExamplesBase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private final CountDownLatch countDownLatch;

    private App() {
        super();
        this.countDownLatch = new CountDownLatch(2);

        try {
            registerForThingChanges(client1);
            startConsumeChanges(client1);
        } finally {
            destroy();
        }
    }

    public static void main(final String... args) {
        new App();
    }

    /**
     * Register for all {@code ThingChange}s.
     */
    private void registerForThingChanges(final DittoClient client) {
        Thing thing = client.twin().forId(
            ThingId.of("io.eclipseprojects.ditto:bulb")
        ).retrieve().toCompletableFuture().join();

        System.out.println(
            //thing.getAttributes().get().getField("manufacturer").get().getValue().asString()
            thing.toJsonString()
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
    }
    
}
