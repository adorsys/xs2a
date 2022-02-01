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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
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
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;
    private static final String TPP_REDIRECT_URI = "tpp redirect uri";
    private static final String TPP_NOK_REDIRECT_URI = "tpp nok redirect uri";

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
    @Mock
    private Xs2aConsentAuthorisationMapper xs2aConsentAuthorisationMapper;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private RequestProviderService requestProviderService;

    @Test
    void createConsentAuthorization_success() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(buildConsent()));
        when(xs2aConsentAuthorisationMapper.mapToAuthorisationRequest(AUTHORISATION_ID, SCA_STATUS, PSU_ID_DATA, SCA_APPROACH, TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI)).thenReturn(getTestCreateAuthRequest());
        when(xs2aConsentService.createConsentAuthorisation(CONSENT_ID, getTestCreateAuthRequest()))
            .thenReturn(Optional.of(buildCreateAisConsentAuthorizationResponse()));
        when(requestProviderService.getTppRedirectURI()).thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI()).thenReturn(TPP_NOK_REDIRECT_URI);
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .consentId(CONSENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = redirectAisAuthorisationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponse).isPresent().contains(buildCreateConsentAuthResponse());
    }

    @Test
    void createConsentAuthorization_wrongConsentId_fail() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .consentId(WRONG_CONSENT_ID)
                                                                            .psuData(PSU_ID_DATA)
                                                                            .scaStatus(SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = redirectAisAuthorisationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getAccountConsentAuthorizationById_success() {
        // When
        Optional<Authorisation> actualResponse = redirectAisAuthorisationService.getConsentAuthorizationById(AUTHORISATION_ID);

        // Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        // Given
        when(xs2aConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actual = redirectAisAuthorisationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actual).isPresent().contains(SCA_STATUS);
    }

    @Test
    void getAuthorisationScaStatus_failure_wrongIds() {
        // Given
        when(xs2aConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = redirectAisAuthorisationService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actual).isNotPresent();
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
        response.setScaApproach(SCA_APPROACH);
        response.setAuthorisationId(AUTHORISATION_ID);
        response.setScaStatus(ScaStatus.RECEIVED);
        response.setInternalRequestId(INTERNAL_REQUEST_ID);
        response.setPsuIdData(PSU_ID_DATA);
        return response;
    }

    private CreateAuthorisationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, INTERNAL_REQUEST_ID, PSU_ID_DATA, SCA_APPROACH);
    }

    private CreateAuthorisationRequest getTestCreateAuthRequest() {
        CreateAuthorisationRequest consentAuthorization = new CreateAuthorisationRequest();
        consentAuthorization.setScaStatus(SCA_STATUS);
        consentAuthorization.setAuthorisationId(AUTHORISATION_ID);
        consentAuthorization.setPsuData(PSU_ID_DATA);
        consentAuthorization.setScaApproach(SCA_APPROACH);
        return consentAuthorization;
    }

    private static AisConsent buildConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        return aisConsent;
    }
}
