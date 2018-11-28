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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.consent.PisConsentDataService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCancelPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.ACTC;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.CANC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CancelPaymentServiceTest {
    private static final String PAYMENT_ID = "12345";
    private static final String WRONG_PAYMENT_ID = "";
    private static final AspspConsentData SOME_ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "some consent id");

    @InjectMocks
    private CancelPaymentService cancelPaymentService;
    @Mock
    private PaymentCancellationSpi paymentCancellationSpi;
    @Mock
    private SpiToXs2aCancelPaymentMapper spiToXs2aCancelPaymentMapper;
    @Mock
    private PisConsentDataService pisConsentDataService;

    @Before
    public void setUp() {
        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), eq(getSpiPayment(PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), eq(getSpiPayment(WRONG_PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .fail(SpiResponseStatus.LOGICAL_FAILURE));
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, SpiTransactionStatus.ACTC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(WRONG_PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .fail(SpiResponseStatus.LOGICAL_FAILURE));

        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, SpiTransactionStatus.CANC))))
            .thenReturn(getCancelPaymentResponse(false, CANC));
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(true, SpiTransactionStatus.ACTC))))
            .thenReturn(getCancelPaymentResponse(true, ACTC));
    }

    @Test
    public void cancelPaymentWithoutAuthorisation_Success() {
        //When
        ResponseObject<CancelPaymentResponse> response =
            cancelPaymentService.cancelPaymentWithoutAuthorisation(getSpiPsuData(), getSpiPayment(PAYMENT_ID));

        //Than
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(false, CANC));
    }

    @Test
    public void cancelPaymentWithoutAuthorisation_Failure_WrongId() {
        //When
        ResponseObject<CancelPaymentResponse> response =
            cancelPaymentService.cancelPaymentWithoutAuthorisation(getSpiPsuData(), getSpiPayment(WRONG_PAYMENT_ID));

        //Than
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError()).isEqualTo(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_403));
    }

    @Test
    public void cancelPaymentWithAuthorisation_Success() {
        //When
        ResponseObject<CancelPaymentResponse> response =
            cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(), getSpiPayment(PAYMENT_ID));

        //Than
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(true, ACTC));
    }

    @Test
    public void cancelPaymentWithAuthorisation_Failure_WrongId() {
        //When
        ResponseObject<CancelPaymentResponse> response =
            cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(), getSpiPayment(WRONG_PAYMENT_ID));

        //Than
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError()).isEqualTo(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_403));
    }

    private SpiPaymentCancellationResponse getSpiCancelPaymentResponse(boolean authorisationRequired, SpiTransactionStatus transactionStatus) {
        SpiPaymentCancellationResponse response = new SpiPaymentCancellationResponse();
        response.setCancellationAuthorisationMandated(authorisationRequired);
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    private CancelPaymentResponse getCancelPaymentResponse(boolean authorisationRequired, TransactionStatus transactionStatus) {
        CancelPaymentResponse response = new CancelPaymentResponse();
        response.setStartAuthorisationRequired(authorisationRequired);
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    private SpiPsuData getSpiPsuData() {
        return new SpiPsuData("", "", "", "");
    }

    private SpiPayment getSpiPayment(String paymentId) {
        SpiSinglePayment spiSinglePayment = new SpiSinglePayment(PaymentProduct.SEPA);
        spiSinglePayment.setPaymentId(paymentId);
        return spiSinglePayment;
    }
}
