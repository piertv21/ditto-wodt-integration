package org.eclipse.ditto.wodt;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.base.model.auth.AuthorizationSubject;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.management.ThingHandle;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.wodt.common.ExamplesBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RegisterForChanges extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterForChanges.class);

    private final CountDownLatch countDownLatch;
    private final ThingId thingId = randomThingId();

    private RegisterForChanges() {
        super();
        this.countDownLatch = new CountDownLatch(2);

        try {
            registerForThingChanges(client1);
            //registerForThingChangesWithDeregistration(client1);

            startConsumeChanges(client1);

            //createThing(client2, authorizationSubject);
        } finally {
            destroy();
        }
    }

    public static void main(final String... args) {
        new RegisterForChanges();
    }

    private static String registrationId() {
        return "registration:" + UUID.randomUUID();
    }

    /**
     * Register for all {@code ThingChange}s.
     */
    private void registerForThingChanges(final DittoClient client) {
        final ThingHandle thingHandle = client.twin().forId(thingId);

        client.twin().registerForThingChanges(registrationId(), change -> {
            LOGGER.info("For all things: ThingChange received: {}", change);
            countDownLatch.countDown();
        });

        thingHandle.registerForThingChanges(registrationId(),
                change -> LOGGER.info("My Thing: ThingChange received: {}", change));
    }

    /**
     * Register for {@code ThingChange}s and deregister after the created-event has been retrieved.
     */
    private void registerForThingChangesWithDeregistration(final DittoClient client) {
        final ThingHandle thingHandle = client.twin().forId(thingId);

        final String registrationId = registrationId();
        LOGGER.info("RegistrationId: {}", registrationId);

        thingHandle.registerForThingChanges(registrationId, change -> {
            LOGGER.info("{}: ThingChange received: {}", thingId, change);

            /* Deregister when the created-event has been retrieved */
            if (change.getAction() == ChangeAction.CREATED) {
                LOGGER.info("{}: Deregister handler with id: {}", thingId, registrationId);
                thingHandle.deregister(registrationId);
                countDownLatch.countDown();
            }
        });
    }

    private void createThing(final DittoClient client, final AuthorizationSubject... subjects) {
        LOGGER.info("Create thing {} and set required permissions.", thingId);

        final Thing thing = Thing.newBuilder()
                .setId(thingId)
                .build();

        try {
            client.twin().create(thing)
                    .thenCompose(createdThing -> {
                        final Thing updatedThing = createdThing.toBuilder()
                                .setAttribute(JsonPointer.of("foo"), JsonValue.of("bar"))
                                .build();
                        return client.twin().update(updatedThing);
                    }).toCompletableFuture()
                    .get(10, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException(e);
        }
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