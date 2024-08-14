package org.eclipse.ditto.wodt;

import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.wodt.common.DittoBase;
import static org.eclipse.ditto.wodt.common.ThingUtils.extractPropertiesActionsEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Prova extends DittoBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(Prova.class);

    private Prova() {
        super();
        prova();
        terminate();
    }

    public static void main(String[] args) {
        new Prova();
    }

    public void prova() {
        // Ditto thing
        Thing thing = client.twin().forId(ThingId.of("io.eclipseprojects.ditto:bulb-holder"))
            .retrieve()
            .toCompletableFuture()
            .join();
        
        //getThingModelUrls(thing).forEach(System.out::println);
        extractPropertiesActionsEvents(thing).get(0).forEach(System.out::println);
    }
    
}