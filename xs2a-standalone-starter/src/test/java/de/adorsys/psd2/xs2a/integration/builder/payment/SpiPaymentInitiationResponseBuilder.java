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

package de.adorsys.psd2.xs2a.integration.builder.payment;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;

public class SpiPaymentInitiationResponseBuilder {
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[16], "some consent id");
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";

    public static SpiResponse<SpiSinglePaymentInitiationResponse> buildSinglePaymentResponse() {
        SpiSinglePaymentInitiationResponse response = new SpiSinglePaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspAccountId(ASPSP_ACCOUNT_ID);
        response.setSpiTransactionFeeIndicator(false);
        return buildSpiResponse(response);
    }

    /**
     * Response field `spiTransactionFeeIndicator` not set.
     * Pay attention that XS2A will not return `spiTransactionFeeIndicator` at all.
     *
     * @return {@link SpiResponse}
     */
    public static SpiResponse<SpiPeriodicPaymentInitiationResponse> buildPeriodicPaymentResponse() {
        SpiPeriodicPaymentInitiationResponse response = new SpiPeriodicPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspAccountId(ASPSP_ACCOUNT_ID);
        return buildSpiResponse(response);
    }

    public static SpiResponse<SpiBulkPaymentInitiationResponse> buildBulkPaymentResponse() {
        SpiBulkPaymentInitiationResponse response = new SpiBulkPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspAccountId(ASPSP_ACCOUNT_ID);
        response.setSpiTransactionFeeIndicator(true);
        return buildSpiResponse(response);
    }

    private static <T> SpiResponse<T> buildSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }
}
