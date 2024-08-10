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

package io.github.webbasedwodt.model.ontology;

/**
 * It models the concept of RDF Literal.
 * @param <T> the type of the literal.
 */
public final class Literal<T> implements Node {
    private final T value;

    /**
     * Default constructor.
     * @param value the value of the literal
     */
    public Literal(final T value) {
        this.value = value;
    }

    /**
     * Getter.
     * @return the literal
     */
    public T getValue() {
        return this.value;
    }
}
