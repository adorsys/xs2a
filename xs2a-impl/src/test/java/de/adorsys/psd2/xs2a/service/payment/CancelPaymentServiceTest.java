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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCancelPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
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
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.*;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_404;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_404;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_CANC_405;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CancelPaymentServiceTest {
    private static final String PAYMENT_ID = "12345";
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String AUTHORISATION_ID = "auth id";
    private static final String PAYMENT_NOT_FOUND_MESSAGE = "Payment not found";
    private static final AspspConsentData SOME_ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "some consent id");
    private static final PsuIdData PSU_DATA = buildPsuIdData();
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(PSU_DATA.getPsuId(), PSU_DATA.getPsuIdType(), PSU_DATA.getPsuCorporateId(), PSU_DATA.getPsuCorporateIdType());
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo(), UUID.randomUUID());

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
    private Xs2aUpdatePaymentAfterSpiService xs2AUpdatePaymentAfterSpiService;
    @Mock
    private PaymentCancellationAuthorisationNeededDecider paymentCancellationAuthorisationNeededDecider;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private PaymentCancellationAuthorisationService paymentCancellationAuthorisationService;
    @Mock
    private RequestProviderService requestProviderService;

    @Before
    public void setUp() {
        when(pisAspspDataService.getAspspConsentData(ENCRYPTED_PAYMENT_ID)).thenReturn(ASPSP_CONSENT_DATA);
        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
    }

    @Test
    public void initiatePaymentCancellation_authorisationNotRequired_shouldCancelPaymentWithoutSca() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, ACTC);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC)), eq(getSpiPayment(PAYMENT_ID, ACTC)), eq(ENCRYPTED_PAYMENT_ID)))
            .thenReturn(getCancelPaymentResponse(false, ACTC));

        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(false))
            .thenReturn(true);

        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), any(), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);

        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(false, CANC));
    }

    @Test
    public void initiatePaymentCancellation_authorisationNotRequired_hasError() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, ACTC);

        SpiResponse<SpiResponse.VoidResponse> spiErrorResponse = SpiResponse.<SpiResponse.VoidResponse>builder()
                                                                     .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                     .fail(SpiResponseStatus.TECHNICAL_FAILURE);

        ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
                                      .messages(Collections.singletonList(PAYMENT_NOT_FOUND_MESSAGE))
                                      .build();

        MessageError expectedError = new MessageError(errorHolder);

        when(paymentCancellationSpi.cancelPaymentWithoutSca(SPI_CONTEXT_DATA, spiPayment, ASPSP_CONSENT_DATA))
            .thenReturn(spiErrorResponse);

        when(spiErrorMapper.mapToErrorHolder(spiErrorResponse, ServiceType.PIS)).thenReturn(errorHolder);

        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC)), eq(getSpiPayment(PAYMENT_ID, ACTC)), eq(ENCRYPTED_PAYMENT_ID)))
            .thenReturn(getCancelPaymentResponse(false, ACTC));

        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(false))
            .thenReturn(true);

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);

        // Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError()).isEqualTo(expectedError);
    }

    @Test
    public void initiatePaymentCancellation_paymentAlreadyCancelledInSpi_shouldReturnCancelledInResponse() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, CANC);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, CANC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, TransactionStatus.CANC)), eq(getSpiPayment(PAYMENT_ID, CANC)), eq(ENCRYPTED_PAYMENT_ID)))
            .thenReturn(getCancelPaymentResponse(false, CANC));

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);

        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(false, CANC));
    }

    @Test
    public void initiatePaymentCancellation_withPaymentInReceivedStatus_shouldCancelPaymentWithoutSca() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, RCVD);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, RCVD))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, RCVD)), eq(getSpiPayment(PAYMENT_ID, RCVD)), eq(ENCRYPTED_PAYMENT_ID)))
            .thenReturn(getCancelPaymentResponse(false, RCVD));

        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), any(), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);

        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(false, CANC));
    }

    @Test
    public void initiatePaymentCancellation_authorisationRequired_shouldReturnResponseFromSpi() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, ACTC);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC)), eq(getSpiPayment(PAYMENT_ID, ACTC)), eq(ENCRYPTED_PAYMENT_ID)))
            .thenReturn(getCancelPaymentResponse(true, ACTC));

        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(true))
            .thenReturn(false);

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);

        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(true, ACTC));
    }

    @Test
    public void initiatePaymentCancellation_withFinalisedPayment_shouldReturnError() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, ACSC);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, ACSC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(false, ACSC)), eq(getSpiPayment(PAYMENT_ID, ACSC)), eq(ENCRYPTED_PAYMENT_ID)))
            .thenReturn(getCancelPaymentResponse(false, ACSC));

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);

        // Then
        assertThat(response.hasError()).isTrue();
        MessageError error = response.getError();
        assertThat(error.getErrorType()).isEqualTo(PIS_CANC_405);
    }

    @Test
    public void initiatePaymentCancellation_spiErrorDuringInitiation_shouldReturnError() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, null);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .fail(SpiResponseStatus.LOGICAL_FAILURE));

        when(spiErrorMapper.mapToErrorHolder(any(), eq(ServiceType.PIS)))
            .thenReturn(ErrorHolder.builder(MessageErrorCode.RESOURCE_BLOCKED).build());

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);
        // Then
        assertThat(response.hasError()).isTrue();
    }

    @Test
    public void initiatePaymentCancellation_noTransactionStatusFromSpi_shouldGetStatusFromPayment() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, ACWC);

        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, null))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(true, null)), any(SpiPayment.class), eq(ENCRYPTED_PAYMENT_ID)))
            .thenReturn(getCancelPaymentResponse(true, null));

        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(true))
            .thenReturn(false);

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);
        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(getCancelPaymentResponse(true, ACWC));
    }

    @Test
    public void initiatePaymentCancellation_authorisationRequired_implicit_shouldReturnResponseFromSpi() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, ACTC);
        Xs2aCreatePisCancellationAuthorisationResponse cancellationResponse = new Xs2aCreatePisCancellationAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, spiPayment.getPaymentType());
        CancelPaymentResponse cancelPaymentResponseExpected = getCancelPaymentResponse(true, ACTC);
        cancelPaymentResponseExpected.setAuthorizationId(AUTHORISATION_ID);
        cancelPaymentResponseExpected.setScaStatus(ScaStatus.RECEIVED);

        when(paymentCancellationAuthorisationService.createPisCancellationAuthorization(ENCRYPTED_PAYMENT_ID, null, spiPayment.getPaymentType(), spiPayment.getPaymentProduct()))
            .thenReturn(ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                            .body(cancellationResponse)
                            .build());

        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC)), eq(getSpiPayment(PAYMENT_ID, ACTC)), eq(ENCRYPTED_PAYMENT_ID)))
            .thenReturn(getCancelPaymentResponse(true, ACTC));

        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(true))
            .thenReturn(false);
        when(authorisationMethodDecider.isImplicitMethod(true, false))
            .thenReturn(true);

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          true,
                                                                                                          null);
        // Then
        assertThat(response.hasError()).isFalse();
        assertThat(response.getBody()).isEqualTo(cancelPaymentResponseExpected);
    }

    @Test
    public void initiatePaymentCancellation_authorisationRequired_implicit_hasError() {
        SpiPayment spiPayment = getSpiPayment(PAYMENT_ID, ACTC);

        when(paymentCancellationAuthorisationService.createPisCancellationAuthorization(ENCRYPTED_PAYMENT_ID, null, spiPayment.getPaymentType(), spiPayment.getPaymentProduct()))
            .thenReturn(ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                            .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                            .build());

        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))
                            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
                            .success());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(eq(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC)), eq(getSpiPayment(PAYMENT_ID, ACTC)), eq(ENCRYPTED_PAYMENT_ID)))
            .thenReturn(getCancelPaymentResponse(true, ACTC));

        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(true))
            .thenReturn(false);
        when(authorisationMethodDecider.isImplicitMethod(true, false))
            .thenReturn(true);

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          true,
                                                                                                          null);
        // Then
        assertThat(response.hasError()).isTrue();
        assertThat(response.getError().getErrorType()).isEqualTo(PIS_CANC_405);
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

        if (authorisationRequired) {
            response.setPaymentId(ENCRYPTED_PAYMENT_ID);
        }
        return response;
    }

    private SpiPayment getSpiPayment(String paymentId, TransactionStatus transactionStatus) {
        SpiSinglePayment spiSinglePayment = new SpiSinglePayment("sepa-credit-transfers");
        spiSinglePayment.setPaymentId(paymentId);
        spiSinglePayment.setPaymentStatus(transactionStatus);
        return spiSinglePayment;
    }

    private static PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    }
}
