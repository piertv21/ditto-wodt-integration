package org.eclipse.ditto.wodt;

import org.eclipse.ditto.wodt.ontologies.FloorLampDTOntology;
import org.junit.jupiter.api.Test;

public class WoDTAdapterTest {

    private static final int TEST_WAIT_TIME = 100000;

    @Test
    void testAppInitialization() throws InterruptedException {
        WoDTAdapter woDTAdapter = WoDTAdapter.create(
            "io.eclipseprojects.ditto:bulb-holder",
            new FloorLampDTOntology(),
            "platformUrl",
            "physicalAssetId"
        );

        Thread.sleep(TEST_WAIT_TIME);
    }

}