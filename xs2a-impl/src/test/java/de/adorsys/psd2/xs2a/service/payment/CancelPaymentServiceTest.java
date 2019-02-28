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
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
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

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.*;
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
    @Mock
    private Xs2aUpdatePaymentStatusAfterSpiService xs2aUpdatePaymentStatusAfterSpiService;
    @Mock
    private PaymentCancellationAuthorisationNeededDecider paymentCancellationAuthorisationNeededDecider;

    @Before
    public void setUp() {
        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), eq(getSpiPayment(WRONG_PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .message(ERROR_MESSAGE_TEXT)
                            .fail(SpiResponseStatus.LOGICAL_FAILURE));
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(WRONG_PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .message(ERROR_MESSAGE_TEXT)
                            .fail(SpiResponseStatus.LOGICAL_FAILURE));

        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, TransactionStatus.CANC))))
            .thenReturn(getCancelPaymentResponse(false, CANC));
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(true, TransactionStatus.CANC))))
            .thenReturn(getCancelPaymentResponse(true, CANC));
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC))))
            .thenReturn(getCancelPaymentResponse(false, ACTC));
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))))
            .thenReturn(getCancelPaymentResponse(true, ACTC));
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, RCVD))))
            .thenReturn(getCancelPaymentResponse(false, RCVD));
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, ACSC))))
            .thenReturn(getCancelPaymentResponse(false, ACSC));
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(true, null))))
            .thenReturn(getCancelPaymentResponse(true, null));

    }

    @Test
    public void initiatePaymentCancellation_authorisationNotRequired_shouldCancelPaymentWithoutSca() {
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(false))
            .thenReturn(true);

        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), any(), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(),
                                                                                                          getSpiPayment(PAYMENT_ID),
                                                                                                          ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(false, CANC));
    }

    @Test
    public void initiatePaymentCancellation_paymentAlreadyCancelledInSpi_shouldReturnCancelledInResponse() {
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, CANC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(),
                                                                                                          getSpiPayment(PAYMENT_ID),
                                                                                                          ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(false, CANC));
    }

    @Test
    public void initiatePaymentCancellation_withPaymentInReceivedStatus_shouldCancelPaymentWithoutSca() {
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, RCVD))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());


        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), any(), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(),
                                                                                                          getSpiPayment(PAYMENT_ID),
                                                                                                          ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(false, CANC));
    }

    @Test
    public void initiatePaymentCancellation_authorisationRequired_shouldReturnResponseFromSpi() {
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(true))
            .thenReturn(false);

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(),
                                                                                                          getSpiPayment(PAYMENT_ID),
                                                                                                          ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(true, ACTC));
    }

    @Test
    public void initiatePaymentCancellation_withFinalisedPayment_shouldReturnError() {
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, ACSC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());


        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), any(), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(),
                                                                                                          getSpiPayment(PAYMENT_ID),
                                                                                                          ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(response.hasError()).isTrue();
        MessageError error = response.getError();
        assertThat(error.getErrorType()).isEqualTo(PIS_400);
    }

    @Test
    public void initiatePaymentCancellation_spiErrorDuringInitiation_shouldReturnError() {
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(PAYMENT_ID)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .fail(SpiResponseStatus.LOGICAL_FAILURE));

        when(spiErrorMapper.mapToErrorHolder(any(), eq(ServiceType.PIS)))
            .thenReturn(ErrorHolder.builder(MessageErrorCode.RESOURCE_BLOCKED).build());

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(),
                                                                                                          getSpiPayment(PAYMENT_ID),
                                                                                                          ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(response.hasError()).isTrue();
    }

    @Test
    public void initiatePaymentCancellation_noTransactionStatusFromSpi_shouldGetStatusFromPayment() {
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(getSpiPayment(PAYMENT_ID, ACWC)), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, null))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(true))
            .thenReturn(false);

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(getSpiPsuData(),
                                                                                                          getSpiPayment(PAYMENT_ID, ACWC),
                                                                                                          ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(true, ACWC));
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
        return getSpiPayment(paymentId, null);
    }

    private SpiPayment getSpiPayment(String paymentId, TransactionStatus transactionStatus) {
        SpiSinglePayment spiSinglePayment = new SpiSinglePayment("sepa-credit-transfers");
        spiSinglePayment.setPaymentId(paymentId);
        spiSinglePayment.setPaymentStatus(transactionStatus);
        return spiSinglePayment;
    }
}
