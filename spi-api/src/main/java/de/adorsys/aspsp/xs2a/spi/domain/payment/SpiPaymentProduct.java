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

package de.adorsys.aspsp.xs2a.spi.domain.payment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum SpiPaymentProduct {
    SEPA("sepa-credit-transfers"),
    INSTANT_SEPA("instant-sepa-credit-transfers"),
    TARGET2("target-2-payments"),
    CROSS_BORDER("cross-border-credit-transfers");

    private String value;

    private static Map<String, SpiPaymentProduct> container = new HashMap<>();

    static {
        Arrays.stream(values())
            .forEach(product -> container.put(product.getValue(), product));
    }

    SpiPaymentProduct(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SpiPaymentProduct getByValue(String value) {
        return container.get(value);
    }
}
