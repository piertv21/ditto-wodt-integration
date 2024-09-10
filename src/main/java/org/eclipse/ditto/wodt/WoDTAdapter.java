package org.eclipse.ditto.wodt;

import java.net.URI;
import java.util.Set;

import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.WoDTDigitalAdapterConfiguration;
import org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl.WoDTDigitalAdapter;

/*
 * Application entry point.
 */
public class WoDTAdapter {

    private WoDTAdapter(String thingId, String yamlOntologyPath, String platformUrl, String physicalAssetId) {
        if (thingId == null || yamlOntologyPath == null || platformUrl == null || physicalAssetId == null) {
            throw new IllegalArgumentException("Ontology, Thing ID, physicalAssetId and Platform URL cannot be null");
        }

        new WoDTDigitalAdapter(
            new WoDTDigitalAdapterConfiguration(
                thingId,
                yamlOntologyPath,
                physicalAssetId,
                Set.of(URI.create(platformUrl))
            )
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
        String yamlOntologyPath,
        String platformUrl,
        String physicalAssetId
    ) {
        return new WoDTAdapter(thingId, yamlOntologyPath, platformUrl, physicalAssetId);
    }
    
}