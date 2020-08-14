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


package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aConsentServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String REDIRECT_URI = "request/redirect_uri";
    private static final String NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final CreateAuthorisationRequest AIS_CONSENT_AUTHORISATION_REQUEST = buildAisConsentAuthorisationRequest();

    @InjectMocks
    private Xs2aConsentService xs2aConsentService;

    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aConsentAuthorisationMapper aisConsentAuthorisationMapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RequestProviderService requestProviderService;

    @Test
    void createAisConsentAuthorization_success() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(aisConsentAuthorisationMapper.mapToAuthorisationRequest(SCA_STATUS, PSU_DATA, SCA_APPROACH, REDIRECT_URI, NOK_REDIRECT_URI))
            .thenReturn(AIS_CONSENT_AUTHORISATION_REQUEST);
        when(authorisationService.createAuthorisation(AIS_CONSENT_AUTHORISATION_REQUEST, CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(Optional.of(buildCreateAisConsentAuthorizationResponse()));
        when(requestProviderService.getTppRedirectURI())
            .thenReturn(REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI())
            .thenReturn(NOK_REDIRECT_URI);

        // When
        Optional<CreateAuthorisationResponse> actualResponse = xs2aConsentService.createConsentAuthorisation(CONSENT_ID, SCA_STATUS, PSU_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(buildCreateAisConsentAuthorizationResponse());
    }

    @Test
    void createAisConsentAuthorization_false() {
        // Given
        when(requestProviderService.getTppRedirectURI()).thenReturn("ok.uri");
        when(requestProviderService.getTppNokRedirectURI()).thenReturn("nok.uri");

        when(scaApproachResolver.resolveScaApproach()).thenReturn(SCA_APPROACH);
        CreateAuthorisationRequest request = new CreateAuthorisationRequest();
        when(aisConsentAuthorisationMapper.mapToAuthorisationRequest(SCA_STATUS, PSU_DATA, SCA_APPROACH, "ok.uri", "nok.uri"))
            .thenReturn(request);
        when(authorisationService.createAuthorisation(request, CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(Optional.empty());

        // When
        Optional<CreateAuthorisationResponse> actualResponse = xs2aConsentService.createConsentAuthorisation(CONSENT_ID, SCA_STATUS, PSU_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationScaStatus() {
        xs2aConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        verify(authorisationService, times(1)).getAuthorisationScaStatus(AUTHORISATION_ID, CONSENT_ID, AuthorisationType.CONSENT);
    }

    private static CreateAuthorisationRequest buildAisConsentAuthorisationRequest() {
        CreateAuthorisationRequest consentAuthorization = new CreateAuthorisationRequest();
        consentAuthorization.setPsuData(PSU_DATA);
        consentAuthorization.setScaApproach(SCA_APPROACH);
        return consentAuthorization;
    }

    private static CreateAuthorisationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null);
    }

}
