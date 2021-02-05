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

import de.adorsys.psd2.consent.api.ais.AdditionalTppInfo;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentTppInformationEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode.PROCESS;
import static de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode.SCA;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConsentTppInformationMapperImpl.class, TppInfoMapperImpl.class})
class ConsentTppInformationMapperTest {
    private static final String TPP_NOTIFICATION_URI = "htp://tpp/notification/uri";
    private static final String TPP_AUTHORISATION_NUMBER = "TPP_AUTHORISATION_NUMBER";
    private static final String TPP_AUTHORITY_ID = "TPP_AUTHORITY_ID";
    ;
    private JsonReader jsonReader;

    @Autowired
    private ConsentTppInformationMapper consentTppInformationMapper;

    @BeforeEach
    void init() {
        jsonReader = new JsonReader();
    }

    @Test
    void mapToConsentTppInformation_success() {
        // Given
        ConsentTppInformationEntity consentTppInformationEntity = buildConsentTppInformationEntity();
        ConsentTppInformation expected = jsonReader.getObjectFromFile("json/service/mapper/consent-tpp-information.json", ConsentTppInformation.class);

        // When
        ConsentTppInformation actual = consentTppInformationMapper.mapToConsentTppInformation(consentTppInformationEntity);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToConsentTppInformation_roles_are_null() {
        // Given
        ConsentTppInformationEntity consentTppInformationEntity = buildConsentTppInformationEntity();
        TppInfoEntity tppInfoEntity = buildTppInfoEntity();
        tppInfoEntity.setTppRoles(null);
        consentTppInformationEntity.setTppInfo(tppInfoEntity);

        ConsentTppInformation expected = jsonReader.getObjectFromFile("json/service/mapper/consent-tpp-information.json", ConsentTppInformation.class);
        TppInfo tppInfo = expected.getTppInfo();
        tppInfo.setTppRoles(null);
        expected.setTppInfo(tppInfo);

        // When
        ConsentTppInformation actual = consentTppInformationMapper.mapToConsentTppInformation(consentTppInformationEntity);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    void mapToConsentTppInformationEntity_success() {
        // Given
        ConsentTppInformation consentTppInformation = jsonReader.getObjectFromFile("json/service/mapper/consent-tpp-information.json", ConsentTppInformation.class);
        ConsentTppInformationEntity expected = buildConsentTppInformationEntity();

        // When
        ConsentTppInformationEntity actual = consentTppInformationMapper.mapToConsentTppInformationEntity(consentTppInformation);

        // Then
        assertEquals(expected, actual);
    }

    private ConsentTppInformationEntity buildConsentTppInformationEntity() {
        ConsentTppInformationEntity consentTppInformationEntity = new ConsentTppInformationEntity();
        consentTppInformationEntity.setTppRedirectPreferred(true);
        consentTppInformationEntity.setTppInfo(buildTppInfoEntity());
        consentTppInformationEntity.setTppFrequencyPerDay(7);
        consentTppInformationEntity.setTppNotificationUri(TPP_NOTIFICATION_URI);
        consentTppInformationEntity.setTppNotificationContentPreferred(Arrays.asList(SCA, PROCESS));
        consentTppInformationEntity.setAdditionalInfo(AdditionalTppInfo.NONE);
        return consentTppInformationEntity;
    }

    private TppInfoEntity buildTppInfoEntity() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfoEntity.setAuthorityId(TPP_AUTHORITY_ID);
        tppInfoEntity.setTppRoles(Arrays.asList(TppRole.AISP, TppRole.ASPSP, TppRole.PIISP, TppRole.PISP));
        return tppInfoEntity;
    }
}
