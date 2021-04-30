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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.AuthenticationObject;
import de.adorsys.psd2.model.ScaMethods;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ScaMethodsMapperImpl.class})
class ScaMethodsMapperTest {

    @Autowired
    private ScaMethodsMapper scaMethodsMapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToScaMethods_withNull_shouldReturnNull() {
        // When
        ScaMethods scaMethods = scaMethodsMapper.mapToScaMethods(null);

        // Then
        assertNull(scaMethods);
    }

    @Test
    void mapToAuthenticationObject_withNull_returnsNull() {
        AuthenticationObject authenticationObject = scaMethodsMapper.mapToAuthenticationObject(null);

        assertNull(authenticationObject);
    }

    @Test
    void mapToScaMethods_withRealData_success() {
        // Given
        de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject authenticationObject =
            jsonReader.getObjectFromFile("json/service/mapper/xs2a-authentication-objects-list.json", de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject.class);

        AuthenticationObject expected =
            jsonReader.getObjectFromFile("json/service/mapper/authentication-objects-list.json", AuthenticationObject.class);

        // When
        ScaMethods scaMethods = scaMethodsMapper.mapToScaMethods(Collections.singletonList(authenticationObject));

        // Then
        assertNotNull(scaMethods);
        assertEquals(expected, scaMethods.get(0));
    }
}
