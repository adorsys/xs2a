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
import de.adorsys.psd2.model.ChosenScaMethod;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ChosenScaMethodMapperImpl.class})
class ChosenScaMethodMapperTest {
    @Autowired
    private ChosenScaMethodMapper chosenScaMethodMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToChosenScaMethod_withNull_shouldReturnNull() {
        // When
        ChosenScaMethod chosenScaMethod = chosenScaMethodMapper.mapToChosenScaMethod(null);

        // Then
        assertNull(chosenScaMethod);
    }

    @Test
    void mapToChosenScaMethod() {
        // Given
        de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject authenticationObject =
            jsonReader.getObjectFromFile("json/web/mapper/Xs2aAuthenticationObject.json", de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject.class);

        AuthenticationObject expected =
            jsonReader.getObjectFromFile("json/web/mapper/chosenScaMethod.json", ChosenScaMethod.class);

        // When
        ChosenScaMethod chosenScaMethod = chosenScaMethodMapper.mapToChosenScaMethod(authenticationObject);

        // Then
        assertNotNull(chosenScaMethod);
        assertEquals(expected, chosenScaMethod);
    }
}
