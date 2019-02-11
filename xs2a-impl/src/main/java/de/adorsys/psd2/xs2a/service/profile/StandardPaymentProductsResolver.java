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

package de.adorsys.psd2.xs2a.service.profile;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Checks if provided payment product belongs to the pre-defined list of standard payment products,
 * handled by JSON-Object mapping.<br>
 * See "5.3.1 Payment Initiation with JSON encoding of the Payment Instruction" of Implementation Guidelines
 */
@Component
public class StandardPaymentProductsResolver {
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
