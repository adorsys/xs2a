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

package de.adorsys.psd2.xs2a.service.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardPaymentProductsResolverTest {
    private StandardPaymentProductsResolver standardPaymentProductsResolver;

    @BeforeEach
    void setUp() {
        standardPaymentProductsResolver = new StandardPaymentProductsResolver();
    }

    @Test
    void isRawPaymentProduct() {
        assertFalse(standardPaymentProductsResolver.isRawPaymentProduct("instant-sepa-credit-transfers"));
        assertFalse(standardPaymentProductsResolver.isRawPaymentProduct("target-2-payments"));
        assertFalse(standardPaymentProductsResolver.isRawPaymentProduct("cross-border-credit-transfers"));
        assertFalse(standardPaymentProductsResolver.isRawPaymentProduct("sepa-credit-transfers"));
        assertTrue(standardPaymentProductsResolver.isRawPaymentProduct("pain"));
        assertTrue(standardPaymentProductsResolver.isRawPaymentProduct("dtazv"));
    }
}
