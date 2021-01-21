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

package de.adorsys.psd2.xs2a.web;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enumerator for storing all PIS REST controller method names (i.e. method names from {@link de.adorsys.psd2.api.PaymentApi}.
 */
public enum Psd2PaymentMethodNameConstant {

    CANCEL_PAYMENT("_cancelPayment"),
    GET_PAYMENT_CANCELLATION_SCA_STATUS("_getPaymentCancellationScaStatus"),
    GET_PAYMENT_INFORMATION("_getPaymentInformation"),
    GET_PAYMENT_INITIATION_AUTHORISATION("_getPaymentInitiationAuthorisation"),
    GET_PAYMENT_INITIATION_CANCELLATION_AUTH_INFO("_getPaymentInitiationCancellationAuthorisationInformation"),
    GET_PAYMENT_INITIATION_SCA_STATUS("_getPaymentInitiationScaStatus"),
    GET_PAYMENT_INITIATION_STATUS("_getPaymentInitiationStatus"),
    INITIATE_PAYMENT("_initiatePayment"),
    START_PAYMENT_AUTHORISATION("_startPaymentAuthorisation"),
    START_PAYMENT_INITIATION_CANCELLATION_AUTH("_startPaymentInitiationCancellationAuthorisation"),
    UPDATE_PAYMENT_CANCELLATION_PSU_DATA("_updatePaymentCancellationPsuData"),
    UPDATE_PAYMENT_PSU_DATA("_updatePaymentPsuData");

    private static final Map<String, Psd2PaymentMethodNameConstant> container = new HashMap<>();

    static {
        for (Psd2PaymentMethodNameConstant methodName : values()) {
            container.put(methodName.getValue(), methodName);
        }
    }

    private final String value;

    Psd2PaymentMethodNameConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonIgnore
    public static Optional<Psd2PaymentMethodNameConstant> getByValue(String name) {
        return Optional.ofNullable(container.get(name));
    }
}
