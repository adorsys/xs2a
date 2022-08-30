/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.AuthenticationObject;
import de.adorsys.psd2.model.ScaMethods;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAuthenticationObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void mapToAuthenticationObjectList_withNull_returnNull(){
        // When
        List<de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject> scaMethodsList = scaMethodsMapper.mapToAuthenticationObjectList(null);

        // Then
        assertThat(scaMethodsList).isNull();

    }

    @Test
    void mapToAuthenticationObjectList_withRealData_success() {
        // Given
        SpiAuthenticationObject authenticationObject =
            jsonReader.getObjectFromFile("json/service/mapper/xs2a-authentication-objects-list.json", SpiAuthenticationObject.class);

        List<de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject> expected =
            Collections.singletonList(jsonReader.getObjectFromFile("json/service/mapper/authentication-objects-list.json", de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject.class));

        // When
        List<de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject> actual = scaMethodsMapper.mapToAuthenticationObjectList(Collections.singletonList(authenticationObject));

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void spiAuthenticationObjectToAuthenticationObject_isNull_returnsNull() {
        //  When
        List<de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject> actual = scaMethodsMapper.mapToAuthenticationObjectList(Collections.singletonList(null));

        // Then
        assertThat(actual.get(0)).isNull();
    }

}
