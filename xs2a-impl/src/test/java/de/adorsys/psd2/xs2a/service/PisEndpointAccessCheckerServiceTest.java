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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisEndpointAccessCheckerServiceTest {
    @InjectMocks
    private PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @Test
    public void isEndpointAccessible_ShouldAccessible_EmptyResponse_True() {
        //When
        when(pisCommonPaymentServiceEncrypted.getPisAuthorisationById(anyString()))
            .thenReturn(Optional.empty());

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(anyString());

        //Then
        assertTrue(endpointAccessible);
    }

    @Test
    public void isEndpointAccessible_ShouldAccessible__Redirect_False() {
        //When
        when(pisCommonPaymentServiceEncrypted.getPisAuthorisationById(anyString()))
            .thenReturn(buildGetPisAuthorisationResponse(ScaApproach.REDIRECT));

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(anyString());

        //Then
        assertFalse(endpointAccessible);
    }

    @Test
    public void isEndpointAccessible_ShouldAccessible_Decoupled_True() {
        //When
        when(pisCommonPaymentServiceEncrypted.getPisAuthorisationById(anyString()))
            .thenReturn(buildGetPisAuthorisationResponse(ScaApproach.DECOUPLED));

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(anyString());

        //Then
        assertTrue(endpointAccessible);
    }

    @Test
    public void isEndpointAccessible_ShouldAccessible_Decoupled_ScaMethodSelected_False() {
        //When
        when(pisCommonPaymentServiceEncrypted.getPisAuthorisationById(anyString()))
            .thenReturn(buildGetPisAuthorisationResponse(ScaApproach.DECOUPLED, ScaStatus.SCAMETHODSELECTED));

        boolean endpointAccessible = pisEndpointAccessCheckerService.isEndpointAccessible(anyString());

        //Then
        assertFalse(endpointAccessible);
    }

    private Optional<GetPisAuthorisationResponse> buildGetPisAuthorisationResponse(ScaApproach scaApproach) {
        return buildGetPisAuthorisationResponse(scaApproach, null);
    }

    private Optional<GetPisAuthorisationResponse> buildGetPisAuthorisationResponse(ScaApproach scaApproach, ScaStatus scaStatus) {
        GetPisAuthorisationResponse response = new GetPisAuthorisationResponse();
        response.setChosenScaApproach(scaApproach);
        response.setScaStatus(scaStatus);
        return Optional.of(response);
    }
}
