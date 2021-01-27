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
