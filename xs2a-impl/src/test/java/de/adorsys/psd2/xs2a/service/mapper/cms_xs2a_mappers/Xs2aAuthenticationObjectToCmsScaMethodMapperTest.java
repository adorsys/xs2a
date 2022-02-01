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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

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
