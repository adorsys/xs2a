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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TppInfoMapperImpl.class})
class TppInfoMapperTest {

    @Autowired
    private TppInfoMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToTppInfoEntity() {
        TppInfo tppInfo = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);

        TppInfoEntity actualTppInfoEntity = mapper.mapToTppInfoEntity(tppInfo);

        TppInfoEntity expectedTppInfoEntity = jsonReader.getObjectFromFile("json/service/mapper/tpp-info-entity.json", TppInfoEntity.class);
        assertEquals(expectedTppInfoEntity, actualTppInfoEntity);
    }

    @Test
    void mapToTppInfoEntity_nullValue() {
        TppInfoEntity actualTppInfoEntity = mapper.mapToTppInfoEntity(null);
        assertNull(actualTppInfoEntity);
    }

    @Test
    void mapToTppInfoEntity_withNullTppRedirectUri_shouldNotMapTppRedirectUris() {
        TppInfo tppInfo = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);
        tppInfo.setCancelTppRedirectUri(null);

        TppInfoEntity actualTppInfoEntity = mapper.mapToTppInfoEntity(tppInfo);

        TppInfoEntity expectedTppInfoEntity = jsonReader.getObjectFromFile("json/service/mapper/tpp-info-entity.json", TppInfoEntity.class);
        assertEquals(expectedTppInfoEntity, actualTppInfoEntity);
    }

    @Test
    void mapToTppInfoEntity_withNullTppRoles_shouldMapRolesToEmptyList() {
        TppInfo tppInfo = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);
        tppInfo.setCancelTppRedirectUri(null);

        TppInfoEntity actualTppInfoEntity = mapper.mapToTppInfoEntity(new TppInfo());

        assertNotNull(actualTppInfoEntity);
        assertNotNull(actualTppInfoEntity.getTppRoles());
        assertEquals(Collections.emptyList(), actualTppInfoEntity.getTppRoles());
    }

    @Test
    void mapToTppInfo() {
        TppInfoEntity tppInfoEntity = jsonReader.getObjectFromFile("json/service/mapper/tpp-info-entity.json", TppInfoEntity.class);

        TppInfo actualTppInfo = mapper.mapToTppInfo(tppInfoEntity);

        TppInfo expectedTppInfo = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);
        assertEquals(expectedTppInfo, actualTppInfo);
    }

    @Test
    void mapToTppInfo_withoutLinks_shouldNotMapRedirectUri() {
        TppInfoEntity tppInfoEntity = jsonReader.getObjectFromFile("json/service/mapper/tpp-info-entity.json", TppInfoEntity.class);

        TppInfo actualTppInfo = mapper.mapToTppInfo(tppInfoEntity);

        TppInfo expectedTppInfo = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);
        expectedTppInfo.setCancelTppRedirectUri(null);
        assertEquals(expectedTppInfo, actualTppInfo);
    }

    @Test
    void mapToTppInfo_withNullTppRoles_shouldMapRolesToEmptyList() {
        TppInfo actualTppInfo = mapper.mapToTppInfo(new TppInfoEntity());

        assertNotNull(actualTppInfo);
        assertNotNull(actualTppInfo.getTppRoles());
        assertEquals(Collections.emptyList(), actualTppInfo.getTppRoles());
    }

    @Test
    void mapToTppInfo_nullValue() {
        TppInfo actualTppInfo = mapper.mapToTppInfo(null);
        assertNull(actualTppInfo);
    }
}
