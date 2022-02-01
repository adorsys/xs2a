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

package de.adorsys.psd2.mapper;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Checks if provided payment product belongs to the pre-defined list of standard payment products,
 * handled by JSON-Object mapping.<br>
 * See "5.3.1 Payment Initiation with JSON encoding of the Payment Instruction" of Implementation Guidelines
 */
@Component
public class CmsStandardPaymentProductsResolver {
    private static final Set<String> STANDARD_PAYMENT_PRODUCTS = new HashSet<>(4);

    static {
        STANDARD_PAYMENT_PRODUCTS.add("sepa-credit-transfers");
        STANDARD_PAYMENT_PRODUCTS.add("instant-sepa-credit-transfers");
        STANDARD_PAYMENT_PRODUCTS.add("target-2-payments");
        STANDARD_PAYMENT_PRODUCTS.add("cross-border-credit-transfers");
    }

    public boolean isRawPaymentProduct(String paymentProduct) {
        return !isStandardPaymentProduct(paymentProduct);
    }

    private boolean isStandardPaymentProduct(String paymentProduct) {
        return STANDARD_PAYMENT_PRODUCTS.contains(paymentProduct);
    }
}
