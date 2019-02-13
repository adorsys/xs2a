/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.repository;

import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;

/**
 * This is proxy CRUD repository that provides compatibility between spring-data 1.x and spring-data 2.x
 *
 * Method {@link org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)} was deleted spring-data version starting 2.x
 *
 */
public interface Xs2aCrudRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {}
