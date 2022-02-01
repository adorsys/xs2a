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
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(actual).isEqualTo(expected);
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
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToConsentTppInformationEntity_success() {
        // Given
        ConsentTppInformation consentTppInformation = jsonReader.getObjectFromFile("json/service/mapper/consent-tpp-information.json", ConsentTppInformation.class);
        ConsentTppInformationEntity expected = buildConsentTppInformationEntity();

        // When
        ConsentTppInformationEntity actual = consentTppInformationMapper.mapToConsentTppInformationEntity(consentTppInformation);

        // Then
        assertThat(actual).isEqualTo(expected);
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
