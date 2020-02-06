/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Xs2aAuthenticationObjectToCmsScaMethodMapperTest {

    private Xs2aAuthenticationObjectToCmsScaMethodMapper mapper;
    private AuthenticationObject authenticationObject;
    private CmsScaMethod cmsScaMethod;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        mapper = new Xs2aAuthenticationObjectToCmsScaMethodMapper();
        authenticationObject = jsonReader.getObjectFromFile("json/service/mapper/consent/authentication-object.json", AuthenticationObject.class);
        cmsScaMethod = jsonReader.getObjectFromFile("json/service/mapper/consent/cms-sca-method.json", CmsScaMethod.class);
    }

    @Test
    void mapToCmsScaMethod() {
        assertEquals(cmsScaMethod, mapper.mapToCmsScaMethod(authenticationObject));
    }

    @Test
    void mapToCmsScaMethods() {
        List<CmsScaMethod> actual = mapper.mapToCmsScaMethods(Collections.singletonList(authenticationObject));

        assertEquals(1, actual.size());
        assertEquals(cmsScaMethod, actual.get(0));
    }
}
