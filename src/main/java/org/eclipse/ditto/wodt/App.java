package org.eclipse.ditto.wodt;

import java.net.URI;
import java.util.Set;

import org.eclipse.ditto.wodt.DTDManager.impl.FloorLampDTOntology;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;

/*
 * Application entry point.
 */
public final class App {

    private static final String DITTO_THING_ID = "io.eclipseprojects.ditto:floor-lamp-0815";
    private static final String PLATFORM_URL = "http://localhost:5000/";
    
    private App() {
        new WoDTDigitalAdapter(
            "wodt-dt-adapter",
            new WoDTDigitalAdapterConfiguration(
                new FloorLampDTOntology(),
                "bulbHolderPA",
                Set.of(URI.create(PLATFORM_URL))
            ),
            DITTO_THING_ID
        );
    }

    public static void main(String[] args) {
        new App();
    }
    
}