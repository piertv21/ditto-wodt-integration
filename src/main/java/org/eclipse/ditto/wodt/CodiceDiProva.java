package org.eclipse.ditto.wodt;

public class CodiceDiProva {
    
    // (EVENTI) SUBSCRIBE PER RICEZIONE NUOVI MESSAGGI (DENTRO TRY CATCH) USARE STESSO SUBJECT PER RISPONDERE
    /*client.live().startConsumption().toCompletableFuture().get(); // this will block the thread! work asynchronously whenever possible!
    System.out.println("Subscribed for live messages/commands/events");

    client.live().registerForMessage("globalMessageHandler", "hello.world", message -> {
        System.out.println("Received Message with subject " +  message.getSubject());
        message.reply()
            .httpStatus(HttpStatus.IM_A_TEAPOT)
            .payload("Hello, I'm just a Teapot!")
            .send();
        });
    
    latch.await();*/
    
    // (AZIONI) INVIO MESSAGGI AD UNA THING
    /*client.live().forId("org.eclipse.ditto:new-thing")
        .message()
        .from()
        .subject("hello.world")
        .payload("I am a Teapot")
        .send(String.class, (response, throwable) ->
            System.out.println("Got response: " + response.getPayload().orElse(null))
        );*/
    
}