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

package de.adorsys.psd2.xs2a.integration.builder.payment;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;

public class SpiPaymentInitiationResponseBuilder {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";

    public static SpiResponse<SpiSinglePaymentInitiationResponse> buildSinglePaymentResponse(boolean multiLevelSca) {
        SpiSinglePaymentInitiationResponse response = new SpiSinglePaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspAccountId(ASPSP_ACCOUNT_ID);
        response.setSpiTransactionFeeIndicator(false);
        response.setMultilevelScaRequired(multiLevelSca);
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
                   .build();
    }
}
