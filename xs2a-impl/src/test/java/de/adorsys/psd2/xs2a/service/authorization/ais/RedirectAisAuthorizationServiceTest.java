/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedirectAisAuthorizationServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final List<String> STRING_LIST = Collections.singletonList(CONSENT_ID);
    private static final Xs2aAuthorisationSubResources XS2A_AUTHORISATION_SUB_RESOURCES = new Xs2aAuthorisationSubResources(STRING_LIST);
    private static final UpdateConsentPsuDataReq UPDATE_CONSENT_PSU_DATA_REQ = new UpdateConsentPsuDataReq();
    private static final AccountConsentAuthorization ACCOUNT_CONSENT_AUTHORIZATION = new AccountConsentAuthorization();
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null);
    private static final CreateConsentAuthorizationResponse CREATE_CONSENT_AUTHORIZATION_RESPONSE = buildCreateConsentAuthResponse();

    @InjectMocks
    private RedirectAisAuthorizationService redirectAisAuthorisationService;

    @Mock
    private Xs2aAisConsentService xs2aAisConsentService;

    @Test
    public void createConsentAuthorization_success() {
        // Given
        when(xs2aAisConsentService.createAisConsentAuthorization(CONSENT_ID, ScaStatus.STARTED, PSU_ID_DATA))
            .thenReturn(Optional.of(AUTHORISATION_ID));

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = redirectAisAuthorisationService.createConsentAuthorization(PSU_ID_DATA, CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(CREATE_CONSENT_AUTHORIZATION_RESPONSE));
    }

    @Test
    public void createConsentAuthorization_wrongConsentId_fail() {
        // Given
        when(xs2aAisConsentService.createAisConsentAuthorization(WRONG_CONSENT_ID, ScaStatus.STARTED, PSU_ID_DATA))
            .thenReturn(Optional.empty());

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = redirectAisAuthorisationService.createConsentAuthorization(PSU_ID_DATA, WRONG_CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void updateConsentPsuData_success() {
        // When
        UpdateConsentPsuDataResponse actualResponse = redirectAisAuthorisationService.updateConsentPsuData(UPDATE_CONSENT_PSU_DATA_REQ, ACCOUNT_CONSENT_AUTHORIZATION);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void getAccountConsentAuthorizationById_success() {
        // When
        Optional<AccountConsentAuthorization>  actualResponse = redirectAisAuthorisationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationSubResources_success() {
        // Given
        when(xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID))
            .thenReturn(Optional.of(STRING_LIST));

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = redirectAisAuthorisationService.getAuthorisationSubResources(CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse).isEqualTo(Optional.of(XS2A_AUTHORISATION_SUB_RESOURCES));
    }

    @Test
    public void getAuthorisationSubResources_wrongConsentId_fail() {
        // Given
        when(xs2aAisConsentService.getAuthorisationSubResources(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = redirectAisAuthorisationService.getAuthorisationSubResources(WRONG_CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // Given
        when(xs2aAisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actual = redirectAisAuthorisationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actual.isPresent()).isTrue();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(actual.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongIds() {
        // Given
        when(xs2aAisConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = redirectAisAuthorisationService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void getScaApproachServiceType_success() {
        //When
        ScaApproach actualResponse = redirectAisAuthorisationService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isEqualTo(ScaApproach.REDIRECT);
    }

    private static CreateConsentAuthorizationResponse buildCreateConsentAuthResponse() {
        CreateConsentAuthorizationResponse response = new CreateConsentAuthorizationResponse();
        response.setConsentId(CONSENT_ID);
        response.setAuthorizationId(AUTHORISATION_ID);
        response.setScaStatus(ScaStatus.STARTED);
        response.setResponseLinkType(ConsentAuthorizationResponseLinkType.SCA_REDIRECT);
        return response;
    }
}
