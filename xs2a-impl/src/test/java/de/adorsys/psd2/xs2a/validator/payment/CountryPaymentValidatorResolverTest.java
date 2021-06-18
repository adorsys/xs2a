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

package de.adorsys.psd2.xs2a.validator.payment;

import de.adorsys.psd2.validator.payment.CountryValidatorHolder;
import de.adorsys.psd2.validator.payment.PaymentBodyFieldsValidator;
import de.adorsys.psd2.validator.payment.PaymentBusinessValidator;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.raw.DefaultPaymentBusinessValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.AustriaPaymentBodyFieldsValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.DefaultPaymentBodyFieldsValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryPaymentValidatorResolverTest {
    private static final String AUSTRIA_CODE = "At";
    private static final String GERMANY_CODE = "dE";
    private static final String WRONG_CODE = "wrong value";
    private CountryPaymentValidatorResolver resolver;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @BeforeEach
    void setUp() {
        DefaultPaymentValidatorHolder defaultPaymentValidatorHolder =
            new DefaultPaymentValidatorHolder(new DefaultPaymentBodyFieldsValidatorImpl(null, null), new DefaultPaymentBusinessValidatorImpl(null, null, null));
        AustriaPaymentValidatorHolder austriaPaymentValidatorHolder =
            new AustriaPaymentValidatorHolder(new AustriaPaymentBodyFieldsValidatorImpl(null, null), new DefaultPaymentBusinessValidatorImpl(null, null, null));
        resolver = new CountryPaymentValidatorResolver(aspspProfileServiceWrapper,
                                                       Arrays.asList(defaultPaymentValidatorHolder, austriaPaymentValidatorHolder));
    }

    @ParameterizedTest
    @ValueSource(strings = {StringUtils.EMPTY, WRONG_CODE, GERMANY_CODE})
    @NullSource
    void getValidationConfig_default(String value) {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn(value);
        assertTrue(resolver.getCountryValidatorHolder() instanceof DefaultPaymentValidatorHolder);
    }

    @Test
    void getValidationConfig_AT() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn(AUSTRIA_CODE);
        assertTrue(resolver.getCountryValidatorHolder() instanceof AustriaPaymentValidatorHolder);
    }

    @ParameterizedTest
    @ValueSource(strings = {StringUtils.EMPTY, WRONG_CODE, GERMANY_CODE})
    @NullSource
    void getPaymentBodyFieldValidator_default(String value) {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn(value);
        assertTrue(resolver.getPaymentBodyFieldValidator() instanceof DefaultPaymentBodyFieldsValidatorImpl);
    }

    @Test
    void getPaymentBodyFieldValidator_AT() {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn(AUSTRIA_CODE);
        assertTrue(resolver.getPaymentBodyFieldValidator() instanceof AustriaPaymentBodyFieldsValidatorImpl);
    }

    @ParameterizedTest
    @ValueSource(strings = {StringUtils.EMPTY, WRONG_CODE, GERMANY_CODE, AUSTRIA_CODE})
    @NullSource
    void getPaymentBusinessValidator_default(String value) {
        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn(value);
        assertTrue(resolver.getPaymentBusinessValidator() instanceof DefaultPaymentBusinessValidatorImpl);
    }

    @Test
    void customHoldersOverridePredefinedOnes() {
        DefaultPaymentValidatorHolder defaultPaymentValidatorHolder = new DefaultPaymentValidatorHolder(null, null);
        AustriaPaymentValidatorHolder austriaPaymentValidatorHolder = new AustriaPaymentValidatorHolder(null, null);
        CountryValidatorHolder customCountryValidatorHolder = new CountryValidatorHolder() {
            @Override
            public String getCountryIdentifier() {
                return defaultPaymentValidatorHolder.getCountryIdentifier();
            }

            @Override
            public PaymentBodyFieldsValidator getPaymentBodyFieldsValidator() {
                return defaultPaymentValidatorHolder.getPaymentBodyFieldsValidator();
            }

            @Override
            public PaymentBusinessValidator getPaymentBusinessValidator() {
                return defaultPaymentValidatorHolder.getPaymentBusinessValidator();
            }
        };
        resolver = new CountryPaymentValidatorResolver(aspspProfileServiceWrapper,
                                                       Arrays.asList(customCountryValidatorHolder, defaultPaymentValidatorHolder, austriaPaymentValidatorHolder));

        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn(GERMANY_CODE);
        assertEquals(customCountryValidatorHolder, resolver.getCountryValidatorHolder());
        reset(aspspProfileServiceWrapper);

        when(aspspProfileServiceWrapper.getSupportedPaymentCountryValidation()).thenReturn(AUSTRIA_CODE);
        assertEquals(austriaPaymentValidatorHolder, resolver.getCountryValidatorHolder());
    }
}
