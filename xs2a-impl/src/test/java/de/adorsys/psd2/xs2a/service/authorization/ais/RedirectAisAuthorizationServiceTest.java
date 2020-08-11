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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectAisAuthorizationServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null, null);
    private static final CreateConsentAuthorizationResponse CREATE_CONSENT_AUTHORIZATION_RESPONSE = buildCreateConsentAuthResponse();
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private RedirectAisAuthorizationService redirectAisAuthorisationService;
    @Mock
    private Xs2aConsentService xs2aConsentService;

    @Mock
    private Xs2aAuthorisationService xs2aAuthorisationService;

    @Mock
    private Xs2aAisConsentMapper xs2aAisConsentMapper;

    @Mock
    private AisAuthorisationConfirmationService aisAuthorisationConfirmationService;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void createConsentAuthorization_success() {
        // Given
        when(xs2aConsentService.createConsentAuthorisation(CONSENT_ID, ScaStatus.RECEIVED, PSU_ID_DATA))
            .thenReturn(Optional.of(buildCreateAisConsentAuthorizationResponse()));

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = redirectAisAuthorisationService.createConsentAuthorization(PSU_ID_DATA, CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(CREATE_CONSENT_AUTHORIZATION_RESPONSE));
    }

    @Test
    void createConsentAuthorization_wrongConsentId_fail() {
        // Given
        when(xs2aConsentService.createConsentAuthorisation(WRONG_CONSENT_ID, ScaStatus.RECEIVED, PSU_ID_DATA))
            .thenReturn(Optional.empty());

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = redirectAisAuthorisationService.createConsentAuthorization(PSU_ID_DATA, WRONG_CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAccountConsentAuthorizationById_success() {
        // When
        Optional<Authorisation> actualResponse = redirectAisAuthorisationService.getAccountConsentAuthorizationById(AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        // Given
        when(xs2aConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actual = redirectAisAuthorisationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    void getAuthorisationScaStatus_failure_wrongIds() {
        // Given
        when(xs2aConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = redirectAisAuthorisationService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    void getScaApproachServiceType_success() {
        //When
        ScaApproach actualResponse = redirectAisAuthorisationService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isEqualTo(ScaApproach.REDIRECT);
    }

    private static CreateConsentAuthorizationResponse buildCreateConsentAuthResponse() {
        CreateConsentAuthorizationResponse response = new CreateConsentAuthorizationResponse();
        response.setConsentId(CONSENT_ID);
        response.setAuthorisationId(AUTHORISATION_ID);
        response.setScaStatus(ScaStatus.RECEIVED);
        response.setInternalRequestId(INTERNAL_REQUEST_ID);
        response.setPsuIdData(PSU_ID_DATA);
        return response;
    }

    private CreateAuthorisationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, INTERNAL_REQUEST_ID, PSU_ID_DATA);
    }
}
