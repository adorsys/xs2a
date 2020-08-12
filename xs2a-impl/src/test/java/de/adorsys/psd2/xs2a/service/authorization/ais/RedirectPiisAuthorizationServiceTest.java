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
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.authorization.piis.RedirectPiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedirectPiisAuthorizationServiceTest {
    private static final ScaStatus STARTED_SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaStatus STARTED_XS2A_SCA_STATUS = ScaStatus.RECEIVED;
    private static final String PSU_ID = "Test psuId";
    private static final PsuIdData PSU_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final String CONSENT_ID = "Test consentId";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "Test authorisationId";
    private static final String INTERNAL_REQUEST_ID = "Internal request id";

    @InjectMocks
    private RedirectPiisAuthorizationService authorizationService;
    @Mock
    private Xs2aPiisConsentService xs2aPiisConsentService;
    @Mock
    private Xs2aConsentService consentService;
    @Mock
    private PiisConsent consent;

    @Test
    void createConsentAuthorization_wrongConsentId_fail() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        //When
        Optional<CreateConsentAuthorizationResponse> actualResponse = authorizationService.createConsentAuthorization(PSU_DATA, WRONG_CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void createConsentAuthorization_Success() {
        //Given
        when(consentService.createConsentAuthorisation(CONSENT_ID, STARTED_XS2A_SCA_STATUS, PSU_DATA))
            .thenReturn(Optional.of(buildCreateAuthorisationResponse()));
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID)).thenReturn(Optional.of(consent));
        //When
        Optional<CreateConsentAuthorizationResponse> actualResponseOptional = authorizationService.createConsentAuthorization(PSU_DATA, CONSENT_ID);
        //Then
        assertThat(actualResponseOptional.isPresent()).isTrue();

        CreateConsentAuthorizationResponse actualResponse = actualResponseOptional.get();

        assertThat(actualResponse.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(actualResponse.getAuthorisationId()).isEqualTo(AUTHORISATION_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(STARTED_SCA_STATUS);
        assertThat(actualResponse.getPsuIdData()).isEqualTo(PSU_DATA);
        assertThat(actualResponse.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
    }

    @Test
    void getScaApproachServiceType() {
        assertEquals(ScaApproach.REDIRECT, authorizationService.getScaApproachServiceType());
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
        assert(authorisationScaStatus).isPresent();
        assertEquals(scaStatus, authorisationScaStatus.get());
    }

    private CreateAuthorisationResponse buildCreateAuthorisationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, STARTED_XS2A_SCA_STATUS, INTERNAL_REQUEST_ID, null);
    }
}
