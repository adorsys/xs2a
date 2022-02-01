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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.TppService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TppInfoCheckerServiceTest {
    private static final String AUTHORISATION_NUMBER = "authorisation number";
    private static final String DIFFERENT_AUTHORISATION_NUMBER = "different authorisation number";

    @Mock
    private TppService tppService;

    @InjectMocks
    private TppInfoCheckerService tppInfoCheckerService;

    @Test
    void differsFromTppInRequest_withDifferentTppInRequest_shouldReturnTrue() {
        // Given
        TppInfo tppInfoInRequest = buildTppInfo(AUTHORISATION_NUMBER);
        when(tppService.getTppInfo()).thenReturn(tppInfoInRequest);

        TppInfo tppInfo = buildTppInfo(DIFFERENT_AUTHORISATION_NUMBER);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertTrue(result);
    }

    @Test
    void differsFromTppInRequest_withNullValuesInRequest_shouldReturnTrue() {
        // Given
        TppInfo tppInfoInRequest = buildTppInfo(null);
        when(tppService.getTppInfo()).thenReturn(tppInfoInRequest);

        TppInfo tppInfo = buildTppInfo(DIFFERENT_AUTHORISATION_NUMBER);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertTrue(result);
    }

    @Test
    void differsFromTppInRequest_withSameTppInRequest_shouldReturnFalse() {
        // Given
        TppInfo tppInfo = buildTppInfo(AUTHORISATION_NUMBER);
        when(tppService.getTppInfo()).thenReturn(tppInfo);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertFalse(result);
    }

    @Test
    void differsFromTppInRequest_withNullTppInfo_shouldReturnTrue() {
        // Given

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(null);

        // Then
        assertTrue(result);
    }

    @Test
    void differsFromTppInRequest_withNullTppAuthorisationNumber_shouldReturnTrue() {
        // Given
        TppInfo tppInfo = buildTppInfo(null);

        // When
        boolean result = tppInfoCheckerService.differsFromTppInRequest(tppInfo);

        // Then
        assertTrue(result);
    }

    private TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }
}
