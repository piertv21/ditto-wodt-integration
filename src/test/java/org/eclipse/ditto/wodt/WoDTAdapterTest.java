package org.eclipse.ditto.wodt;

import org.eclipse.ditto.wodt.ontologies.FloorLampDTOntology;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class WoDTAdapterTest {

    private static final long TEST_WAIT_TIME = 9223372036854775807L;

    @Test
    void testAppInitialization() throws InterruptedException {
        WoDTAdapter woDTAdapter = WoDTAdapter.create(
                "io.eclipseprojects.ditto:floor-lamp-0815",
                new FloorLampDTOntology(),
                "http://localhost:5000",
                "bulbHolderId"
        );

        assertNotNull(woDTAdapter);
        Thread.sleep(TEST_WAIT_TIME);
    }

}