package org.eclipse.ditto.wodt.DTKGEngine.api;

/*
 * Copyright (c) 2023. Andrea Giulianelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.eclipse.ditto.wodt.model.ontology.Individual;
import org.eclipse.ditto.wodt.model.ontology.Node;
import org.eclipse.ditto.wodt.model.ontology.Property;

/**
 * This interface models the DTKGEngine component of the Abstract Architecture in a compatible way with Ditto.
*/
public interface DTKGEngine extends DTKGEngineReader {
    /**
     * Method that allows to signal the deletion or the stop of the underlying Digital Twin.
    */
    void removeDigitalTwin();

    /**
     * Add or update a Digital Twin property within the Digital Twin Knowledge Graph.
    * @param property the property to add/update
    * @param newValue the value of the property.
    */
    void addDigitalTwinPropertyUpdate(Property property, Node newValue);

    /**
     * Remove a Digital Twin property within the Digital Twin Knowledge Graph.
    * @param property the property to delete.
    * @return true if deleted, false if not-existent.
    */
    boolean removeProperty(Property property);

    /**
     * Add a relationship with another Digital Twin.
    * @param relationshipPredicate the associated predicate
    * @param targetIndividual the target individual
    */
    void addRelationship(Property relationshipPredicate, Individual targetIndividual);

    /**
     * Delete an existing relationship with another Digital Twin.
    * @param relationshipPredicate the associated predicate
    * @param targetIndividual the target individual.
    * @return true if correctly deleted, false if the relationship doesn't exist
    */
    boolean removeRelationship(Property relationshipPredicate, Individual targetIndividual);

    /**
     * Add an available action on the Digital Twin Knowledge Graph.
    * @param actionId the action identifier to identify the available action.
    */
    void addActionId(String actionId);

    /**
     * Remove an action from the Digital Twin Knowledge Graph.
    * @param actionId the action identifier to remove
    * @return true if correctly deleted, false if the action id doesn't exist
    */
    boolean removeActionId(String actionId);

    /**
     * Add a {@link DTKGObserver} that will be notified for each DTKG update.
    * @param observer the observer to add.
    */
    void addDTKGObserver(DTKGObserver observer);
}