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


package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisEndpointAccessCheckerServiceTest {
    private static final boolean CONFIRMATION_CODE_RECEIVED_FALSE = false;
    private static final String AUTHORISATION_ID = "11111111";

    @InjectMocks
    private PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @Test
    void isEndpointAccessible_InitiationAuthorisation_ShouldAccessible_EmptyResponse_True() {
        //When
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .error(CmsError.TECHNICAL_ERROR)
                            .build());

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE);

        //Then
        assertTrue(endpointAccessible);
    }

    @Test
    void isEndpointAccessible_InitiationAuthorisation_ShouldAccessible__Redirect_False() {
        //When
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(buildGetPisAuthorisationResponse(ScaApproach.REDIRECT));

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE);

        //Then
        assertFalse(endpointAccessible);
    }

    @Test
    void isEndpointAccessible_InitiationAuthorisation_ShouldBeNotAccessible_Decoupled_False() {
        //When
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(buildGetPisAuthorisationResponse(ScaApproach.DECOUPLED));

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE);

        //Then
        assertFalse(endpointAccessible);
    }

    @Test
    void isEndpointAccessible_oauthAndConfirmationRequestMandated_true() {
        when(aspspProfileService.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        when(aspspProfileService.isAuthorisationConfirmationRequestMandated()).thenReturn(true);

        boolean actual = pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertTrue(actual);

        verify(authorisationServiceEncrypted, never()).getAuthorisationById(anyString());
    }

    @Test
    void isEndpointAccessible_oauthAndNotConfirmationRequestMandated_false() {
        when(aspspProfileService.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        when(aspspProfileService.isAuthorisationConfirmationRequestMandated()).thenReturn(false);

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(buildGetPisAuthorisationResponse(ScaApproach.REDIRECT));

        boolean actual = pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertFalse(actual);

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationById(AUTHORISATION_ID);
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
