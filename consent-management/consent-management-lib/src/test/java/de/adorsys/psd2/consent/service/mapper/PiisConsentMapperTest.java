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

import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.reader.JsonReader;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.piis.PiisConsentTppAccessType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PiisConsentMapper.class, PsuDataMapper.class, TppInfoMapperImpl.class,
    AccountReferenceMapper.class})
public class PiisConsentMapperTest {
    @Autowired
    private PiisConsentMapper piisConsentMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void mapToPiisConsent() {
        PiisConsentEntity piisConsentEntity = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", PiisConsentEntity.class);
        PiisConsent actualPiisConsent = piisConsentMapper.mapToPiisConsent(piisConsentEntity);

        PiisConsent expectedPiisConsent = jsonReader.getObjectFromFile("json/service/mapper/piis-consent.json", PiisConsent.class);
        assertEquals(expectedPiisConsent, actualPiisConsent);
    }

    @Test
    public void mapToPiisConsentList() {
        PiisConsentEntity piisConsentEntity = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", PiisConsentEntity.class);
        List<PiisConsent> actualPiisConsentList = piisConsentMapper.mapToPiisConsentList(Collections.singletonList(piisConsentEntity));

        PiisConsent expectedPiisConsent = jsonReader.getObjectFromFile("json/service/mapper/piis-consent.json", PiisConsent.class);
        assertNotNull(actualPiisConsentList);
        assertEquals(1, actualPiisConsentList.size());
        assertEquals(expectedPiisConsent, actualPiisConsentList.get(0));
    }

    @Test
    public void mapToPiisConsentEntity() {
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        CreatePiisConsentRequest createPiisConsentRequest = jsonReader.getObjectFromFile("json/service/mapper/create-piis-consent-request.json", CreatePiisConsentRequest.class);

        PiisConsentEntity actualPiisConsentEntity = piisConsentMapper.mapToPiisConsentEntity(psuIdData, createPiisConsentRequest);

        PiisConsentEntity expectedPiisConsentEntity = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", PiisConsentEntity.class);
        expectedPiisConsentEntity.setId(null);
        expectedPiisConsentEntity.setRecurringIndicator(false);
        expectedPiisConsentEntity.setLastActionDate(null);
        expectedPiisConsentEntity.setStatusChangeTimestamp(null);
        expectedPiisConsentEntity.setExternalId(actualPiisConsentEntity.getExternalId());
        expectedPiisConsentEntity.setRequestDateTime(actualPiisConsentEntity.getRequestDateTime());
        expectedPiisConsentEntity.setCreationTimestamp(actualPiisConsentEntity.getCreationTimestamp());

        assertEquals(expectedPiisConsentEntity, actualPiisConsentEntity);
    }

    @Test
    public void mapToPiisConsentEntity_noTppInfoAndAuthorisationNumber_accessTypeAllTpp() {
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        CreatePiisConsentRequest createPiisConsentRequest = jsonReader.getObjectFromFile("json/service/mapper/create-piis-consent-request.json", CreatePiisConsentRequest.class);
        createPiisConsentRequest.setTppAuthorisationNumber(null);

        PiisConsentEntity actualPiisConsentEntity = piisConsentMapper.mapToPiisConsentEntity(psuIdData, createPiisConsentRequest);

        PiisConsentEntity expectedPiisConsentEntity = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", PiisConsentEntity.class);
        expectedPiisConsentEntity.setId(null);
        expectedPiisConsentEntity.setRecurringIndicator(false);
        expectedPiisConsentEntity.setLastActionDate(null);
        expectedPiisConsentEntity.setStatusChangeTimestamp(null);
        expectedPiisConsentEntity.setExternalId(actualPiisConsentEntity.getExternalId());
        expectedPiisConsentEntity.setRequestDateTime(actualPiisConsentEntity.getRequestDateTime());
        expectedPiisConsentEntity.setCreationTimestamp(actualPiisConsentEntity.getCreationTimestamp());
        expectedPiisConsentEntity.setTppAuthorisationNumber(null);
        expectedPiisConsentEntity.setTppAccessType(PiisConsentTppAccessType.ALL_TPP);

        assertEquals(expectedPiisConsentEntity, actualPiisConsentEntity);
    }
}
