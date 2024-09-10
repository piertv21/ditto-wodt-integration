package org.eclipse.ditto.wodt.DTDManager.api;

import java.util.List;

import org.eclipse.ditto.wodt.common.ThingModelElement;

/*
 * Get the available properties, relationships, actions, and events of the digital twin.
 * 
 * These data are obtained combining the information from the ThingModel and the Ontology mapping,
 * prioritizing the Ontology mapping.
 */
public interface OntologyManager {

    /*
     * Get the available context extensions of the digital twin.
     */
    List<ThingModelElement> getAvailableContextExtensions();

    /*
     * Get the type of the digital twin.
     */
    List<ThingModelElement> getAvailableProperties();
    
    /*
     * Get the available properties of the digital twin.
     */
    List<ThingModelElement> getAvailableRelationships();
    
    /*
     * Get the available relationships of the digital twin.
     */
    List<ThingModelElement> getAvailableActions();

    /*
     * Get the available actions of the digital twin.
     */
    List<ThingModelElement> getAvailableEvents();
}
