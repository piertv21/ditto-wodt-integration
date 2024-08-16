package org.eclipse.ditto.wodt;

import java.util.Set;

import org.eclipse.ditto.wodt.DTDManager.impl.BulbHolderDTOntology;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;
import org.eclipse.ditto.wodt.common.DittoBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Application entry point.
 */
public final class Prova extends DittoBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(Prova.class);
    
    private WoDTDigitalAdapterConfiguration configuration;
    private WoDTDigitalAdapter digitalAdapter;

    private static final int DITTO_PORT_NUMBER = 3000;

    private Prova() {
        super();
        prova();
        terminate();
    }    

    public void prova() {
        this.configuration = new WoDTDigitalAdapterConfiguration(
            "http://localhost:" + DITTO_PORT_NUMBER,
            new BulbHolderDTOntology(),
            DITTO_PORT_NUMBER,
            "bulbHolderPA",
            Set.of()
        );
        
        this.digitalAdapter = new WoDTDigitalAdapter(
            "wodt-dt-adapter",
            this.configuration
        );
    }

    public static void main(String[] args) {
        new Prova();
    }
    
}