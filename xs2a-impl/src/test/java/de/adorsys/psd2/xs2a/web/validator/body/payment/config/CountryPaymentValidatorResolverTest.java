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

package de.adorsys.psd2.xs2a.web.validator.body.payment.config;

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CountryPaymentValidatorResolverTest {

    @InjectMocks
    private CountryPaymentValidatorResolver resolver;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Test
    public void getValidationConfig_nullValue() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn(null);
        assertTrue(resolver.getValidationConfig() instanceof DefaultPaymentValidationConfigImpl);
    }

    @Test
    public void getValidationConfig_emptyValue() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn("");
        assertTrue(resolver.getValidationConfig() instanceof DefaultPaymentValidationConfigImpl);
    }

    @Test
    public void getValidationConfig_wrongValue() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn("wrong value");
        assertTrue(resolver.getValidationConfig() instanceof DefaultPaymentValidationConfigImpl);
    }

    @Test
    public void getValidationConfig_DE() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn("dE");
        assertTrue(resolver.getValidationConfig() instanceof DefaultPaymentValidationConfigImpl);
    }

    @Test
    public void getValidationConfig_AT() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn("At");
        assertTrue(resolver.getValidationConfig() instanceof AustriaValidationConfigImpl);
    }

    @Test
    public void getValidationConfig_CH() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn("ch");
        assertTrue(resolver.getValidationConfig() instanceof SwitzerlandValidationConfigImpl);
    }
}
