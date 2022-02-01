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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCancelPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelPaymentServiceTest {
    private static final String PAYMENT_ID = "12345";
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String AUTHORISATION = "Bearer 1111111";
    private static final String AUTHORISATION_ID = "auth id";
    private static final PsuIdData PSU_DATA = buildPsuIdData();
    private static final SpiPsuData SPI_PSU_DATA = SpiPsuData.builder().psuId(PSU_DATA.getPsuId()).psuIdType(PSU_DATA.getPsuIdType()).psuCorporateId(PSU_DATA.getPsuCorporateId()).psuCorporateIdType(PSU_DATA.getPsuCorporateIdType()).build();
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.buildWithPsuTppAuthToken(SPI_PSU_DATA, new TppInfo(), AUTHORISATION);
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private CancelPaymentService cancelPaymentService;
    @Mock
    private PaymentCancellationSpi paymentCancellationSpi;
    @Mock
    private SpiToXs2aCancelPaymentMapper spiToXs2aCancelPaymentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
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
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;

    @Test
    void initiatePaymentCancellation_authorisationNotRequired_shouldCancelPaymentWithoutSca() {
        // Given
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));

        SpiPayment spiPayment = getSpiPayment(ACTC);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC))
                            .build());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC), getSpiPayment(ACTC), ENCRYPTED_PAYMENT_ID))
            .thenReturn(getCancelPaymentResponse(false, ACTC));
        when(paymentCancellationAuthorisationNeededDecider.isNoScaRequired(false))
            .thenReturn(true);

        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), any(), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .build());

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
    void initiatePaymentCancellation_authorisationNotRequired_hasError() {
        // Given
        SpiPayment spiPayment = getSpiPayment(ACTC);

        SpiResponse<SpiResponse.VoidResponse> spiErrorResponse = SpiResponse.<SpiResponse.VoidResponse>builder()
                                                                     .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR))
                                                                     .build();

        ErrorHolder errorHolder = ErrorHolder.builder(PIS_404)
                                      .tppMessages(TppMessageInformation.of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                      .build();

        MessageError expectedError = new MessageError(errorHolder);
        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));

        when(paymentCancellationSpi.cancelPaymentWithoutSca(SPI_CONTEXT_DATA, spiPayment, spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)))
            .thenReturn(spiErrorResponse);

        when(spiErrorMapper.mapToErrorHolder(spiErrorResponse, ServiceType.PIS)).thenReturn(errorHolder);

        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC))
                            .build());

        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(getSpiCancelPaymentResponse(false, TransactionStatus.ACTC), getSpiPayment(ACTC), ENCRYPTED_PAYMENT_ID))
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
    void initiatePaymentCancellation_paymentAlreadyCancelledInSpi_shouldReturnCancelledInResponse() {
        // Given
        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));
        SpiPayment spiPayment = getSpiPayment(CANC);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, CANC))
                            .build());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(getSpiCancelPaymentResponse(false, TransactionStatus.CANC), getSpiPayment(CANC), ENCRYPTED_PAYMENT_ID))
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
    void initiatePaymentCancellation_withPaymentInReceivedStatus_shouldCancelPaymentWithoutSca() {
        // Given
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));

        SpiPayment spiPayment = getSpiPayment(RCVD);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, RCVD))
                            .build());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(getSpiCancelPaymentResponse(false, RCVD), getSpiPayment(RCVD), ENCRYPTED_PAYMENT_ID))
            .thenReturn(getCancelPaymentResponse(false, RCVD));


        when(paymentCancellationSpi.cancelPaymentWithoutSca(any(), any(), any()))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .build());

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
    void initiatePaymentCancellation_authorisationRequired_shouldReturnResponseFromSpi() {
        // Given
        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));

        SpiPayment spiPayment = getSpiPayment(ACTC);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))
                            .build());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC), getSpiPayment(ACTC), ENCRYPTED_PAYMENT_ID))
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
    void initiatePaymentCancellation_withFinalisedPayment_shouldReturnError() {
        // Given
        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));

        SpiPayment spiPayment = getSpiPayment(ACSC);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(false, ACSC))
                            .build());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(getSpiCancelPaymentResponse(false, ACSC), getSpiPayment(ACSC), ENCRYPTED_PAYMENT_ID))
            .thenReturn(getCancelPaymentResponse(false, ACSC));

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);

        // Then
        assertThat(response.hasError()).isTrue();
        MessageError error = response.getError();
        assertThat(error.getErrorType()).isEqualTo(PIS_400);
    }

    @Test
    void initiatePaymentCancellation_spiErrorDuringInitiation_shouldReturnError() {
        // Given
        SpiPayment spiPayment = getSpiPayment(null);
        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                            .build());

        when(spiErrorMapper.mapToErrorHolder(any(), eq(ServiceType.PIS)))
            .thenReturn(ErrorHolder.builder(PIS_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_BLOCKED))
                            .build());

        // When
        ResponseObject<CancelPaymentResponse> response = cancelPaymentService.initiatePaymentCancellation(spiPayment,
                                                                                                          ENCRYPTED_PAYMENT_ID,
                                                                                                          false,
                                                                                                          null);
        // Then
        assertThat(response.hasError()).isTrue();
    }

    @Test
    void initiatePaymentCancellation_noTransactionStatusFromSpi_shouldGetStatusFromPayment() {
        // Given
        SpiPayment spiPayment = getSpiPayment(ACWC);

        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));

        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, null))
                            .build());
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
    void initiatePaymentCancellation_authorisationRequired_implicit_shouldReturnResponseFromSpi() {
        SpiPayment spiPayment = getSpiPayment(ACTC);
        Xs2aCreatePisCancellationAuthorisationResponse cancellationResponse = new Xs2aCreatePisCancellationAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, spiPayment.getPaymentType(), INTERNAL_REQUEST_ID);
        CancelPaymentResponse cancelPaymentResponseExpected = getCancelPaymentResponse(true, ACTC);
        cancelPaymentResponseExpected.setAuthorizationId(AUTHORISATION_ID);
        cancelPaymentResponseExpected.setScaStatus(ScaStatus.RECEIVED);

        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));

        when(paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(ENCRYPTED_PAYMENT_ID, EMPTY_PSU_DATA, spiPayment.getPaymentProduct(), spiPayment.getPaymentType(), null)))
            .thenReturn(ResponseObject.<CancellationAuthorisationResponse>builder()
                            .body(cancellationResponse)
                            .build());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC), getSpiPayment(ACTC), ENCRYPTED_PAYMENT_ID))
            .thenReturn(getCancelPaymentResponse(true, ACTC));

        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))
                            .build());

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
    void initiatePaymentCancellation_authorisationRequired_implicit_hasError() {
        SpiPayment spiPayment = getSpiPayment(ACTC);

        when(spiContextDataProvider.provide()).thenReturn(SPI_CONTEXT_DATA);
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.fromString(INTERNAL_REQUEST_ID));

        when(paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(ENCRYPTED_PAYMENT_ID, EMPTY_PSU_DATA, spiPayment.getPaymentProduct(), spiPayment.getPaymentType(), null)))
            .thenReturn(ResponseObject.<CancellationAuthorisationResponse>builder()
                            .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                            .build());

        when(paymentCancellationSpi.initiatePaymentCancellation(any(), eq(spiPayment), any()))
            .thenReturn(SpiResponse.<SpiPaymentCancellationResponse>builder()
                            .payload(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC))
                            .build());
        when(spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(getSpiCancelPaymentResponse(true, TransactionStatus.ACTC), getSpiPayment(ACTC), ENCRYPTED_PAYMENT_ID))
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
        response.setInternalRequestId(INTERNAL_REQUEST_ID);
        return response;
    }

    private SpiPayment getSpiPayment(TransactionStatus transactionStatus) {
        SpiSinglePayment spiSinglePayment = new SpiSinglePayment("sepa-credit-transfers");
        spiSinglePayment.setPaymentId(PAYMENT_ID);
        spiSinglePayment.setPaymentStatus(transactionStatus);
        return spiSinglePayment;
    }

    private static PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    }
}
