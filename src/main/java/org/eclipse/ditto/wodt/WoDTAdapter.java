package org.eclipse.ditto.wodt;

import java.net.URI;
import java.util.Set;

import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;
import org.eclipse.ditto.wodt.model.ontology.DTOntology;

/*
 * Application entry point.
 */
public class WoDTAdapter {

    private WoDTAdapter(String thingId, DTOntology ontology, String platformUrl, String physicalAssetId) {
        if (ontology == null || thingId == null || platformUrl == null || physicalAssetId == null) {
            throw new IllegalArgumentException("Ontology, Thing ID, physicalAssetId and Platform URL cannot be null");
        }

        new WoDTDigitalAdapter(
            "wodt-dt-adapter",
            new WoDTDigitalAdapterConfiguration(
                    ontology,
                    physicalAssetId,
                    Set.of(URI.create(platformUrl))
            ),
            thingId
        );
    }

    /**
     * Factory method to create an instance of App.
     * 
     * @param ontology the ontology to use.
     * @param thingId the ID of the Ditto thing.
     * @param platformUrl the URL of the platform.
     * @return a new instance of App.
     */
    public static WoDTAdapter create(
        String thingId,
        DTOntology ontology,
        String platformUrl,
        String physicalAssetId
    ) {
        return new WoDTAdapter(thingId, ontology, platformUrl, physicalAssetId);
    }
    
}