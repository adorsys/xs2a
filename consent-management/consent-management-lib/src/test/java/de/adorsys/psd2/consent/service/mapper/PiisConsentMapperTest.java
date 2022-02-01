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

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PiisConsentMapper.class, PsuDataMapper.class, ConsentDataMapper.class, AccessMapper.class})
class PiisConsentMapperTest {
    private static final String INSTANCE_ID = "UNDEFINED";

    @Autowired
    private PiisConsentMapper piisConsentMapper;

    @Autowired
    private ConsentDataMapper consentDataMapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToPiisConsent() {
        ConsentEntity piisConsentEntity = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", ConsentEntity.class);
        PiisConsentData piisConsentData = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-data.json", PiisConsentData.class);
        piisConsentEntity.setData(consentDataMapper.getBytesFromConsentData(piisConsentData));

        CmsPiisConsent actualCmsPiisConsent = piisConsentMapper.mapToCmsPiisConsent(piisConsentEntity);

        CmsPiisConsent expectedPiisConsent = jsonReader.getObjectFromFile("json/service/mapper/cms-piis-consent.json", CmsPiisConsent.class);
        assertEquals(expectedPiisConsent, actualCmsPiisConsent);
    }

    @Test
    void mapToPiisConsentList() {
        ConsentEntity piisConsentEntity = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", ConsentEntity.class);
        PiisConsentData piisConsentData = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-data.json", PiisConsentData.class);
        piisConsentEntity.setData(consentDataMapper.getBytesFromConsentData(piisConsentData));

        List<CmsPiisConsent> actualCmsPiisConsentList = piisConsentMapper.mapToCmsPiisConsentList(Collections.singletonList(piisConsentEntity));

        CmsPiisConsent expectedPiisConsent = jsonReader.getObjectFromFile("json/service/mapper/cms-piis-consent.json", CmsPiisConsent.class);
        assertNotNull(actualCmsPiisConsentList);
        assertEquals(1, actualCmsPiisConsentList.size());
        assertEquals(expectedPiisConsent, actualCmsPiisConsentList.get(0));
    }

    @Test
    void mapToPiisConsentEntity() {
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        CreatePiisConsentRequest createPiisConsentRequest = jsonReader.getObjectFromFile("json/service/mapper/create-piis-consent-request.json", CreatePiisConsentRequest.class);
        TppInfoEntity tppInfoEntity = jsonReader.getObjectFromFile("json/service/mapper/tpp-info-consent.json", TppInfoEntity.class);
        ;

        ConsentEntity actual = piisConsentMapper.mapToPiisConsentEntity(psuIdData, tppInfoEntity, createPiisConsentRequest, INSTANCE_ID);

        ConsentEntity expected = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", ConsentEntity.class);
        expected.setId(null);
        expected.getAspspAccountAccesses().get(0).setConsent(actual);
        expected.setRecurringIndicator(false);
        expected.setLastActionDate(LocalDate.now());
        expected.setStatusChangeTimestamp(null);
        expected.setExternalId(actual.getExternalId());
        expected.setRequestDateTime(actual.getRequestDateTime());
        expected.setCreationTimestamp(actual.getCreationTimestamp());
        PiisConsentData piisConsentData = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-data.json", PiisConsentData.class);
        expected.setData(consentDataMapper.getBytesFromConsentData(piisConsentData));

        assertEquals(expected, actual);
    }

    @Test
    void mapToPiisConsentEntity_noTppInfoAndAuthorisationNumber_accessTypeAllTpp() {
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        CreatePiisConsentRequest createPiisConsentRequest = jsonReader.getObjectFromFile("json/service/mapper/create-piis-consent-request.json", CreatePiisConsentRequest.class);
        createPiisConsentRequest.setTppAuthorisationNumber(null);
        TppInfoEntity tppInfoEntity = jsonReader.getObjectFromFile("json/service/mapper/tpp-info-consent.json", TppInfoEntity.class);
        ;

        ConsentEntity actual = piisConsentMapper.mapToPiisConsentEntity(psuIdData, tppInfoEntity, createPiisConsentRequest, INSTANCE_ID);

        ConsentEntity expected = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", ConsentEntity.class);
        expected.setId(null);
        expected.getAspspAccountAccesses().get(0).setConsent(expected);
        expected.setRecurringIndicator(false);
        expected.setLastActionDate(LocalDate.now());
        expected.setStatusChangeTimestamp(null);
        expected.setExternalId(actual.getExternalId());
        expected.setRequestDateTime(actual.getRequestDateTime());
        expected.setCreationTimestamp(actual.getCreationTimestamp());
        PiisConsentData piisConsentData = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-data.json", PiisConsentData.class);
        expected.setData(consentDataMapper.getBytesFromConsentData(piisConsentData));

        assertEquals(expected, actual);
    }
}
