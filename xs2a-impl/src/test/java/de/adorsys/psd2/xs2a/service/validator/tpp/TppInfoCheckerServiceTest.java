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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.TppService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TppInfoCheckerServiceTest {
    private static final String AUTHORISATION_NUMBER = "authorisation number";
    private static final String AUTHORITY_ID = "authority id";
    private static final String DIFFERENT_AUTHORISATION_NUMBER = "different authorisation number";
    private static final String DIFFERENT_AUTHORITY_ID = "different authority id";

    @Mock
    private TppService tppService;

    @InjectMocks
    private TppInfoCheckerService tppInfoCheckerService;

    @Test
    public void differsFromTppInRequest_withDifferentTppInRequest_shouldReturnTrue() {
        // Given
        TppInfo tppInfoInRequest = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        when(tppService.getTppInfo()).thenReturn(tppInfoInRequest);

        TppInfo tppInfo = buildTppInfo(DIFFERENT_AUTHORISATION_NUMBER, DIFFERENT_AUTHORITY_ID);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertTrue(result);
    }

    @Test
    public void differsFromTppInRequest_withSameAuthorisationNumber_shouldReturnTrue() {
        // Given
        TppInfo tppInfoInRequest = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        when(tppService.getTppInfo()).thenReturn(tppInfoInRequest);

        TppInfo tppInfo = buildTppInfo(AUTHORISATION_NUMBER, DIFFERENT_AUTHORITY_ID);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertTrue(result);
    }

    @Test
    public void differsFromTppInRequest_withSameAuthorityId_shouldReturnTrue() {
        // Given
        TppInfo tppInfoInRequest = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        when(tppService.getTppInfo()).thenReturn(tppInfoInRequest);

        TppInfo tppInfo = buildTppInfo(DIFFERENT_AUTHORISATION_NUMBER, AUTHORITY_ID);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertTrue(result);
    }

    @Test
    public void differsFromTppInRequest_withNullValuesInRequest_shouldReturnTrue() {
        // Given
        TppInfo tppInfoInRequest = buildTppInfo(null, null);
        when(tppService.getTppInfo()).thenReturn(tppInfoInRequest);

        TppInfo tppInfo = buildTppInfo(DIFFERENT_AUTHORISATION_NUMBER, DIFFERENT_AUTHORITY_ID);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertTrue(result);
    }

    @Test
    public void differsFromTppInRequest_withSameTppInRequest_shouldReturnFalse() {
        // Given
        TppInfo tppInfo = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        when(tppService.getTppInfo()).thenReturn(tppInfo);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertFalse(result);
    }

    @Test
    public void differsFromTppInRequest_withNullTppInfo_shouldReturnTrue() {
        // Given
        TppInfo tppInfoInRequest = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        when(tppService.getTppInfo()).thenReturn(tppInfoInRequest);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(null);

        // Then
        assertTrue(result);
    }

    @Test
    public void differsFromTppInRequest_withNullTppAuthorisationNumber_shouldReturnTrue() {
        // Given
        TppInfo tppInfoInRequest = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        when(tppService.getTppInfo()).thenReturn(tppInfoInRequest);

        TppInfo tppInfo = buildTppInfo(null, DIFFERENT_AUTHORITY_ID);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertTrue(result);
    }

    @Test
    public void differsFromTppInRequest_withNullTppAuthorityId_shouldReturnTrue() {
        // Given
        TppInfo tppInfoInRequest = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        when(tppService.getTppInfo()).thenReturn(tppInfoInRequest);

        TppInfo tppInfo = buildTppInfo(DIFFERENT_AUTHORISATION_NUMBER, null);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertTrue(result);
    }

    private TppInfo buildTppInfo(String authorisationNumber, String authorityId) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        tppInfo.setAuthorityId(authorityId);
        return tppInfo;
    }
}
