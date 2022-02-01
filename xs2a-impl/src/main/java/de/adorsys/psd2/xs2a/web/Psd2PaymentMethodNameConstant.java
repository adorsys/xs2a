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

package de.adorsys.psd2.xs2a.web;

import java.util.HashMap;
import java.util.Map;

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
}
