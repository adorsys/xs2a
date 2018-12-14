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

package de.adorsys.psd2.consent.repository.specification;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

/**
 * This is a class for providing Spring Data Jpa Specification for different entities attributes
 */
public class EntityAttributeSpecificationProvider {
    private EntityAttributeSpecificationProvider(){}

    public static <T> Specification<T> provideSpecificationForEntityAttribute(String attribute, String value) {
        return  (root, query, cb) -> {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            return cb.and(cb.equal(root.get(attribute), value));
        };
    }
}
