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

package de.adorsys.aspsp.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "PaymentProduct", value = "Payment products of ASPSP")
public enum PaymentProduct {
    SCT("sepa-credit-transfers"),
    ISCT("instant-sepa-credit-transfers"),
    T2P("target-2-payments"),
    CBCT("cross-border-credit-transfers");

    private String code;

    @JsonCreator
    PaymentProduct(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static PaymentProduct forValue(String code) {
        for (PaymentProduct prod : values()) {
            if (prod.code.equals(code)) {
                return prod;
            }
        }
        throw new IllegalArgumentException();
    }
}
