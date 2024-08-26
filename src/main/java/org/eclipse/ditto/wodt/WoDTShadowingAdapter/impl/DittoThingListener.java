package org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl;

import java.util.concurrent.CountDownLatch;

import org.eclipse.ditto.wodt.common.DittoBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class hanlde a Ditto Client that listen to Thing changes and messages.
 */
public class DittoThingListener extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(DittoThingListener.class);
    
    private final CountDownLatch latch = new CountDownLatch(1);
    private final DittoBase client;
    private final WoDTDigitalAdapter woDTDigitalAdapter;

    public DittoThingListener(WoDTDigitalAdapter woDTDigitalAdapter) {
        super();
        this.woDTDigitalAdapter = woDTDigitalAdapter;
        this.client = new DittoBase();
    }

    @Override
    public void run() {
        try {
            // Listen Ditto Thing changes
            client.getClient().twin().startConsumption().thenAccept(v -> {
                LOGGER.info("Subscribed for Ditto Thing changes");
                client.getClient().twin().registerForThingChanges("my-changes", change -> {
                    LOGGER.info("Received Thing change");
                    this.woDTDigitalAdapter.onThingChange(change);
                });
            });

            // Listen all messages TO thing (Actions)
            /*client.getClient().live().startConsumption().thenAccept(v -> {
                System.out.println("Subscribed for live messages/commands/events");
                client.getClient().live().registerForMessage("globalMessageHandler", "*", new Consumer<RepliableMessage<?, Object>>() {
                    @Override
                    public void accept(RepliableMessage<?, Object> message) {
                        System.out.println("Received Message with subject " + message.getSubject());
                        
                        //this.woDTDigitalAdapter.onMessage(message);
                        
                        message.reply()
                                .httpStatus(HttpStatus.IM_A_TEAPOT)
                                .payload("Hello, I'm just a Teapot!")
                                .send();
                    }
                });
            });*/

            // Keep the thread alive until stopThread() is called
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.getClient().destroy();
            woDTDigitalAdapter.stopAdapter();
        }
    }
    
    public void stopThread() {
        latch.countDown();
    }
}
