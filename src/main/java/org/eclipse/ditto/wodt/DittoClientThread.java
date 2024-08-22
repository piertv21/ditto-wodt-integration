package org.eclipse.ditto.wodt;

import java.util.concurrent.CountDownLatch;

import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DittoClientThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DittoClientThread.class);

    private static final String DITTO_THING_ID = "io.eclipseprojects.ditto:floor-lamp-0815";
    private final CountDownLatch latch = new CountDownLatch(1);
    private final DittoClient client;

    public DittoClientThread(DittoClient client) {
        super();
        this.client = client;
    }

    @Override
    public void run() {
        try {
            System.out.println("SO ATTIVO");

            // Ascolta i cambiamenti delle Thing
            client.twin().startConsumption().thenAccept(v -> {
                System.out.println("Subscribed for Twin events");
                client.twin().registerForThingChanges("my-changes", change -> {
                    if (change.getAction() == ChangeAction.CREATED) {
                        System.out.println("An existing Thing was modified: " + change.getThing());
                        // perform custom actions ..
                    }
                    System.out.println(change);
                });
            });

            // Ascolta i messaggi in arrivo
            client.live().startConsumption().thenAccept(v -> {
                System.out.println("Subscribed for live messages/commands/events");
                client.live().registerForMessage("globalMessageHandler", "hello.world", message -> {
                    System.out.println("Received Message with subject " + message.getSubject());
                    message.reply()
                        .httpStatus(HttpStatus.IM_A_TEAPOT)
                        .payload("Hello, I'm just a Teapot!")
                        .send();
                });
            });

            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.destroy();
        }
    }
    
    public void stop() {
        latch.countDown();
    }
}
