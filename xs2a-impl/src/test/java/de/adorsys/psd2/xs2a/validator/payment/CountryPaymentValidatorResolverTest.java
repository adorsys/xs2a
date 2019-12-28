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

package de.adorsys.psd2.xs2a.validator.payment;

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CountryPaymentValidatorResolverTest {

    @Autowired
    private CountryPaymentValidatorResolver resolver;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Before
    public void setUp() {
        DefaultPaymentValidatorHolder defaultPaymentValidatorHolder = new DefaultPaymentValidatorHolder(null, null);
        AustriaPaymentValidatorHolder austriaPaymentValidatorHolder = new AustriaPaymentValidatorHolder(null, null);
        resolver = new CountryPaymentValidatorResolver(aspspProfileServiceWrapper,
                                                       Arrays.asList(defaultPaymentValidatorHolder, austriaPaymentValidatorHolder));
    }

    @Test
    public void getValidationConfig_nullValue() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn(null);
        assertTrue(resolver.getCountryValidatorHolder() instanceof DefaultPaymentValidatorHolder);
    }

    @Test
    public void getValidationConfig_emptyValue() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn("");
        assertTrue(resolver.getCountryValidatorHolder() instanceof DefaultPaymentValidatorHolder);
    }

    @Test
    public void getValidationConfig_wrongValue() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn("wrong value");
        assertTrue(resolver.getCountryValidatorHolder() instanceof DefaultPaymentValidatorHolder);
    }

    @Test
    public void getValidationConfig_DE() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn("dE");
        assertTrue(resolver.getCountryValidatorHolder() instanceof DefaultPaymentValidatorHolder);
    }

    @Test
    public void getValidationConfig_AT() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn("At");
        assertTrue(resolver.getCountryValidatorHolder() instanceof AustriaPaymentValidatorHolder);
    }
}
