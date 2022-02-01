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

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.integration.builder.PsuIdDataBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiCommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.xs2a.reader.JsonReader;

import java.util.Collections;
import java.util.List;

public class PisCommonPaymentResponseBuilder {
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final JsonReader JSON_READER = new JsonReader();

    public static PisCommonPaymentResponse buildPisCommonPaymentResponseWithAuthorisation(Authorisation authorisation) {
        PisCommonPaymentResponse response = buildPisCommonPaymentResponse();
        response.setAuthorisations(Collections.singletonList(authorisation));
        return response;
    }

    public static PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        commonPaymentResponse.setTransactionStatus(TRANSACTION_STATUS);
        commonPaymentResponse.setInternalPaymentStatus(InternalPaymentStatus.CANCELLED_INITIATED);
        commonPaymentResponse.setPaymentType(PAYMENT_TYPE);
        commonPaymentResponse.setPaymentProduct(PAYMENT_PRODUCT);
        commonPaymentResponse.setTppInfo(TppInfoBuilder.buildTppInfo());
        commonPaymentResponse.setPsuData(Collections.singletonList(PsuIdDataBuilder.buildPsuIdData()));
        return commonPaymentResponse;
    }

    public static PisCommonPaymentResponse buildPisCommonPaymentResponse(List<Authorisation> authorisationList) {
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();
        commonPaymentResponse.setAuthorisations(authorisationList);
        return commonPaymentResponse;
    }

    public static PisCommonPaymentResponse buildPisCommonPaymentResponseWithPayment() {
        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse();
        pisCommonPaymentResponse.setPaymentData(JSON_READER.getBytesFromFile("json/payment/single-payment-part.json"));
        return pisCommonPaymentResponse;
    }

    public static SpiResponse<SpiPaymentInfo> buildGetPaymentResponse(SpiPaymentInfo spiPaymentInfo) {
        return buildSpiResponse(spiPaymentInfo);
    }

    public static SpiResponse<SpiGetPaymentStatusResponse> buildGetPaymentStatusResponse(SpiGetPaymentStatusResponse spiGetPaymentStatusResponse) {
        return buildSpiResponse(spiGetPaymentStatusResponse);
    }

    public static SpiResponse<SpiPaymentInitiationResponse> buildSpiPaymentInitiationResponse() {
        SpiPaymentInitiationResponse response = new SpiCommonPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspAccountId(ASPSP_ACCOUNT_ID);
        response.setSpiTransactionFeeIndicator(false);
        response.setMultilevelScaRequired(false);
        return buildSpiResponse(response);
    }

    private static <T> SpiResponse<T> buildSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .build();
    }
}
