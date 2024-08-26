package org.eclipse.ditto.wodt.DTKGEngine.impl;

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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shared.Lock;
import org.eclipse.ditto.wodt.DTKGEngine.api.DTKGEngine;
import org.eclipse.ditto.wodt.DTKGEngine.api.DTKGObserver;
import org.eclipse.ditto.wodt.model.ontology.BlankNode;
import org.eclipse.ditto.wodt.model.ontology.Individual;
import org.eclipse.ditto.wodt.model.ontology.Literal;
import org.eclipse.ditto.wodt.model.ontology.Node;
import org.eclipse.ditto.wodt.model.ontology.Property;
import org.eclipse.ditto.wodt.model.ontology.WoDTVocabulary;

/**
 * This class provides an implementation of the {@link io.github.webbasedwodt.application.component.DTKGEngine} using
* Apache Jena.
*/
public class JenaDTKGEngine implements DTKGEngine {
    private final Model dtkgModel;
    private final Resource digitalTwinResource;
    private final List<DTKGObserver> observers;

    /**
     * Default constructor.
    * @param digitalTwinUri the uri of the Digital Twin for which this class creates the DTKG
    */
    public JenaDTKGEngine(final String digitalTwinUri) {
        this.dtkgModel = ModelFactory.createDefaultModel();
        this.digitalTwinResource = this.dtkgModel.createResource(digitalTwinUri);
        this.observers = new ArrayList<>();
    }

    @Override
    public void removeDigitalTwin() {
        this.writeModel(Model::removeAll);
        this.notifyObservers();
    }

    @Override
    public void addDigitalTwinPropertyUpdate(final Property property, final Node newValue) {
        if (property.getUri().isPresent()) {
            this.writeModel(model -> {
                this.digitalTwinResource.removeAll(model.getProperty(property.getUri().get()));
                addProperty(this.digitalTwinResource, Pair.of(property, newValue));
            });
            this.notifyObservers();
        }
    }

    @Override
    public boolean removeProperty(final Property property) {
        if (property.getUri().isPresent()
                && this.digitalTwinResource.hasProperty(this.dtkgModel.getProperty(property.getUri().get()))) {
            this.writeModel(model ->
                this.digitalTwinResource.removeAll(model.getProperty(property.getUri().get()))
            );
            this.notifyObservers();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addRelationship(final Property relationshipPredicate, final Individual targetIndividual) {
        if (relationshipPredicate.getUri().isPresent()) {
            this.writeModel(model ->
                    addProperty(this.digitalTwinResource, Pair.of(relationshipPredicate, targetIndividual))
            );
            this.notifyObservers();
        }
    }

    @Override
    public boolean removeRelationship(final Property relationshipPredicate, final Individual targetIndividual) {
        if (relationshipPredicate.getUri().isPresent()
                && targetIndividual.getUri().isPresent()
                && this.digitalTwinResource.hasProperty(this.dtkgModel.getProperty(relationshipPredicate.getUri().get()))) {
            this.writeModel(model ->
                    model.remove(
                            this.digitalTwinResource,
                            this.dtkgModel.getProperty(relationshipPredicate.getUri().get()),
                            model.getResource(targetIndividual.getUri().get())
                    )
            );
            this.notifyObservers();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addActionId(final String actionId) {
        this.writeModel(model ->
                this.digitalTwinResource.addLiteral(
                        this.dtkgModel.createProperty(WoDTVocabulary.AVAILABLE_ACTION_ID.getUri()),
                        actionId
                )
        );
        this.notifyObservers();
    }

    @Override
    public boolean removeActionId(final String actionId) {
        if (this.dtkgModel.containsLiteral(
                this.digitalTwinResource,
                this.dtkgModel.getProperty(WoDTVocabulary.AVAILABLE_ACTION_ID.getUri()),
                actionId)
        ) {
            this.writeModel(model ->
                    model.remove(
                            this.digitalTwinResource,
                            model.getProperty(WoDTVocabulary.AVAILABLE_ACTION_ID.getUri()),
                            ResourceFactory.createStringLiteral(actionId)
                    )
            );
            return true;
        }
        return false;
    }

    @Override
    public String getCurrentDigitalTwinKnowledgeGraph() {
        try {
            this.dtkgModel.enterCriticalSection(Lock.READ);
            return RDFWriter.create().lang(Lang.TTL).source(this.dtkgModel).asString();
        } finally {
            this.dtkgModel.leaveCriticalSection();
        }
    }

    @Override
    public void addDTKGObserver(final DTKGObserver observer) {
        this.observers.add(observer);
    }

    private void notifyObservers() {
        final String currentDTKG = this.getCurrentDigitalTwinKnowledgeGraph();
        this.observers.forEach(observer -> observer.notifyNewDTKG(currentDTKG));
    }

    private void addProperty(final Resource resourceToAdd, final Pair<Property, Node> predicate) {
        final String propertyUri = predicate.getLeft().getUri().orElse("");
        final org.apache.jena.rdf.model.Property property = this.dtkgModel.createProperty(propertyUri); // TO DO potrebbe dare errore
        if (predicate.getRight() instanceof Property) {
            resourceToAdd.addProperty(
                    property,
                    dtkgModel.createProperty(((Property) predicate.getRight()).getUri().orElse(""))
            );
        } else if (predicate.getRight() instanceof BlankNode) {
            resourceToAdd.addProperty(
                    property,
                    addProperties(this.dtkgModel.createResource(), ((BlankNode) predicate.getRight()).getPredicates())
            );
        } else if (predicate.getRight() instanceof Literal<?>) {
            resourceToAdd.addLiteral(property, ((Literal<?>) predicate.getRight()).getValue());
        } else if (predicate.getRight() instanceof Individual) {
            resourceToAdd.addProperty(
                    property,
                    this.dtkgModel.createResource(((Individual) predicate.getRight()).getUri().orElse(""))
            );
        }
    }

    private Resource addProperties(final Resource resourceToAdd, final List<Pair<Property, Node>> predicates) {
        predicates.forEach(predicate -> addProperty(resourceToAdd, predicate));
        return resourceToAdd;
    }

    private void writeModel(final Consumer<Model> modelConsumer) {
        this.dtkgModel.enterCriticalSection(Lock.WRITE);
        modelConsumer.accept(this.dtkgModel);
        this.dtkgModel.leaveCriticalSection();
    }

    @Override
    public void addEvent(String event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeEvent(String event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}