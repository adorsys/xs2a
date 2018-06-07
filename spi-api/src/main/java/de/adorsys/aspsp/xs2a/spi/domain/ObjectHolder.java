/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.spi.domain;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class ObjectHolder <K, V>{
    private Map<K, V> values = new HashMap<>();

    public ObjectHolder<K, V> addValue(K key, V value){
        values.put(key, value);
        return this;
    }
}
