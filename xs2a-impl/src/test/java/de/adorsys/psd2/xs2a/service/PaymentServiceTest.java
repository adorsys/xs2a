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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.LoggingContextService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.payment.PaymentServiceResolver;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelCertainPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreatePaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.status.AbstractReadPaymentStatusService;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.*;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.dto.CreatePaymentRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_404;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {
    private static final String PAYMENT_ID = "12345";
    private static final String WRONG_PAYMENT_ID = "777";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(null, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(null, null, null, null, null);
    private static final MessageError VALIDATION_ERROR = new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo(), UUID.randomUUID(), UUID.randomUUID());

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private TppService tppService;
    @Mock
    private PaymentServiceResolver paymentServiceResolver;
    @Mock
    private CreatePaymentService createPaymentService;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private AbstractReadPaymentStatusService readPaymentStatusService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private PisCommonPaymentResponse pisCommonPaymentResponse;
    @Mock
    private PisCommonPaymentResponse invalidPisCommonPaymentResponse;
    @Mock
    private Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;
    @Mock
    private CreatePaymentValidator createPaymentValidator;
    @Mock
    private GetPaymentByIdValidator getPaymentByIdValidator;
    @Mock
    private GetPaymentStatusByIdValidator getPaymentStatusByIdValidator;
    @Mock
    private CancelPaymentValidator cancelPaymentValidator;
    @Mock
    private InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private ReadPaymentService readPaymentService;
    @Mock
    private CancelCertainPaymentService cancelCertainPaymentService;
    @Mock
    private LoggingContextService loggingContextService;

    private JsonReader jsonReader;

    private SinglePayment singlePayment;
    private BulkPayment bulkPayment;
    private PeriodicPayment periodicPayment;

    @Before
    public void setUp() {
        jsonReader = new JsonReader();

        singlePayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-single-payment.json", SinglePayment.class);
        bulkPayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-bulk-payment.json", BulkPayment.class);
        periodicPayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-periodic-payment.json", PeriodicPayment.class);

        when(tppService.getTppInfo()).thenReturn(getTppInfo());
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(getPisCommonPayment());
        when(createPaymentValidator.validate(any(CreatePaymentRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getPaymentByIdValidator.validate(any(GetPaymentByIdPO.class)))
            .thenReturn(ValidationResult.valid());
        when(getPaymentStatusByIdValidator.validate(any(GetPaymentStatusByIdPO.class)))
            .thenReturn(ValidationResult.valid());
        when(cancelPaymentValidator.validate(any(CancelPaymentPO.class)))
            .thenReturn(ValidationResult.valid());
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
    }

    @Test
    public void createRawPayment_Success() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PaymentType.SINGLE);
        when(paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters)).thenReturn(createPaymentService);
        when(createPaymentService.createPayment(any(), any(), any()))
            .thenReturn(ResponseObject.<PaymentInitiationResponse>builder()
                            .body(buildSinglePaymentInitiationResponse())
                            .build());
        // When
        ResponseObject<PaymentInitiationResponse> actualResponse = paymentService.createPayment("".getBytes(), buildPaymentInitiationParameters(PaymentType.SINGLE));

        // Then
        assertThatPaymentWasCreated(actualResponse);
    }

    @Test
    public void createSinglePayment_Success() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PaymentType.SINGLE);
        when(paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters)).thenReturn(createPaymentService);
        when(createPaymentService.createPayment(any(), any(), any()))
            .thenReturn(ResponseObject.<PaymentInitiationResponse>builder()
                            .body(buildSinglePaymentInitiationResponse())
                            .build());
        // When
        ResponseObject<PaymentInitiationResponse> actualResponse = paymentService.createPayment(singlePayment, paymentInitiationParameters);

        // Then
        assertThatPaymentWasCreated(actualResponse);
    }

    @Test
    public void createPeriodicPayment_Success() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PaymentType.PERIODIC);
        when(paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters)).thenReturn(createPaymentService);
        when(createPaymentService.createPayment(any(), any(), any()))
            .thenReturn(ResponseObject.<PaymentInitiationResponse>builder()
                            .body(buildPeriodicPaymentInitiationResponse())
                            .build());
        // When
        ResponseObject<PaymentInitiationResponse> actualResponse = paymentService.createPayment(periodicPayment, paymentInitiationParameters);

        // Then
        assertThatPaymentWasCreated(actualResponse);
    }

    @Test
    public void createSinglePayment_Failure_ShouldReturnError() {
        // Given
        when(createPaymentValidator.validate(any(CreatePaymentRequestObject.class)))
            .thenReturn(ValidationResult.invalid(new MessageError()));

        // When
        ResponseObject<PaymentInitiationResponse> actualResponse = paymentService.createPayment(singlePayment, buildPaymentInitiationParameters(PaymentType.SINGLE));

        // Then
        assertThatErrorIs(actualResponse, new MessageError());
    }

    @Test
    public void createSinglePayment_withInvalidInitiationParameters_shouldReturnValidationError() {
        // Given
        PaymentInitiationParameters invalidPaymentInitiationParameters = buildInvalidPaymentInitiationParameters();

        CreatePaymentRequestObject createPaymentRequestObject = new CreatePaymentRequestObject(singlePayment, invalidPaymentInitiationParameters);
        when(createPaymentValidator.validate(createPaymentRequestObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<PaymentInitiationResponse> actualResponse = paymentService.createPayment(singlePayment, invalidPaymentInitiationParameters);

        // Then
        verify(createPaymentValidator).validate(createPaymentRequestObject);
        assertThatErrorIs(actualResponse, VALIDATION_ERROR);
    }

    @Test
    public void createPeriodicPayment_Failure_ShouldReturnError() {
        // Given
        when(createPaymentValidator.validate(any(CreatePaymentRequestObject.class)))
            .thenReturn(ValidationResult.invalid(new MessageError()));

        // When
        ResponseObject<PaymentInitiationResponse> actualResponse = paymentService.createPayment(periodicPayment, buildPaymentInitiationParameters(PaymentType.PERIODIC));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
    }

    @Test
    public void createPeriodicPayment_withInvalidInitiationParameters_shouldReturnValidationError() {
        // Given
        PaymentInitiationParameters invalidPaymentInitiationParameters = buildInvalidPaymentInitiationParameters();

        CreatePaymentRequestObject createPaymentRequestObject = new CreatePaymentRequestObject(periodicPayment, invalidPaymentInitiationParameters);
        when(createPaymentValidator.validate(createPaymentRequestObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<PaymentInitiationResponse> actualResponse = paymentService.createPayment(periodicPayment, invalidPaymentInitiationParameters);

        // Then
        verify(createPaymentValidator).validate(createPaymentRequestObject);
        assertThatErrorIs(actualResponse, VALIDATION_ERROR);
    }

    @Test
    public void createBulkPayments() {
        // When
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PaymentType.BULK);
        when(paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters)).thenReturn(createPaymentService);
        when(createPaymentService.createPayment(bulkPayment, paymentInitiationParameters, getTppInfoServiceModified()))
            .thenReturn(getValidResponse());
        ResponseObject<PaymentInitiationResponse> actualResponse = paymentService.createPayment(bulkPayment, paymentInitiationParameters);

        // Then
        assertThatPaymentWasCreated(actualResponse);
    }

    @Test
    public void createBulkPayments_withInvalidInitiationParameters_shouldReturnValidationError() {
        // Given
        PaymentInitiationParameters invalidPaymentInitiationParameters = buildInvalidPaymentInitiationParameters();

        CreatePaymentRequestObject createPaymentRequestObject = new CreatePaymentRequestObject(bulkPayment, invalidPaymentInitiationParameters);
        when(createPaymentValidator.validate(createPaymentRequestObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<PaymentInitiationResponse> actualResponse = paymentService.createPayment(bulkPayment, invalidPaymentInitiationParameters);

        // Then
        verify(createPaymentValidator).validate(createPaymentRequestObject);
        assertThatErrorIs(actualResponse, VALIDATION_ERROR);
    }

    @Test
    public void createPayment_Success_ShouldRecordEvent() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PaymentType.SINGLE);
        when(paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters)).thenReturn(createPaymentService);
        when(createPaymentService.createPayment(any(), any(), any()))
            .thenReturn(ResponseObject.<PaymentInitiationResponse>builder()
                            .body(buildSinglePaymentInitiationResponse())
                            .build());
        PaymentInitiationParameters parameters = buildPaymentInitiationParameters(PaymentType.SINGLE);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        // When
        paymentService.createPayment(singlePayment, parameters);

        // Then
        verify(xs2aEventService, times(1)).recordTppRequest(argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.PAYMENT_INITIATION_REQUEST_RECEIVED);
    }

    @Test
    public void createPayment_shouldStoreStatusesInLoggingContext() {
        // Given
        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PaymentType.SINGLE);
        when(paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters)).thenReturn(createPaymentService);

        SinglePaymentInitiationResponse paymentInitiationResponse = buildSinglePaymentInitiationResponse();
        ScaStatus scaStatus = ScaStatus.PSUIDENTIFIED;
        paymentInitiationResponse.setScaStatus(scaStatus);
        when(createPaymentService.createPayment(any(), any(), any()))
            .thenReturn(ResponseObject.<PaymentInitiationResponse>builder()
                            .body(paymentInitiationResponse)
                            .build());

        PaymentInitiationParameters parameters = buildPaymentInitiationParameters(PaymentType.SINGLE);

        // When
        ResponseObject<PaymentInitiationResponse> response = paymentService.createPayment(singlePayment, parameters);

        // Then
        assertFalse(response.hasError());
        verify(loggingContextService).storeTransactionAndScaStatus(RCVD, scaStatus);
    }

    @Test
    public void getPaymentById_Success_ShouldRecordEvent() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString()))
            .thenReturn(Optional.of(pisCommonPaymentResponse));
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(paymentServiceResolver.getReadPaymentService(any())).thenReturn(readPaymentService);
        when(readPaymentService.getPayment(pisCommonPaymentResponse, null, PAYMENT_ID)).thenReturn(new PaymentInformationResponse<>(singlePayment));

        // When
        paymentService.getPaymentById(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_REQUEST_RECEIVED);
    }

    @Test
    public void getPaymentById_shouldStoreTransactionStatusInLoggingContext() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString()))
            .thenReturn(Optional.of(pisCommonPaymentResponse));
        when(paymentServiceResolver.getReadPaymentService(any())).thenReturn(readPaymentService);
        when(readPaymentService.getPayment(pisCommonPaymentResponse, null, PAYMENT_ID)).thenReturn(new PaymentInformationResponse<>(singlePayment));

        TransactionStatus expectedStatus = singlePayment.getTransactionStatus();

        // When
        ResponseObject response = paymentService.getPaymentById(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID);

        // Then
        assertFalse(response.hasError());
        verify(loggingContextService).storeTransactionStatus(expectedStatus);
    }

    @Test
    public void getPaymentById_withInvalidPaymentResponse_shouldReturnValidationError() {
        // Given
        when(getPaymentByIdValidator.validate(any(GetPaymentByIdPO.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(invalidPisCommonPaymentResponse));
        PaymentType paymentType = PaymentType.SINGLE;

        // When
        ResponseObject actualResponse = paymentService.getPaymentById(paymentType, PAYMENT_PRODUCT, PAYMENT_ID);

        // Then
        verify(getPaymentByIdValidator).validate(new GetPaymentByIdPO(invalidPisCommonPaymentResponse, paymentType, PAYMENT_PRODUCT));
        assertThatErrorIs(actualResponse, VALIDATION_ERROR);
    }

    @Test
    public void getPaymentById_Failure_WrongId() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject actualResult = paymentService.getPaymentById(PaymentType.SINGLE, PAYMENT_PRODUCT, WRONG_PAYMENT_ID);

        //Then
        assertThatPaymentHasWrongId(actualResult);
    }

    @Test
    public void getPaymentStatusById_Success_ShouldRecordEvent() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(pisCommonPaymentResponse));
        when(readPaymentStatusService.readPaymentStatus(any(), any(SpiContextData.class), any(String.class)))
            .thenReturn(new ReadPaymentStatusResponse(RCVD));
        when(paymentServiceResolver.getReadPaymentStatusService(any(PisCommonPaymentResponse.class))).thenReturn(readPaymentStatusService);
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(anyString(), any(TransactionStatus.class)))
            .thenReturn(true);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(spiContextDataProvider.provideWithPsuIdData(any())).thenReturn(SPI_CONTEXT_DATA);

        // When
        paymentService.getPaymentStatusById(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_TRANSACTION_STATUS_REQUEST_RECEIVED);
    }

    @Test
    public void getPaymentStatusById_shouldStoreTransactionStatusInLoggingContext() {
        // Given
        TransactionStatus transactionStatus = RCVD;

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(pisCommonPaymentResponse));
        when(spiContextDataProvider.provideWithPsuIdData(any())).thenReturn(SPI_CONTEXT_DATA);
        when(paymentServiceResolver.getReadPaymentStatusService(any(PisCommonPaymentResponse.class))).thenReturn(readPaymentStatusService);
        when(readPaymentStatusService.readPaymentStatus(any(), any(SpiContextData.class), any(String.class)))
            .thenReturn(new ReadPaymentStatusResponse(transactionStatus));
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(anyString(), any(TransactionStatus.class)))
            .thenReturn(true);

        // When
        ResponseObject<GetPaymentStatusResponse> response = paymentService.getPaymentStatusById(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID);

        // Then
        assertFalse(response.hasError());
        verify(loggingContextService).storeTransactionStatus(transactionStatus);
    }

    @Test
    public void getPaymentStatusById_withInvalidPaymentResponse_shouldReturnValidationError() {
        // Given
        when(getPaymentStatusByIdValidator.validate(any(GetPaymentStatusByIdPO.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(invalidPisCommonPaymentResponse));
        PaymentType paymentType = PaymentType.SINGLE;

        // When
        ResponseObject actualResponse = paymentService.getPaymentStatusById(paymentType, PAYMENT_PRODUCT, PAYMENT_ID);

        // Then
        verify(getPaymentStatusByIdValidator).validate(new GetPaymentStatusByIdPO(invalidPisCommonPaymentResponse, paymentType, PAYMENT_PRODUCT));
        assertThatErrorIs(actualResponse, VALIDATION_ERROR);
    }

    @Test
    public void getPaymentStatusById_Failure_WrongId() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject actualResult = paymentService.getPaymentStatusById(PaymentType.SINGLE, PAYMENT_PRODUCT, WRONG_PAYMENT_ID);

        // Then
        assertThatPaymentHasWrongId(actualResult);
    }

    @Test
    public void getPaymentStatusById_Success_FundsAvailableIsNull() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(pisCommonPaymentResponse));
        when(readPaymentStatusService.readPaymentStatus(any(), any(SpiContextData.class), any(String.class)))
            .thenReturn(new ReadPaymentStatusResponse(ACCP));
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(anyString(), any(TransactionStatus.class)))
            .thenReturn(true);
        when(spiContextDataProvider.provideWithPsuIdData(any())).thenReturn(SPI_CONTEXT_DATA);
        when(paymentServiceResolver.getReadPaymentStatusService(any())).thenReturn(readPaymentStatusService);
        // When
        ResponseObject<GetPaymentStatusResponse> response = paymentService.getPaymentStatusById(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID);

        // Then
        assertThat(response.getBody()).isNotNull();
        GetPaymentStatusResponse getPaymentResponse = response.getBody();
        assertThat(getPaymentResponse.getTransactionStatus()).isEqualTo(ACCP);
        assertThat(getPaymentResponse.getFundsAvailable()).isNull();
    }

    @Test
    public void getPaymentStatusById_Success_FundsAvailableIsTrue() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(pisCommonPaymentResponse));
        when(readPaymentStatusService.readPaymentStatus(any(), any(SpiContextData.class), any(String.class)))
            .thenReturn(new ReadPaymentStatusResponse(ACCP, true));
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(anyString(), any(TransactionStatus.class)))
            .thenReturn(true);
        when(spiContextDataProvider.provideWithPsuIdData(any())).thenReturn(SPI_CONTEXT_DATA);
        when(paymentServiceResolver.getReadPaymentStatusService(any())).thenReturn(readPaymentStatusService);

        // When
        ResponseObject<GetPaymentStatusResponse> response = paymentService.getPaymentStatusById(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID);

        // Then
        assertThat(response.getBody()).isNotNull();
        GetPaymentStatusResponse getPaymentResponse = response.getBody();
        assertThat(getPaymentResponse.getTransactionStatus()).isEqualTo(ACCP);
        assertThat(getPaymentResponse.getFundsAvailable()).isEqualTo(true);
    }

    @Test
    public void cancelPayment_Success() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(pisCommonPaymentResponse));
        when(pisCommonPaymentResponse.getTransactionStatus()).thenReturn(ACCP);
        when(paymentServiceResolver.getCancelPaymentService(any())).thenReturn(cancelCertainPaymentService);
        when(cancelCertainPaymentService.cancelPayment(any(), any())).thenReturn(ResponseObject.<CancelPaymentResponse>builder().body(getCancelPaymentResponse()).build());

        // When
        ResponseObject<CancelPaymentResponse> actual = paymentService.cancelPayment(
            new PisPaymentCancellationRequest(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, false, new TppRedirectUri("", "")));

        // Then
        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody().getTransactionStatus()).isEqualTo(CANC);
        assertThat(actual.getBody().isStartAuthorisationRequired()).isTrue();
    }

    @Test
    public void cancelPayment_Success_ShouldRecordEvent() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(pisCommonPaymentResponse));
        when(pisCommonPaymentResponse.getTransactionStatus()).thenReturn(RCVD);

        when(paymentServiceResolver.getCancelPaymentService(any())).thenReturn(cancelCertainPaymentService);
        CancelPaymentResponse cancelPaymentResponse = getCancelPaymentResponse();
        when(cancelCertainPaymentService.cancelPayment(any(), any())).thenReturn(ResponseObject.<CancelPaymentResponse>builder().body(cancelPaymentResponse).build());

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentService.cancelPayment(new PisPaymentCancellationRequest(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, false, null));

        // Then
        verify(xs2aEventService).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.PAYMENT_CANCELLATION_REQUEST_RECEIVED);
    }

    @Test
    public void cancelPayment_shouldStoreTransactionStatusInLoggingContext() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(pisCommonPaymentResponse));
        when(pisCommonPaymentResponse.getTransactionStatus()).thenReturn(RCVD);

        when(paymentServiceResolver.getCancelPaymentService(any())).thenReturn(cancelCertainPaymentService);
        CancelPaymentResponse cancelPaymentResponse = getCancelPaymentResponse();
        when(cancelCertainPaymentService.cancelPayment(any(), any())).thenReturn(ResponseObject.<CancelPaymentResponse>builder().body(cancelPaymentResponse).build());

        TransactionStatus expectedTransactionStatus = cancelPaymentResponse.getTransactionStatus();

        // When
        ResponseObject<CancelPaymentResponse> response = paymentService.cancelPayment(new PisPaymentCancellationRequest(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, false, null));

        // Then
        assertFalse(response.hasError());
        verify(loggingContextService).storeTransactionStatus(expectedTransactionStatus);
    }

    @Test
    public void cancelPayment_withInvalidPaymentResponse_shouldReturnValidationError() {
        // Given
        when(cancelPaymentValidator.validate(any(CancelPaymentPO.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(anyString())).thenReturn(Optional.of(invalidPisCommonPaymentResponse));
        PaymentType paymentType = PaymentType.SINGLE;

        // When
        ResponseObject actualResponse = paymentService.cancelPayment(
            new PisPaymentCancellationRequest(paymentType, PAYMENT_PRODUCT, PAYMENT_ID, false, null));

        // Then
        verify(cancelPaymentValidator).validate(new CancelPaymentPO(invalidPisCommonPaymentResponse, paymentType, PAYMENT_PRODUCT, null));
        assertThatErrorIs(actualResponse, VALIDATION_ERROR);
    }

    @Test
    public void cancelPayment_Failure_WrongId() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject actualResult = paymentService.cancelPayment(
            new PisPaymentCancellationRequest(PaymentType.SINGLE, PAYMENT_PRODUCT, WRONG_PAYMENT_ID, false, null));

        // Then
        assertThatPaymentHasWrongId(actualResult);
    }

    @Test
    public void cancelPayment_Fail_FinalisedTransactionStatus() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(getFinalisedPisCommonPayment()));

        // When
        ResponseObject<CancelPaymentResponse> actualResult = paymentService.cancelPayment(
            new PisPaymentCancellationRequest(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, false, null));

        // Then
        assertThat(actualResult.getError()).isNotNull();
        assertThat(actualResult.getError().getErrorType()).isEqualTo(PIS_400);
        assertThat(actualResult.getError().getTppMessages().contains(of(RESOURCE_BLOCKED))).isTrue();
    }

    private void assertThatPaymentWasCreated(ResponseObject<PaymentInitiationResponse> actualResponse) {
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }

    private void assertThatErrorIs(ResponseObject actualResponse, MessageError messageError) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(messageError);
    }

    private void assertThatPaymentHasWrongId(ResponseObject actualResult) {
        assertThat(actualResult.hasError()).isTrue();
        assertThat(actualResult.getError().getErrorType()).isEqualTo(PIS_404);
        assertThat(actualResult.getError().getTppMessages().contains(of(RESOURCE_UNKNOWN_404_NO_PAYMENT))).isTrue();
    }

    private BulkPaymentInitiationResponse getBulkResponses() {
        BulkPaymentInitiationResponse response = new BulkPaymentInitiationResponse();
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setPaymentId(PAYMENT_ID);
        return response;
    }

    private static TppInfo getTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        tppInfo.setAuthorityName("authorityName");
        tppInfo.setCountry("country");
        tppInfo.setOrganisation("organisation");
        tppInfo.setOrganisationUnit("organisationUnit");
        tppInfo.setCity("city");
        tppInfo.setState("state");
        return tppInfo;
    }

    private static TppInfo getTppInfoServiceModified() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        tppInfo.setAuthorityName("authorityName");
        tppInfo.setCountry("country");
        tppInfo.setOrganisation("organisation");
        tppInfo.setOrganisationUnit("organisationUnit");
        tppInfo.setCity("city");
        tppInfo.setState("state");

        return tppInfo;
    }

    private PaymentInitiationParameters buildPaymentInitiationParameters(PaymentType type) {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(type);
        requestParameters.setPaymentProduct(PAYMENT_PRODUCT);
        requestParameters.setPsuData(PSU_ID_DATA);
        return requestParameters;
    }

    private PaymentInitiationParameters buildInvalidPaymentInitiationParameters() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPsuData(new PsuIdData(null, null, null, null));
        return requestParameters;
    }

    private SinglePaymentInitiationResponse buildSinglePaymentInitiationResponse() {
        SinglePaymentInitiationResponse response = new SinglePaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspConsentDataProvider(initialSpiAspspConsentDataProvider);
        return response;
    }

    private PeriodicPaymentInitiationResponse buildPeriodicPaymentInitiationResponse() {
        PeriodicPaymentInitiationResponse response = new PeriodicPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspConsentDataProvider(initialSpiAspspConsentDataProvider);
        return response;
    }

    private ResponseObject<PaymentInitiationResponse> getValidResponse() {
        return ResponseObject.<PaymentInitiationResponse>builder().body(getBulkResponses()).build();
    }

    private CancelPaymentResponse getCancelPaymentResponse() {
        CancelPaymentResponse cancelPaymentResponse = jsonReader.getObjectFromFile("json/service/mapper/cancel-payment-response.json", CancelPaymentResponse.class);
        cancelPaymentResponse.setTransactionStatus(CANC);
        return cancelPaymentResponse;
    }

    private Optional<PisCommonPaymentResponse> getPisCommonPayment() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/pis-payment.json", PisPayment.class);
        response.setPayments(Collections.singletonList(pisPayment));
        response.setPaymentProduct(PAYMENT_PRODUCT);
        return Optional.of(response);
    }

    private PisCommonPaymentResponse getFinalisedPisCommonPayment() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPayments(getFinalisedPisPayment());
        response.setTransactionStatus(TransactionStatus.ACCC);
        return response;
    }

    private List<PisPayment> getFinalisedPisPayment() {
        PisPayment pisPayment = new PisPayment();
        pisPayment.setTransactionStatus(TransactionStatus.RJCT);
        return Collections.singletonList(pisPayment);
    }
}
