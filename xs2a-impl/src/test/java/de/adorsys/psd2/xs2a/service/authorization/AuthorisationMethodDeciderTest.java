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
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationMethodDeciderTest {

    @InjectMocks
    private AuthorisationMethodDecider authorisationMethodDecider;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;

    @BeforeEach
    void setUp() {
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
    }

    //Multilevel = true
    @Test
    void isExplicitMethod_Success_Multilevel_TppExplicitPreferredTrue_SigningBasketTrue() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isExplicitMethod_Success_Multilevel_TppExplicitPreferredFalse_SigningBasketTrue() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isExplicitMethod_Success_Multilevel_TppExplicitPreferredTrue_SigningBasketFalse() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isExplicitMethod_Success_Multilevel_TppExplicitPreferredFalse_SigningBasketFalse() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isImplicitMethod_Fail_Multilevel_TppExplicitPreferredTrue_SigningBasketTrue() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isImplicitMethod_Fail_Multilevel_TppExplicitPreferredFalse_SigningBasketTrue() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isImplicitMethod_Fail_Multilevel_TppExplicitPreferredTrue_SigningBasketFalse() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isImplicitMethod_Fail_Multilevel_TppExplicitPreferredFalse_SigningBasketFalse() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertFalse(actualResult);
    }

    //Multilevel false
    @Test
    void isExplicitMethod_Success_TppExplicitPreferredTrue_SigningBasketTrue() {
        //Given
        boolean multilevelScaRequired = false;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);
        when(aspspProfileService.isSigningBasketSupported()).thenReturn(true);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isExplicitMethod_Fail_TppExplicitPreferredFalse_SigningBasketTrue() {
        //Given
        boolean multilevelScaRequired = false;
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isExplicitMethod_Fail_TppExplicitPreferredTrue_SigningBasketFalse() {
        //Given
        boolean multilevelScaRequired = false;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);
        when(aspspProfileService.isSigningBasketSupported()).thenReturn(false);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isExplicitMethod_Success_ExplicitAuthorisationMode() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.EXPLICIT);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isExplicitMethod_Fail_ImplicitAuthorisationMode() {
        //Given
        boolean multilevelScaRequired = true;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.IMPLICIT);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isExplicitMethod_Fail_TppExplicitPreferredFalse_SigningBasketFalse() {
        //Given
        boolean multilevelScaRequired = false;
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isImplicitMethod_Fail_TppExplicitPreferredTrue_SigningBasketTrue() {
        //Given
        boolean multilevelScaRequired = false;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);
        when(aspspProfileService.isSigningBasketSupported()).thenReturn(true);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertFalse(actualResult);
    }

    @Test
    void isImplicitMethod_Success_TppExplicitPreferredFalse_SigningBasketTrue() {
        //Given
        boolean multilevelScaRequired = false;
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertTrue(actualResult);
    }

    @Test
    void isImplicitMethod_Success_TppExplicitPreferredTrue_SigningBasketFalse() {
        //Given
        boolean multilevelScaRequired = false;
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.getStartAuthorisationMode()).thenReturn(StartAuthorisationMode.AUTO);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);

        //Then
        assertTrue(actualResult);
    }
}
