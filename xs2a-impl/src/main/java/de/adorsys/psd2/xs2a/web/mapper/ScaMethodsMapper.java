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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.AuthenticationObject;
import de.adorsys.psd2.model.ScaMethods;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ScaMethodsMapper {

    default ScaMethods mapToScaMethods(List<Xs2aAuthenticationObject> xs2aAuthenticationObjects) {
        if (xs2aAuthenticationObjects == null) {
            return  null;
        }

        return xs2aAuthenticationObjects.stream()
                                 .map(this::mapToAuthenticationObject)
                                 .collect(Collectors.toCollection(ScaMethods::new));
    }

    AuthenticationObject mapToAuthenticationObject(Xs2aAuthenticationObject xs2aAuthenticationObject);
}
