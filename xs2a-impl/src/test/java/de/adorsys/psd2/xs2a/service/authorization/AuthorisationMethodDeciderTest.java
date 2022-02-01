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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationMethodDeciderTest {

    @InjectMocks
    private AuthorisationMethodDecider authorisationMethodDecider;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isExplicitMethod_Success(boolean tppExplicitAuthorisationPreferred) {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, true);

        //Then
        assertTrue(actualResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isImplicitMethod_Fail(boolean tppExplicitAuthorisationPreferred) {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred, true);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isExplicitMethod_Success_TppExplicitPreferredTrue_SigningBasketTrue() {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);
        when(aspspProfileService.isSigningBasketSupported()).thenReturn(true);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(true, false);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isExplicitMethod_Fail_TppExplicitPreferredFalse_SigningBasketTrue() {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(false, false);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isExplicitMethod_Fail_TppExplicitPreferredTrue_SigningBasketFalse() {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);
        when(aspspProfileService.isSigningBasketSupported()).thenReturn(false);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(true, false);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isExplicitMethod_Success_ExplicitAuthorisationMode() {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.EXPLICIT);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(true, true);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isExplicitMethod_Fail_ImplicitAuthorisationMode() {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.IMPLICIT);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(true, true);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isExplicitMethod_Fail_TppExplicitPreferredFalse_SigningBasketFalse() {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(false, false);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isImplicitMethod_Fail_TppExplicitPreferredTrue_SigningBasketTrue() {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);
        when(aspspProfileService.isSigningBasketSupported()).thenReturn(true);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(true, false);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isImplicitMethod_Success_TppExplicitPreferredFalse_SigningBasketTrue() {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(false, false);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isImplicitMethod_Success_TppExplicitPreferredTrue_SigningBasketFalse() {
        //Given
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(true, false);

        //Then
        assertTrue(actualResult);
    }
}
