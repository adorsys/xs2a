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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuPisControllerTest {
    private static final String PAYMENT_ID = "payment id";
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String PSU_ID = "psu id";
    private static final String PSU_ID_TYPE = "psu id type";
    private static final String PSU_CORPORATE_ID = "psu corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "psu corporate id type";
    private static final String INSTANCE_ID = "instance id";
    private static final String SCA_STATUS_RECEIVED = "RECEIVED";

    @InjectMocks
    private CmsPsuPisController cmsPsuPisController;

    @Mock
    private CmsPsuPisService cmsPsuPisService;

    @Test
    public void updateAuthorisationStatus_withValidRequest_shouldReturnOk() {
        // Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID))
            .thenReturn(true);

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updateAuthorisationStatus(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED, INSTANCE_ID);

        // Then
        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void updateAuthorisationStatus_withValidRequestAndLowercaseScaStatus_shouldReturnOk() {
        // Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID))
            .thenReturn(true);
        String lowercaseScaStatus = SCA_STATUS_RECEIVED.toLowerCase();

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updateAuthorisationStatus(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, AUTHORISATION_ID, lowercaseScaStatus, INSTANCE_ID);

        // Then
        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void updateAuthorisationStatus_withFalseFromService_shouldReturnBadRequest() {
        // Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID))
            .thenReturn(false);

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updateAuthorisationStatus(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED, INSTANCE_ID);

        // Then
        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void updateAuthorisationStatus_withInvalidScaStatus_shouldReturnBadRequest() {
        // Given
        String invalidScaStatus = "invalid SCA status";

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updateAuthorisationStatus(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, AUTHORISATION_ID, invalidScaStatus, INSTANCE_ID);

        // Then
        verify(cmsPsuPisService, never()).updateAuthorisationStatus(any(), anyString(), anyString(), any(), anyString());

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }
}
