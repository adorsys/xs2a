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
