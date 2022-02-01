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

package de.adorsys.psd2.xs2a.service.authorization.piis;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedirectPiisAuthorizationServiceTest {
    private static final ScaStatus STARTED_SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaStatus STARTED_XS2A_SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;
    private static final String PSU_ID = "Test psuId";
    private static final PsuIdData PSU_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final String CONSENT_ID = "Test consentId";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "Test authorisationId";
    private static final String INTERNAL_REQUEST_ID = "Internal request id";
    private static final String TPP_REDIRECT_URI = "tpp redirect uri";
    private static final String TPP_NOK_REDIRECT_URI = "tpp nok redirect uri";

    @InjectMocks
    private RedirectPiisAuthorizationService authorizationService;
    @Mock
    private Xs2aPiisConsentService xs2aPiisConsentService;
    @Mock
    private Xs2aConsentService consentService;
    @Mock
    private PiisConsent consent;
    @Mock
    private Xs2aConsentAuthorisationMapper xs2aConsentAuthorisationMapper;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private RequestProviderService requestProviderService;

    @Test
    void createConsentAuthorization_wrongConsentId_fail() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .consentId(WRONG_CONSENT_ID)
                                                                            .psuData(PSU_DATA)
                                                                            .scaStatus(STARTED_SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        //When
        Optional<CreateConsentAuthorizationResponse> actualResponse = authorizationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);

        //Then
        assertThat(actualResponse).isNotPresent();
    }

    @Test
    void createConsentAuthorization_Success() {
        //Given
        when(xs2aConsentAuthorisationMapper.mapToAuthorisationRequest(AUTHORISATION_ID, STARTED_SCA_STATUS, PSU_DATA, SCA_APPROACH, TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI)).thenReturn(getTestCreateAuthRequest());
        when(consentService.createConsentAuthorisation(CONSENT_ID, getTestCreateAuthRequest()))
            .thenReturn(Optional.of(buildCreateAuthorisationResponse()));
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(consent));
        when(requestProviderService.getTppRedirectURI()).thenReturn(TPP_REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI()).thenReturn(TPP_NOK_REDIRECT_URI);
        Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .consentId(CONSENT_ID)
                                                                            .psuData(PSU_DATA)
                                                                            .scaStatus(STARTED_SCA_STATUS)
                                                                            .scaApproach(SCA_APPROACH)
                                                                            .authorisationId(AUTHORISATION_ID)
                                                                            .build();
        //When
        Optional<CreateConsentAuthorizationResponse> actualResponseOptional = authorizationService.createConsentAuthorization(xs2aCreateAuthorisationRequest);
        //Then
        assertThat(actualResponseOptional).isPresent();

        CreateConsentAuthorizationResponse actualResponse = actualResponseOptional.get();

        assertThat(actualResponse.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(actualResponse.getAuthorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(STARTED_SCA_STATUS);
        assertThat(actualResponse.getPsuIdData()).isEqualTo(PSU_DATA);
        assertThat(actualResponse.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
    }

    @Test
    void getScaApproachServiceType() {
        assertThat(authorizationService.getScaApproachServiceType()).isEqualTo(ScaApproach.REDIRECT);
    }

    @Test
    void getAuthorisationScaStatus() {
        //Given
        ScaStatus scaStatus = ScaStatus.RECEIVED;
        when(consentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(scaStatus));
        //When
        Optional<ScaStatus> authorisationScaStatus = authorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        //Then
        verify(consentService).getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        assertThat(authorisationScaStatus).isPresent().contains(scaStatus);
    }

    private CreateAuthorisationResponse buildCreateAuthorisationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, STARTED_XS2A_SCA_STATUS, INTERNAL_REQUEST_ID, null, SCA_APPROACH);
    }

    private CreateAuthorisationRequest getTestCreateAuthRequest() {
        CreateAuthorisationRequest consentAuthorization = new CreateAuthorisationRequest();
        consentAuthorization.setScaStatus(STARTED_SCA_STATUS);
        consentAuthorization.setAuthorisationId(AUTHORISATION_ID);
        consentAuthorization.setPsuData(PSU_DATA);
        consentAuthorization.setScaApproach(SCA_APPROACH);
        return consentAuthorization;
    }
}
