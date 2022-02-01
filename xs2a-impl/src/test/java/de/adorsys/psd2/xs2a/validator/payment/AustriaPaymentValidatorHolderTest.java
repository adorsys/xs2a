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
