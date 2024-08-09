package org.eclipse.ditto.wodt;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.wodt.common.ExamplesBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RegisterForChanges extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterForChanges.class);

    private final CountDownLatch countDownLatch;

    private RegisterForChanges() {
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
        new RegisterForChanges();
    }

    /**
     * Register for all {@code ThingChange}s.
     */
    private void registerForThingChanges(final DittoClient client) {
        client.twin().search()
            .stream(queryBuilder -> queryBuilder.namespace("org.eclipse.ditto")
                .options(builder -> builder.sort(s -> s.desc("thingId")))
            )
            .forEach(foundThing -> System.out.println("Found thing: " + foundThing));
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