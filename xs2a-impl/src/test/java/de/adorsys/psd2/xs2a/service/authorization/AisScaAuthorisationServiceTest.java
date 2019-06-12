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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AisScaAuthorisationServiceTest {
    @InjectMocks
    private AisScaAuthorisationService aisScaAuthorisationService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Test
    public void isOneFactorAuthorisation_AllAvailableTrue_OneAccessTypeTrue_ScaRequiredFalse() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired()).thenReturn(false);
        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(true, true);
        //Then
        assertTrue(oneFactorAuthorisation);
    }

    @Test
    public void isOneFactorAuthorisation_AllAvailableTrue_OneAccessTypeTrue_ScaRequiredTrue() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired()).thenReturn(true);
        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(true, true);
        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    public void isOneFactorAuthorisation_AllAvailableFalse_OneAccessTypeTrue_ScaRequiredTrue() {
        //Given
        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(false, true);
        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    public void isOneFactorAuthorisation_AllAvailableFalse_OneAccessTypeFalse_ScaRequiredTrue() {
        //Given
        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(false, false);
        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    public void isOneFactorAuthorisation_AllAvailableFalse_OneAccessTypeTrue_ScaRequiredFalse() {
        //Given
        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(false, true);
        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    public void isOneFactorAuthorisation_AllAvailableFalse_OneAccessTypeFalse_ScaRequiredFalse() {
        //Given
        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(false, false);
        //Then
        assertFalse(oneFactorAuthorisation);
    }
}
