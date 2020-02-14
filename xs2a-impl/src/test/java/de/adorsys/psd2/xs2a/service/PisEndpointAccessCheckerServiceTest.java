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


package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisEndpointAccessCheckerServiceTest {
    private static final boolean CONFIRMATION_CODE_RECEIVED_FALSE = false;

    @InjectMocks
    private PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;

    @Test
    void isEndpointAccessible_InitiationAuthorisation_ShouldAccessible_EmptyResponse_True() {
        //When
        when(authorisationServiceEncrypted.getAuthorisationById(anyString()))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .error(CmsError.TECHNICAL_ERROR)
                            .build());

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(anyString(), CONFIRMATION_CODE_RECEIVED_FALSE);

        //Then
        assertTrue(endpointAccessible);
    }

    @Test
    void isEndpointAccessible_InitiationAuthorisation_ShouldAccessible__Redirect_False() {
        //When
        when(authorisationServiceEncrypted.getAuthorisationById(anyString()))
            .thenReturn(buildGetPisAuthorisationResponse(ScaApproach.REDIRECT));

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(anyString(), CONFIRMATION_CODE_RECEIVED_FALSE);

        //Then
        assertFalse(endpointAccessible);
    }

    @Test
    void isEndpointAccessible_InitiationAuthorisation_ShouldAccessible_Decoupled_True() {
        //When
        when(authorisationServiceEncrypted.getAuthorisationById(anyString()))
            .thenReturn(buildGetPisAuthorisationResponse(ScaApproach.DECOUPLED));

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(anyString(), CONFIRMATION_CODE_RECEIVED_FALSE);

        //Then
        assertTrue(endpointAccessible);
    }

    private CmsResponse<Authorisation> buildGetPisAuthorisationResponse(ScaApproach scaApproach) {
        return buildGetPisAuthorisationResponse(scaApproach, null);
    }

    private CmsResponse<Authorisation> buildGetPisAuthorisationResponse(ScaApproach scaApproach, ScaStatus scaStatus) {
        Authorisation response = new Authorisation();
        response.setChosenScaApproach(scaApproach);
        response.setScaStatus(scaStatus);
        return CmsResponse.<Authorisation>builder()
                   .payload(response)
                   .build();
    }
}
