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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCancelPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
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

import java.util.Arrays;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.ACTC;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.CANC;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CancelPaymentServiceTest {
    private static final String PAYMENT_ID = "12345";
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String WRONG_PAYMENT_ID = "";
    private static final AspspConsentData SOME_ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "some consent id");
    private final List<String> ERROR_MESSAGE_TEXT = Arrays.asList("message 1", "message 2", "message 3");

    @InjectMocks
    private CancelPaymentService cancelPaymentService;
    @Mock
    private PaymentCancellationSpi paymentCancellationSpi;
    @Mock
    private SpiToXs2aCancelPaymentMapper spiToXs2aCancelPaymentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private SpiErrorMapper spiErrorMapper;

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
                            .message(ERROR_MESSAGE_TEXT)
                            .fail(SpiResponseStatus.LOGICAL_FAILURE));
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(WRONG_PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .message(ERROR_MESSAGE_TEXT)
                            .fail(SpiResponseStatus.LOGICAL_FAILURE));

        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, TransactionStatus.CANC))))
            .thenReturn(getCancelPaymentResponse(false, CANC));
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))))
            .thenReturn(getCancelPaymentResponse(true, ACTC));
    }

    @Test
    public void cancelPaymentWithoutAuthorisation_Success() {
        //When
        ResponseObject<CancelPaymentResponse> response =
            cancelPaymentService.cancelPaymentWithoutAuthorisation(getSpiPsuData(), getSpiPayment(PAYMENT_ID), ENCRYPTED_PAYMENT_ID);

        //Than
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(false, CANC));
    }

    @Test
    public void cancelPaymentWithoutAuthorisation_Failure_WrongId() {

        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");
        ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_403)
                                      .errorType(ErrorType.PIS_403)
                                      .messages(ERROR_MESSAGE_TEXT)
                                      .build();

        when(spiErrorMapper.mapToErrorHolder(any(SpiResponse.class), eq(ServiceType.PIS)))
            .thenReturn(errorHolder);

        //When
        ResponseObject<CancelPaymentResponse> response =
            cancelPaymentService.cancelPaymentWithoutAuthorisation(getSpiPsuData(), getSpiPayment(WRONG_PAYMENT_ID), WRONG_PAYMENT_ID);

        //Than
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.PIS_403);
        assertThat(response.getError().getTppMessage().getText()).isEqualTo(errorMessagesString);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.RESOURCE_UNKNOWN_403);
    }

    @Test
    public void cancelPaymentWithAuthorisation_Success() {
        //When
        ResponseObject<CancelPaymentResponse> response =
            cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(), getSpiPayment(PAYMENT_ID), ENCRYPTED_PAYMENT_ID);

        //Than
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(true, ACTC));
    }

    @Test
    public void cancelPaymentWithAuthorisation_Failure_WrongId() {
        // Given
        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");

        ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
                                      .errorType(PIS_400)
                                      .messages(ERROR_MESSAGE_TEXT)
                                      .build();

        when(spiErrorMapper.mapToErrorHolder(any(SpiResponse.class), eq(ServiceType.PIS)))
            .thenReturn(errorHolder);
        //When
        ResponseObject<CancelPaymentResponse> response =
            cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(), getSpiPayment(WRONG_PAYMENT_ID), WRONG_PAYMENT_ID);

        //Than
        assertThat(response.hasError()).isTrue();
        assertThat(response.getBody()).isNull();
        assertThat(response.getError().getErrorType()).isEqualTo(PIS_400);
        assertThat(response.getError().getTppMessage().getText()).isEqualTo(errorMessagesString);
        assertThat(response.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }

    private SpiPaymentCancellationResponse getSpiCancelPaymentResponse(boolean authorisationRequired, TransactionStatus transactionStatus) {
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

    private PsuIdData getSpiPsuData() {
        return new PsuIdData("", "", "", "");
    }

    private SpiPayment getSpiPayment(String paymentId) {
        SpiSinglePayment spiSinglePayment = new SpiSinglePayment("sepa-credit-transfers");
        spiSinglePayment.setPaymentId(paymentId);
        return spiSinglePayment;
    }
}
