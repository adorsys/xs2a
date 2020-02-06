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

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CmsPsuAuthorisationMapperImpl.class})
class CmsPsuAuthorisationMapperTest {
    private static final String EXTERNAL_ID = "123";
    private static final String OK_REDIRECT_URI = "OK redirect";
    private static final String NOK_REDIRECT_URI = "Not OK redirect";
    private static final OffsetDateTime EXPIRATION_TIMESTAMP = OffsetDateTime.now().plusDays(1);
    private static final String PSU_ID = "PSU ID";
    private static final String PSU_ID_TYPE = "PSU ID type";

    private CmsPsuAuthorisationMapper cmsPsuAuthorisationMapper = new CmsPsuAuthorisationMapperImpl();

    private JsonReader jsonReader;

    @BeforeEach
    void init() {
        jsonReader = new JsonReader();
    }

    @Test
    void mapToCmsPsuAuthorisationPis_success() {
        // Given
        AuthorisationEntity pisAuthorization = buildPisAuthorisation();

        // When
        CmsPsuAuthorisation actual = cmsPsuAuthorisationMapper.mapToCmsPsuAuthorisation(pisAuthorization);

        // Then
        CmsPsuAuthorisation expected = jsonReader.getObjectFromFile("json/service/mapper/cms-psu-authorisation-pis.json", CmsPsuAuthorisation.class);
        expected.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);

        assertEquals(expected, actual);
    }

    @Test
    void mapToCmsPsuAuthorisationAis_success() {
        // Given
        AuthorisationEntity aisConsentAuthorization = buildAisConsentAuthorisation();

        // When
        CmsPsuAuthorisation actual = cmsPsuAuthorisationMapper.mapToCmsPsuAuthorisation(aisConsentAuthorization);

        // Then
        CmsPsuAuthorisation expected = jsonReader.getObjectFromFile("json/service/mapper/cms-psu-authorisation-ais.json", CmsPsuAuthorisation.class);
        expected.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);
        assertEquals(expected, actual);
    }

    @Test
    void mapToCmsPsuAuthorisationPisCancellation_success() {
        // Given
        AuthorisationEntity cancellationAuthorisation = buildPisCancellationAuthorisation();

        // When
        CmsPsuAuthorisation actual = cmsPsuAuthorisationMapper.mapToCmsPsuAuthorisation(cancellationAuthorisation);

        // Then
        CmsPsuAuthorisation expected = jsonReader.getObjectFromFile("json/service/mapper/cms-psu-pis-cancellation-authorisation.json", CmsPsuAuthorisation.class);
        expected.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);

        assertEquals(expected, actual);
    }

    @NotNull
    private AuthorisationEntity buildPisAuthorisation() {
        AuthorisationEntity pisAuthorization = new AuthorisationEntity();
        pisAuthorization.setAuthorisationType(AuthorisationType.PIS_CREATION);
        pisAuthorization.setExternalId(EXTERNAL_ID);
        pisAuthorization.setScaStatus(ScaStatus.RECEIVED);
        pisAuthorization.setPsuData(new PsuData(PSU_ID, PSU_ID_TYPE, "", "", ""));
        pisAuthorization.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);
        pisAuthorization.setScaApproach(ScaApproach.EMBEDDED);
        pisAuthorization.setTppOkRedirectUri(OK_REDIRECT_URI);
        pisAuthorization.setTppNokRedirectUri(NOK_REDIRECT_URI);
        return pisAuthorization;
    }

    @NotNull
    private AuthorisationEntity buildPisCancellationAuthorisation() {
        AuthorisationEntity pisAuthorization = new AuthorisationEntity();
        pisAuthorization.setAuthorisationType(AuthorisationType.PIS_CANCELLATION);
        pisAuthorization.setExternalId(EXTERNAL_ID);
        pisAuthorization.setScaStatus(ScaStatus.RECEIVED);
        pisAuthorization.setPsuData(new PsuData(PSU_ID, PSU_ID_TYPE, "", "", ""));
        pisAuthorization.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);
        pisAuthorization.setScaApproach(ScaApproach.EMBEDDED);
        pisAuthorization.setTppOkRedirectUri(OK_REDIRECT_URI);
        pisAuthorization.setTppNokRedirectUri(NOK_REDIRECT_URI);
        return pisAuthorization;
    }

    @NotNull
    private AuthorisationEntity buildAisConsentAuthorisation() {
        AuthorisationEntity authorization = new AuthorisationEntity();
        authorization.setAuthorisationType(AuthorisationType.AIS);
        authorization.setExternalId(EXTERNAL_ID);
        authorization.setScaStatus(ScaStatus.RECEIVED);
        authorization.setPsuData(new PsuData("PSU ID", "PSU ID type", "", "", ""));
        authorization.setAuthorisationExpirationTimestamp(EXPIRATION_TIMESTAMP);
        authorization.setScaApproach(ScaApproach.EMBEDDED);
        authorization.setTppOkRedirectUri(OK_REDIRECT_URI);
        authorization.setTppNokRedirectUri(NOK_REDIRECT_URI);
        return authorization;
    }
}
