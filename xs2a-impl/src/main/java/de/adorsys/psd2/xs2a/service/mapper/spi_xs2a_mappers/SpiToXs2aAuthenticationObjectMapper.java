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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpiToXs2aAuthenticationObjectMapper {
    public Xs2aAuthenticationObject mapToXs2aAuthenticationObject(SpiAuthenticationObject authenticationObject) {
        Xs2aAuthenticationObject object = new Xs2aAuthenticationObject();
        object.setAuthenticationType(authenticationObject.getAuthenticationType());
        object.setAuthenticationMethodId(authenticationObject.getAuthenticationMethodId());
        object.setAuthenticationVersion(authenticationObject.getAuthenticationVersion());
        object.setName(authenticationObject.getName());
        object.setExplanation(authenticationObject.getExplanation());
        object.setDecoupled(authenticationObject.isDecoupled());
        return object;
    }

    public List<Xs2aAuthenticationObject> mapToXs2aListAuthenticationObject(List<SpiAuthenticationObject> authenticationObjects) {
        return authenticationObjects.stream()
                   .map(this::mapToXs2aAuthenticationObject)
                   .collect(Collectors.toList());
    }
}

