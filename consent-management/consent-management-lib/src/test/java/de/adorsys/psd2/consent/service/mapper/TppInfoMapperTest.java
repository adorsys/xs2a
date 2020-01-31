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
