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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CmsConsentMapper.class, AuthorisationTemplateMapperImpl.class, ConsentTppInformationMapperImpl.class,
    TppInfoMapperImpl.class, PsuDataMapper.class, AuthorisationMapperImpl.class, AccessMapper.class})
class CmsConsentMapperTest {
    @Autowired
    private CmsConsentMapper cmsConsentMapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToCmsConsent() {
        //Given
        AuthorisationEntity authorisationEntity = jsonReader.getObjectFromFile("json/service/mapper/cms-consent-mapper/authorisation-entity.json", AuthorisationEntity.class);
        List<AuthorisationEntity> authorisations = Collections.singletonList(authorisationEntity);
        ConsentEntity consentEntity = jsonReader.getObjectFromFile("json/service/mapper/cms-consent-mapper/consent-entity.json", ConsentEntity.class);
        //When
        CmsConsent actual = cmsConsentMapper.mapToCmsConsent(consentEntity, authorisations, getUsages());
        //Then
        CmsConsent expected = jsonReader.getObjectFromFile("json/service/mapper/cms-consent-mapper/cms-consent.json", CmsConsent.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToNewConsentEntity() {
        //Given
        CmsConsent cmsConsent = jsonReader.getObjectFromFile("json/service/mapper/cms-consent-mapper/new-cms-consent.json", CmsConsent.class);
        //When
        ConsentEntity actual = cmsConsentMapper.mapToNewConsentEntity(cmsConsent);
        //Then
        ConsentEntity expected = jsonReader.getObjectFromFile("json/service/mapper/cms-consent-mapper/new-consent-entity.json", ConsentEntity.class);
        expected.setExternalId(actual.getExternalId());
        expected.setLastActionDate(actual.getLastActionDate());
        expected.setRequestDateTime(actual.getRequestDateTime());
        expected.setCreationTimestamp(actual.getCreationTimestamp());
        assertEquals(expected, actual);
    }

    private Map<String, Integer> getUsages() {
        Map<String, Integer> usages = new HashMap<>();
        usages.put("/v1/accounts", 12);
        return usages;
    }
}
