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

import de.adorsys.psd2.xs2a.service.validator.pis.payment.raw.DefaultPaymentBusinessValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.AustriaPaymentBodyFieldsValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AustriaPaymentValidatorHolderTest {

    private AustriaPaymentValidatorHolder holder;

    @BeforeEach
    void setUp() {
        holder = new AustriaPaymentValidatorHolder(new AustriaPaymentBodyFieldsValidatorImpl(null, null),
                                                   new DefaultPaymentBusinessValidatorImpl(null, null, null));
    }

    @Test
    void getCountryIdentifier() {
        assertEquals("AT", holder.getCountryIdentifier());
    }

    @Test
    void getPaymentBodyFieldsValidator() {
        assertTrue(holder.getPaymentBodyFieldsValidator() instanceof AustriaPaymentBodyFieldsValidatorImpl);
    }

    @Test
    void getPaymentBusinessValidator() {
        assertTrue(holder.getPaymentBusinessValidator() instanceof DefaultPaymentBusinessValidatorImpl);
    }

    @Test
    void isCustom() {
        assertFalse(holder.isCustom());
    }
}
