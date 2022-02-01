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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentEndpointAccessCheckerServiceTest {

    private static final String AUTHORISATION_ID = "11111111";

    @InjectMocks
    private ConsentEndpointAccessCheckerService consentEndpointAccessCheckerService;

    @Mock
    private Xs2aAuthorisationService authorisationService;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @Test
    void isEndpointAccessible_Received_false() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated())
            .thenReturn(true);

        when(authorisationService.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAccountConsentAuthorization(ScaStatus.RECEIVED, ScaApproach.REDIRECT)));

        boolean actual = consentEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertFalse(actual);
    }

    @Test
    void isEndpointAccessible_Unconfirmed_true() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated())
            .thenReturn(true);

        when(authorisationService.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAccountConsentAuthorization(ScaStatus.UNCONFIRMED, ScaApproach.REDIRECT)));

        boolean actual = consentEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertTrue(actual);
    }

    @Test
    void isEndpointAccessible_Unconfirmed_Decoupled_false() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated())
            .thenReturn(true);

        when(authorisationService.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAccountConsentAuthorization(ScaStatus.UNCONFIRMED, ScaApproach.DECOUPLED)));

        boolean actual = consentEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertFalse(actual);
    }

    @Test
    void isEndpointAccessible_Unconfirmed_Embedded_true() {

        when(aspspProfileService.isAuthorisationConfirmationRequestMandated()).thenReturn(true);

        when(authorisationService.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAccountConsentAuthorization(ScaStatus.UNCONFIRMED, ScaApproach.EMBEDDED)));

        boolean actual = consentEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertTrue(actual);
    }

    @Test
    void isEndpointAccessible_oauthAndConfirmationRequestMandated_true() {
        when(aspspProfileService.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        when(aspspProfileService.isAuthorisationConfirmationRequestMandated()).thenReturn(true);

        boolean actual = consentEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertTrue(actual);

        verify(authorisationService, never()).getAuthorisationById(anyString());
    }

    @Test
    void isEndpointAccessible_oauthAndNotConfirmationRequestMandated_false() {
        when(aspspProfileService.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.OAUTH);
        when(aspspProfileService.isAuthorisationConfirmationRequestMandated()).thenReturn(false);

        when(authorisationService.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAccountConsentAuthorization(ScaStatus.UNCONFIRMED, ScaApproach.REDIRECT)));

        boolean actual = consentEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, true);

        assertFalse(actual);

        verify(authorisationService, times(1)).getAuthorisationById(AUTHORISATION_ID);
    }

    private Authorisation buildAccountConsentAuthorization(ScaStatus scaStatus, ScaApproach scaApproach) {
        Authorisation authorisation = new Authorisation();
        authorisation.setChosenScaApproach(scaApproach);
        authorisation.setScaStatus(scaStatus);
        return authorisation;
    }

}
