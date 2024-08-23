package org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl;

import java.util.concurrent.CountDownLatch;

import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.wodt.common.DittoBase;

/*
 * This class hanlde a Ditto Client that listen to Thing changes and messages.
 */
public class DittoClientThread extends Thread {

    private static final String DITTO_THING_ID = "io.eclipseprojects.ditto:floor-lamp-0815";
    private final CountDownLatch latch = new CountDownLatch(1);
    private final DittoBase client;
    private final WoDTDigitalAdapter woDTDigitalAdapter;

    public DittoClientThread(WoDTDigitalAdapter woDTDigitalAdapter) {
        super();
        this.woDTDigitalAdapter = woDTDigitalAdapter;
        this.client = new DittoBase();
    }

    @Override
    public void run() {
        try {
            // Ascolta i cambiamenti delle Thing
            client.getClient().twin().startConsumption().thenAccept(v -> {
                System.out.println("Subscribed for Twin events");
                client.getClient().twin().registerForThingChanges("my-changes", change -> {
                    this.woDTDigitalAdapter.onThingChange(change);
                    System.out.println("Received Thing change");
                });
            });

            // Ascolta i messaggi in arrivo
            client.getClient().live().startConsumption().thenAccept(v -> {
                System.out.println("Subscribed for live messages/commands/events");
                client.getClient().live().registerForMessage("globalMessageHandler", "hello.world", message -> {
                    System.out.println("Received Message with subject " + message.getSubject());
                    
                    this.woDTDigitalAdapter.onMessage(message);
                    
                    message.reply()
                        .httpStatus(HttpStatus.IM_A_TEAPOT)
                        .payload("Hello, I'm just a Teapot!")
                        .send();
                });
            });

            // Mantieni il thread in esecuzione fino a quando `stop()` non viene chiamato
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.getClient().destroy();
        }
    }
    
    public void stopThread() {
        latch.countDown();
    }
}
