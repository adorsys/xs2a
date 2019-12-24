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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.ais.CreateAisConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DecoupledAisAuthorizationServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final AccountConsentAuthorization ACCOUNT_CONSENT_AUTHORIZATION = buildAccountConsentAuthorization();
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final AccountConsent ACCOUNT_CONSENT = buildConsent(CONSENT_ID);
    private static final CreateConsentAuthorizationResponse CREATE_CONSENT_AUTHORIZATION_RESPONSE = buildCreateConsentAuthorizationResponse();

    @InjectMocks
    private DecoupledAisAuthorizationService decoupledAisAuthorizationService;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private AisScaStageAuthorisationFactory scaStageAuthorisationFactory;
    @Mock
    private RequestProviderService requestProviderService;

    @Before
    public void setUp() {
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
    }

    @Test
    public void createConsentAuthorization_success() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(ACCOUNT_CONSENT));
        when(aisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA))
            .thenReturn(Optional.of(buildCreateAisConsentAuthorizationResponse()));

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_DATA, CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(CREATE_CONSENT_AUTHORIZATION_RESPONSE);
    }

    @Test
    public void createConsentAuthorization_wrongConsentId_fail() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_DATA, WRONG_CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAccountConsentAuthorizationById_success() {
        // Given
        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(ACCOUNT_CONSENT_AUTHORIZATION));

        // When
        Optional<AccountConsentAuthorization> actualResponse = decoupledAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(ACCOUNT_CONSENT_AUTHORIZATION);
    }

    @Test
    public void getAccountConsentAuthorizationById_wrongIds_fail() {
        // Given
        when(aisConsentService.getAccountConsentAuthorizationById(WRONG_AUTHORISATION_ID, WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<AccountConsentAuthorization> actualResponse = decoupledAisAuthorizationService.getAccountConsentAuthorizationById(WRONG_AUTHORISATION_ID, WRONG_CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // Given
        when(aisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    public void getAuthorisationScaStatus_wrongIds_fail() {
        // Given
        when(aisConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getScaApproachServiceType_success() {
        // When
        ScaApproach actualResponse = decoupledAisAuthorizationService.getScaApproachServiceType();

        // Then
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }

    private static CreateConsentAuthorizationResponse buildCreateConsentAuthorizationResponse() {
        CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();
        resp.setConsentId(CONSENT_ID);
        resp.setAuthorisationId(AUTHORISATION_ID);
        resp.setScaStatus(ScaStatus.RECEIVED);
        resp.setPsuIdData(PSU_DATA);
        return resp;
    }

    private static AccountConsentAuthorization buildAccountConsentAuthorization() {
        AccountConsentAuthorization accountConsentAuthorization = new AccountConsentAuthorization();
        accountConsentAuthorization.setScaStatus(ScaStatus.RECEIVED);
        return accountConsentAuthorization;
    }

    private static AccountConsent buildConsent(String id) {
        return new AccountConsent(id, new Xs2aAccountAccess(null, null, null, null, null, null, null), new Xs2aAccountAccess(null, null, null, null, null, null, null), false, LocalDate.now(), 4, LocalDate.now(), ConsentStatus.VALID, false, false, Collections.singletonList(PSU_DATA), null, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap(), OffsetDateTime.now());
    }

    private CreateAisConsentAuthorizationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAisConsentAuthorizationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", PSU_DATA);
    }
}
