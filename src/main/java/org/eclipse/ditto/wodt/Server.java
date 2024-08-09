package org.eclipse.ditto.wodt;

import io.github.sanecity.wot.DefaultWot;
import io.github.sanecity.wot.Wot;
import io.github.sanecity.wot.WotException;
import io.github.sanecity.wot.thing.ExposedThing;
import io.github.sanecity.wot.thing.Thing;
import io.github.sanecity.wot.thing.form.Form;
import io.github.sanecity.wot.thing.form.Operation;
import io.github.sanecity.wot.thing.property.ThingProperty;

/*
 * Module entrypoint
 */
public class Server {
    
    public static void main(String[] args) throws WotException {
        Wot wot = new DefaultWot();

        Thing thing;
        thing = new Thing.Builder()
                .setId("counter")
                .setTitle("My Counter")
                .setDescription("This is a simple counter thing")
                .addForm(
                        new Form.Builder()
                                .addOp(Operation.OBSERVE_PROPERTY)
                                .setHref("ws://localhost:3650/dtkg")
                                .setSubprotocol("websocket")
                                .build()
                )
                .build();



        ExposedThing exposedThing = wot.produce(thing);
        exposedThing.addProperty("count", new ThingProperty.Builder()
                        .setType("integer")
                        .setDescription("current counter value")
                        .setObservable(true)
                        .setReadOnly(true)
                        .build(),
            42);

            System.out.println(exposedThing.toJson());

        exposedThing.expose();
        wot.destroy();
    }
    
}
