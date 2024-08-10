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

package org.eclipse.ditto.wodt.DTDManager.api;

import java.util.Set;

import io.github.sanecity.wot.thing.Thing;

/**
 * Reader part of the DTDManager component of the Abstract Architecture -- for ISP.
*/
public interface DTDManagerReader {
    /**
     * Get the current available actions.
    * @return the available action ids.
    */
    Set<String> getAvailableActionIds();

    /**
     * Obtain the Digital Twin Descriptor.
    * @return Digital Twin Descriptor implemented with a Thing Description
    */
    Thing<?, ?, ?> getDTD();
}