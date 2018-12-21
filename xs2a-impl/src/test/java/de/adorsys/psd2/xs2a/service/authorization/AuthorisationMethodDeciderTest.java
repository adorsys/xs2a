/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationMethodDeciderTest {

    @InjectMocks
    private AuthorisationMethodDecider authorisationMethodDecider;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @Test
    public void isExplicitMethod_Success_TppExplicitPrefferedTrue_SigningBasketTrue() {
        //Given
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred);

        //Then
        assertTrue(actualResult);
    }

    @Test
    public void isExplicitMethod_Fail_TppExplicitPrefferedFalse_SigningBasketTrue() {
        //Given
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred);

        //Then
        assertFalse(actualResult);
    }

    @Test
    public void isExplicitMethod_Fail_TppExplicitPrefferedFalse_SigningBasketFalse() {
        //Given
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(false);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred);

        //Then
        assertFalse(actualResult);
    }

    @Test
    public void isExplicitMethod_Fail_TppExplicitPrefferedTrue_SigningBasketFalse() {
        //Given
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(false);

        //When
        boolean actualResult = authorisationMethodDecider.isExplicitMethod(tppExplicitAuthorisationPreferred);

        //Then
        assertFalse(actualResult);
    }

    @Test
    public void isImplicitMethod_Fail_TppExplicitPrefferedTrue_SigningBasketTrue() {
        //Given
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(false);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred);

        //Then
        assertTrue(actualResult);
    }

    @Test
    public void isImplicitMethod_Success_TppExplicitPrefferedFalse_SigningBasketTrue() {
        //Given
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred);

        //Then
        assertTrue(actualResult);
    }

    @Test
    public void isImplicitMethod_Success_TppExplicitPrefferedFalse_SigningBasketFalse() {
        //Given
        boolean tppExplicitAuthorisationPreferred = false;
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(false);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred);

        //Then
        assertTrue(actualResult);
    }

    @Test
    public void isImplicitMethod_Success_TppExplicitPrefferedTrue_SigningBasketFalse() {
        //Given
        boolean tppExplicitAuthorisationPreferred = true;
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(false);

        //When
        boolean actualResult = authorisationMethodDecider.isImplicitMethod(tppExplicitAuthorisationPreferred);

        //Then
        assertTrue(actualResult);
    }
}
