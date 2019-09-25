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

package de.adorsys.psd2.xs2a.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.*;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ConsentService;
import de.adorsys.psd2.xs2a.service.PaymentAuthorisationService;
import de.adorsys.psd2.xs2a.service.PaymentCancellationAuthorisationService;
import de.adorsys.psd2.xs2a.service.PaymentService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.header.PaymentCancellationHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.PaymentInitiationHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.function.Function;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_403;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_404;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentControllerTest {
    private static final String CORRECT_PAYMENT_ID = "33333-444444-55555-55555";
    private static final String CORRECT_PAYMENT_ID_2 = "33333-444444-55555-66666";
    private static final String WRONG_PAYMENT_ID = "wrong_payment_id";
    private static final String PSU_ID = "PSU ID";
    private static final String PSU_ID_TYPE = "PSU ID TYPE";
    private static final String PSU_CORPORATE_ID = "PSU CORPORATE ID";
    private static final String PSU_CORPORATE_ID_TYPE = "PSU CORPORATE ID TYPE";
    private static final String REDIRECT_LINK = "http://localhost:4200/consent/confirmation/pis";
    private static final UUID REQUEST_ID = UUID.fromString("ddd36e05-d67a-4830-93ad-9462f71ae1e6");
    private static final String AUTHORISATION_ID = "3e96e9e0-9974-42aa-beb8-003e91416652";
    private static final String CANCELLATION_AUTHORISATION_ID = "d7ba791c-2231-4ed5-8232-cb1ad4cf7332";
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_SERVICE = "Wrong payment service";
    private static final String CORRECT_PAYMENT_SERVICE = "payments";
    private static final Object XML_SCT = new Object();
    private static final PeriodicPaymentInitiationXmlPart2StandingorderTypeJson JSON_STANDING_ORDER_TYPE = new PeriodicPaymentInitiationXmlPart2StandingorderTypeJson();
    private static final MessageError PIS_400_MESSAGE_ERROR = new MessageError(ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final MessageError PIS_404_MESSAGE_ERROR = new MessageError(ErrorType.PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404));
    private static final boolean TPP_REDIRECT_PREFERRED_TRUE = true;
    private static final PaymentInitiationResponse PAYMENT_INITIATION_RESPONSE = new SinglePaymentInitiationResponse();
    private static final PaymentInitationRequestResponse201 PAYMENT_OBJECT = new PaymentInitationRequestResponse201();
    private static final ResponseHeaders RESPONSE_HEADERS = ResponseHeaders.builder().aspspScaApproach(ScaApproach.REDIRECT).build();
    private static final boolean EXPLICIT_PREFERRED_FALSE = false;
    private static final String PSU_DATA_PASSWORD_JSON_PATH = "json/web/controller/psuData-password.json";

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private PaymentModelMapperPsd2 paymentModelMapperPsd2;
    @Mock
    private PaymentModelMapperXs2a paymentModelMapperXs2a;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private ConsentService consentService;
    @Mock
    private ConsentModelMapper consentModelMapper;
    @Mock
    private PaymentAuthorisationService paymentAuthorisationService;
    @Mock
    private PaymentCancellationAuthorisationService paymentCancellationAuthorisationService;
    @Mock
    private AuthorisationMapper authorisationMapper;
    @Mock
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private PaymentService xs2aPaymentService;
    @Mock
    private PaymentInitiationParameters paymentInitiationParameters;
    @Mock
    private PaymentInitiationHeadersBuilder paymentInitiationHeadersBuilder;
    @Mock
    private AuthorisationModelMapper authorisationModelMapper;
    @Mock
    private PaymentCancellationHeadersBuilder paymentCancellationHeadersBuilder;
    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        when(xs2aPaymentService.getPaymentById(eq(SINGLE), eq(PRODUCT), eq(CORRECT_PAYMENT_ID)))
            .thenReturn(ResponseObject.builder().body(getXs2aPayment()).build());

        when(xs2aPaymentService.getPaymentStatusById(eq(PaymentType.SINGLE), eq(PRODUCT), eq(CORRECT_PAYMENT_ID)))
            .thenReturn(ResponseObject.<GetPaymentStatusResponse>builder().body(new GetPaymentStatusResponse(TransactionStatus.ACCP, null)).build());
        when(xs2aPaymentService.getPaymentStatusById(eq(PaymentType.SINGLE), eq(PRODUCT), eq(WRONG_PAYMENT_ID)))
            .thenReturn(ResponseObject.<GetPaymentStatusResponse>builder().body(new GetPaymentStatusResponse(TransactionStatus.ACCP, null)).build());

    }

    @Test
    public void getPaymentById() {
        doReturn(new ResponseEntity<>(getPaymentInitiationResponse(de.adorsys.psd2.model.TransactionStatus.ACCP), OK))
            .when(responseMapper).ok(any());

        //Given:
        Object expectedBody = getPaymentInitiationResponse(de.adorsys.psd2.model.TransactionStatus.ACCP);

        //When
        ResponseEntity response = paymentController.getPaymentInformation(SINGLE.getValue(), PRODUCT, CORRECT_PAYMENT_ID,
                                                                          REQUEST_ID, null, null, null, null, null,
                                                                          null, null, null, null, null,
                                                                          null, null, null);

        //Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualToComparingFieldByField(expectedBody);
    }

    @Test
    public void getPaymentById_Failure() {
        when(responseErrorMapper.generateErrorResponse(createMessageError(ErrorType.PIS_404, RESOURCE_UNKNOWN_404))).thenReturn(ResponseEntity.status(FORBIDDEN).build());

        //When
        ResponseEntity response = paymentController.getPaymentInformation(SINGLE.getValue(), WRONG_PAYMENT_ID,
                                                                          null, null, null, null, null, null,
                                                                          null, null, null, null, null,
                                                                          null, null, null, null);

        //Then
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    private PaymentInitiationStatusResponse200Json getPaymentInitiationResponse(de.adorsys.psd2.model.TransactionStatus transactionStatus) {
        PaymentInitiationStatusResponse200Json response = new PaymentInitiationStatusResponse200Json();
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    private Object getXs2aPayment() {
        SinglePayment payment = new SinglePayment();
        payment.setEndToEndIdentification(CORRECT_PAYMENT_ID);
        return payment;
    }

    @Test
    public void getTransactionStatusById_Success() {
        doReturn(new ResponseEntity<>(getPaymentInitiationStatus(de.adorsys.psd2.model.TransactionStatus.ACCP), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        when(xs2aPaymentService.getPaymentStatusById(SINGLE, PRODUCT, CORRECT_PAYMENT_ID)).thenReturn(ResponseObject.<GetPaymentStatusResponse>builder().body(new GetPaymentStatusResponse(TransactionStatus.ACCP, null)).build());

        //Given:
        PaymentInitiationStatusResponse200Json expectedBody = getPaymentInitiationStatus(de.adorsys.psd2.model.TransactionStatus.ACCP);

        //When:
        ResponseEntity<PaymentInitiationStatusResponse200Json> actualResponse =
            (ResponseEntity<PaymentInitiationStatusResponse200Json>) paymentController.getPaymentInitiationStatus(
                PaymentType.SINGLE.getValue(), PRODUCT, CORRECT_PAYMENT_ID, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null);

        //Then:
        HttpStatus actualHttpStatus = actualResponse.getStatusCode();
        assertThat(actualHttpStatus).isEqualTo(OK);
        assertThat(actualResponse.getBody()).isEqualTo(expectedBody);
    }

    private PaymentInitiationStatusResponse200Json getPaymentInitiationStatus(de.adorsys.psd2.model.TransactionStatus transactionStatus) {
        PaymentInitiationStatusResponse200Json response = new PaymentInitiationStatusResponse200Json();
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    @Test
    public void getTransactionStatusById_WrongId() {
        when(responseErrorMapper.generateErrorResponse(createMessageError(PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());
        when(xs2aPaymentService.getPaymentStatusById(SINGLE, PRODUCT, WRONG_PAYMENT_ID)).thenReturn(ResponseObject.<GetPaymentStatusResponse>builder().fail(createMessageError(PIS_403, RESOURCE_UNKNOWN_403)).build());
        //Given:

        //When:
        ResponseEntity<PaymentInitiationStatusResponse200Json> actualResponse =
            (ResponseEntity<PaymentInitiationStatusResponse200Json>) paymentController.getPaymentInitiationStatus(
                PaymentType.SINGLE.getValue(), PRODUCT, WRONG_PAYMENT_ID, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null);

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void cancelPayment_WithoutAuthorisation_Success() {
        PisPaymentCancellationRequest paymentCancellationRequest = new PisPaymentCancellationRequest(SINGLE, PRODUCT, CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null);
        when(paymentModelMapperPsd2.mapToPaymentCancellationRequest(PRODUCT, SINGLE.getValue(), CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null, null))
            .thenReturn(paymentCancellationRequest);
        when(xs2aPaymentService.cancelPayment(paymentCancellationRequest)).thenReturn(getCancelPaymentResponseObject(false));

        // Given
        PaymentInitiationCancelResponse202 response = getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.CANC);
        ResponseEntity<PaymentInitiationCancelResponse202> expectedResult = new ResponseEntity<>(response, NO_CONTENT);

        when(paymentModelMapperPsd2.mapToPaymentInitiationCancelResponse(any())).thenReturn(response);
        when(responseMapper.delete(any())).thenReturn(expectedResult);

        // When
        ResponseEntity<PaymentInitiationCancelResponse202> actualResult = (ResponseEntity<PaymentInitiationCancelResponse202>) paymentController.cancelPayment(SINGLE.getValue(), PRODUCT,
                                                                                                                                                               CORRECT_PAYMENT_ID, null, null,
                                                                                                                                                               null, null, null, null, null,
                                                                                                                                                               null, null,
                                                                                                                                                               null, null, null, null, null, null, null, null, EXPLICIT_PREFERRED_FALSE);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(NO_CONTENT);
        assertThat(actualResult.getBody()).isEqualTo(response);
    }

    @Test
    public void cancelPayment_WithAuthorisation_Success() {
        PisPaymentCancellationRequest paymentCancellationRequest = new PisPaymentCancellationRequest(SINGLE, PRODUCT, CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null);

        when(responseMapper.accepted(any()))
            .thenReturn(new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.ACTC), HttpStatus.ACCEPTED));
        when(paymentModelMapperPsd2.mapToPaymentCancellationRequest(PRODUCT, SINGLE.getValue(), CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null, null))
            .thenReturn(paymentCancellationRequest);
        when(xs2aPaymentService.cancelPayment(paymentCancellationRequest)).thenReturn(getCancelPaymentResponseObject(true));

        // Given
        ResponseEntity<PaymentInitiationCancelResponse202> expectedResult = new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.ACTC), HttpStatus.ACCEPTED);

        // When
        ResponseEntity<PaymentInitiationCancelResponse202> actualResult = (ResponseEntity<PaymentInitiationCancelResponse202>) paymentController.cancelPayment(PaymentType.SINGLE.getValue(), PRODUCT,
                                                                                                                                                               CORRECT_PAYMENT_ID, null, null, null,
                                                                                                                                                               null, null, null, null, null,
                                                                                                                                                               null, null,
                                                                                                                                                               null, null, null, null, null,
                                                                                                                                                               null, null, EXPLICIT_PREFERRED_FALSE);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    @Test
    public void cancelPayment_WithoutAuthorisation_Fail_FinalisedStatus() {
        ResponseObject<CancelPaymentResponse> cancelPaymentResponse = getErrorOnPaymentCancellation();
        ResponseEntity expectedResult = ResponseEntity.status(BAD_REQUEST).build();

        when(responseErrorMapper.generateErrorResponse(cancelPaymentResponse.getError())).thenReturn(expectedResult);

        // Given
        ResponseEntity actualResult = paymentController.cancelPayment(SINGLE.getValue(), PRODUCT,
                                                                      CORRECT_PAYMENT_ID, REQUEST_ID, null, null,
                                                                      null, null, null, null, null,
                                                                      null, null,
                                                                      null, null, null, null,
                                                                      null, null, null, EXPLICIT_PREFERRED_FALSE);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
    }

    @Test
    public void cancelPayment_WithAuthorisation_Fail_FinalisedStatus() {
        PisPaymentCancellationRequest paymentCancellationRequest = new PisPaymentCancellationRequest(SINGLE, PRODUCT, CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null);

        when(paymentModelMapperPsd2.mapToPaymentCancellationRequest(PRODUCT, SINGLE.getValue(), CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null, null))
            .thenReturn(paymentCancellationRequest);
        when(xs2aPaymentService.cancelPayment(paymentCancellationRequest)).thenReturn(getErrorOnPaymentCancellation());
        when(responseErrorMapper.generateErrorResponse(createMessageError(ErrorType.PIS_400, FORMAT_ERROR))).thenReturn(ResponseEntity.status(BAD_REQUEST).build());

        // Given
        ResponseEntity<PaymentInitiationCancelResponse202> expectedResult = ResponseEntity.badRequest().build();
        // When
        ResponseEntity actualResult = paymentController.cancelPayment(PaymentType.SINGLE.getValue(), PRODUCT,
                                                                      CORRECT_PAYMENT_ID, REQUEST_ID, null, null,
                                                                      null, null, null, null,
                                                                      null, null,
                                                                      null, null, null, null, null,
                                                                      null, null, null, EXPLICIT_PREFERRED_FALSE);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
    }

    @Test
    public void getPaymentInitiationScaStatus_success() {
        ResponseObject<de.adorsys.psd2.xs2a.core.sca.ScaStatus> responseObject = ResponseObject.<de.adorsys.psd2.xs2a.core.sca.ScaStatus>builder()
                                                                                     .body(de.adorsys.psd2.xs2a.core.sca.ScaStatus.RECEIVED)
                                                                                     .build();
        when(paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(any(String.class), any(String.class)))
            .thenReturn(responseObject);
        doReturn(ResponseEntity.ok(buildScaStatusResponse(ScaStatus.RECEIVED)))
            .when(responseMapper).ok(eq(responseObject), any());

        // Given
        ScaStatusResponse expected = buildScaStatusResponse(ScaStatus.RECEIVED);

        // When
        ResponseEntity actual = paymentController.getPaymentInitiationScaStatus(SINGLE.getValue(), PRODUCT, CORRECT_PAYMENT_ID,
                                                                                AUTHORISATION_ID, REQUEST_ID,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    public void getPaymentInitiationScaStatus_failure() {
        when(paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(WRONG_PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(buildScaStatusError());
        when(responseErrorMapper.generateErrorResponse(createMessageError(PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());

        // When
        ResponseEntity actual = paymentController.getPaymentInitiationScaStatus(SINGLE.getValue(), PRODUCT, WRONG_PAYMENT_ID,
                                                                                AUTHORISATION_ID, REQUEST_ID,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void getPaymentCancellationScaStatus_success() {
        ResponseObject<de.adorsys.psd2.xs2a.core.sca.ScaStatus> responseObject = ResponseObject.<de.adorsys.psd2.xs2a.core.sca.ScaStatus>builder()
                                                                                     .body(de.adorsys.psd2.xs2a.core.sca.ScaStatus.RECEIVED)
                                                                                     .build();
        when(paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(any(String.class), any(String.class)))
            .thenReturn(responseObject);
        doReturn(ResponseEntity.ok(buildScaStatusResponse(ScaStatus.RECEIVED)))
            .when(responseMapper).ok(eq(responseObject), any());

        // Given
        ScaStatusResponse expected = buildScaStatusResponse(ScaStatus.RECEIVED);

        // When
        ResponseEntity actual = paymentController.getPaymentCancellationScaStatus(SINGLE.getValue(), PRODUCT, CORRECT_PAYMENT_ID,
                                                                                  CANCELLATION_AUTHORISATION_ID, REQUEST_ID,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    public void getPaymentCancellationScaStatus_failure() {
        when(paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(buildScaStatusError());
        when(responseErrorMapper.generateErrorResponse(createMessageError(PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());

        // When
        ResponseEntity actual = paymentController.getPaymentCancellationScaStatus(SINGLE.getValue(), PRODUCT, WRONG_PAYMENT_ID,
                                                                                  CANCELLATION_AUTHORISATION_ID, REQUEST_ID,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformationClassCheck_success() {
        List<String> cancellationsList = Collections.singletonList(CORRECT_PAYMENT_ID);
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseList(cancellationsList);
        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_ID))
            .thenReturn(cancellationResponseList);

        ResponseEntity cancellationsResponseEntity = ResponseEntity.ok(buildCancellations(cancellationsList));
        when(responseMapper.ok(eq(cancellationResponseList), any()))
            .thenReturn(cancellationsResponseEntity);

        // When
        ResponseEntity actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(null, null, CORRECT_PAYMENT_ID,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertTrue(actual.getBody() instanceof Cancellations);
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformation_MethodMapperCheck() {
        //noinspection unchecked
        ArgumentCaptor<Function<Xs2aPaymentCancellationAuthorisationSubResource, Cancellations>> argumentCaptor = ArgumentCaptor.forClass(Function.class);

        List<String> cancellationsList = Collections.singletonList(CORRECT_PAYMENT_ID);
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseList(cancellationsList);
        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_ID))
            .thenReturn(cancellationResponseList);

        ResponseEntity cancellationsResponseEntity = ResponseEntity.ok(buildCancellations(cancellationsList));
        when(responseMapper.ok(eq(cancellationResponseList), argumentCaptor.capture()))
            .thenReturn(cancellationsResponseEntity);

        // When
        paymentController.getPaymentInitiationCancellationAuthorisationInformation(null, null, CORRECT_PAYMENT_ID,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null);

        // Then
        argumentCaptor.getValue().apply(null);
        verify(consentModelMapper).mapToCancellations(any());
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformation_success() {
        //Given
        List<String> cancellationsList = Collections.singletonList(CORRECT_PAYMENT_ID);
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseList(cancellationsList);

        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_ID))
            .thenReturn(cancellationResponseList);

        ResponseEntity cancellationsResponseEntity = ResponseEntity.ok(buildCancellations(cancellationsList));
        when(responseMapper.ok(eq(cancellationResponseList), any()))
            .thenReturn(cancellationsResponseEntity);

        // When
        ResponseEntity actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(null, null, CORRECT_PAYMENT_ID,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(((Cancellations) actual.getBody()).getCancellationIds().size()).isEqualTo(1);
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformationManyIds_success() {
        List<String> cancellationsList = Arrays.asList(CORRECT_PAYMENT_ID, CORRECT_PAYMENT_ID_2);
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseList(cancellationsList);

        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_ID))
            .thenReturn(cancellationResponseList);

        ResponseEntity cancellationsResponseEntity = ResponseEntity.ok(buildCancellations(cancellationsList));
        when(responseMapper.ok(eq(cancellationResponseList), any()))
            .thenReturn(cancellationsResponseEntity);

        // When
        ResponseEntity actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(null, null, CORRECT_PAYMENT_ID,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null);

        // Then
        assertThat(((Cancellations) actual.getBody()).getCancellationIds().size()).isEqualTo(2);
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformationWithNull_success() {
        List<String> cancellationsList = new ArrayList<>();
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseNullList();
        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(anyString()))
            .thenReturn(cancellationResponseList);

        ResponseEntity cancellationsResponseEntity = ResponseEntity.ok(buildCancellations(cancellationsList));
        when(responseMapper.ok(eq(cancellationResponseList), any()))
            .thenReturn(cancellationsResponseEntity);

        // When
        ResponseEntity actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(null, null, CORRECT_PAYMENT_ID,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null);
        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        Cancellations cancellations = (Cancellations) actual.getBody();
        assertTrue(CollectionUtils.isEmpty(cancellations.getCancellationIds()));
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformation_error() {
        when(responseErrorMapper.generateErrorResponse(any()))
            .thenReturn(ResponseEntity.status(FORBIDDEN).build());

        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(anyString()))
            .thenReturn(getCancellationResponseWithError());

        // When
        ResponseEntity actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(null, null, CORRECT_PAYMENT_ID,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null,
                                                                                                           null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void initiatePayment_Failure_PaymentServiceIsNotPresent() {
        when(responseErrorMapper.generateErrorResponse(PIS_404_MESSAGE_ERROR))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity actual = paymentController.initiatePayment(null, REQUEST_ID, null, WRONG_PAYMENT_SERVICE, PRODUCT,
                                                                  null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                  null, TPP_REDIRECT_PREFERRED_TRUE, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                  null, null, null, null, null, null,
                                                                  null, null, null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void initiatePayment_Failure_CreatePaymentHasError() {
        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, null, REDIRECT_LINK, REDIRECT_LINK, true, buildPsuIdData()))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aPayment(any(), eq(paymentInitiationParameters)))
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildFailResponseObject());

        when(responseErrorMapper.generateErrorResponse(PIS_400_MESSAGE_ERROR))
            .thenReturn(new ResponseEntity<>(BAD_REQUEST));

        Object jsonRequestObject = new Object();

        // When
        ResponseEntity actual = paymentController.initiatePayment(jsonRequestObject, REQUEST_ID, null, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                  null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                  null, TPP_REDIRECT_PREFERRED_TRUE, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                  null, null, null, null, null, null,
                                                                  null, null, null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void initiatePayment_Success() {
        // Given
        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, null, REDIRECT_LINK, REDIRECT_LINK, true, buildPsuIdData()))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aPayment(any(), eq(paymentInitiationParameters)))
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildSuccessResponseObjectWithLinks());

        ResponseObject expectedResponseObject = buildSuccessResponseObjectWithLinks();

        when(responseMapper.created(any(ResponseObject.class), eq(RESPONSE_HEADERS)))
            .thenReturn(new ResponseEntity<>(expectedResponseObject, CREATED));

        when(paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(any(), any())).thenReturn(RESPONSE_HEADERS);

        Object jsonRequestObject = new Object();

        // When
        ResponseEntity actual = paymentController.initiatePayment(jsonRequestObject, REQUEST_ID, null, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                  null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                  null, TPP_REDIRECT_PREFERRED_TRUE, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                  null, null, null, null, null, null,
                                                                  null, null, null, null, null);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);
        assertThat(actual.getBody()).isEqualTo(expectedResponseObject);
    }

    @Test
    public void initiatePayment_XML_Failure_PaymentServiceIsNotPresent() {
        when(responseErrorMapper.generateErrorResponse(PIS_404_MESSAGE_ERROR))
            .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When
        ResponseEntity actual = paymentController.initiatePayment(REQUEST_ID, null, WRONG_PAYMENT_SERVICE, PRODUCT,
                                                                  XML_SCT, JSON_STANDING_ORDER_TYPE, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                  null, TPP_REDIRECT_PREFERRED_TRUE, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                  null, null, null, null, null, null,
                                                                  null, null, null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void initiatePayment_XML_Failure_CreatePaymentHasError() {
        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, null, REDIRECT_LINK, REDIRECT_LINK, true, buildPsuIdData()))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aRawPayment(paymentInitiationParameters, XML_SCT, JSON_STANDING_ORDER_TYPE))
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildFailResponseObject());

        when(responseErrorMapper.generateErrorResponse(PIS_400_MESSAGE_ERROR))
            .thenReturn(new ResponseEntity<>(BAD_REQUEST));

        // When
        ResponseEntity actual = paymentController.initiatePayment(REQUEST_ID, null, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                  XML_SCT, JSON_STANDING_ORDER_TYPE, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                  null, TPP_REDIRECT_PREFERRED_TRUE, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                  null, null, null, null, null, null,
                                                                  null, null, null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void initiatePayment_XML_Success() {
        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, null, REDIRECT_LINK, REDIRECT_LINK, true, buildPsuIdData()))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aRawPayment(paymentInitiationParameters, XML_SCT, JSON_STANDING_ORDER_TYPE))
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildSuccessResponseObjectWithLinks());

        ResponseObject expectedResponseObject = buildSuccessResponseObjectWithLinks();

        when(paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(any(), any())).thenReturn(RESPONSE_HEADERS);

        when(responseMapper.created(any(ResponseObject.class), eq(RESPONSE_HEADERS)))
            .thenReturn(new ResponseEntity<>(expectedResponseObject, CREATED));

        // When
        ResponseEntity actual = paymentController.initiatePayment(REQUEST_ID, null, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                  XML_SCT, JSON_STANDING_ORDER_TYPE, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                  null, TPP_REDIRECT_PREFERRED_TRUE, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                  null, null, null, null, null, null,
                                                                  null, null, null, null, null);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);
        assertThat(actual.getBody()).isEqualTo(expectedResponseObject);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initiatePayment_XML_WithException() {
        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, null, REDIRECT_LINK, REDIRECT_LINK, true, buildPsuIdData()))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aRawPayment(paymentInitiationParameters, XML_SCT, JSON_STANDING_ORDER_TYPE))
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildSuccessResponseObjectWithLinksNullType());

        ResponseObject expectedResponseObject = buildSuccessResponseObjectWithLinksNullType();

        paymentController.initiatePayment(REQUEST_ID, null, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                          XML_SCT, JSON_STANDING_ORDER_TYPE, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                          null, TPP_REDIRECT_PREFERRED_TRUE, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                          null, null, null, null, null, null,
                                          null, null, null, null, null);
    }

    @Test
    public void startPaymentInitiationCancellationAuthorisation() {
        // Given
        String password = "some password";
        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);

        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(CORRECT_PAYMENT_ID, psuIdData, PRODUCT, CORRECT_PAYMENT_SERVICE, password);
        when(authorisationMapper.mapToXs2aCreatePisAuthorisationRequest(any(), anyString(), anyString(), anyString(), any()))
            .thenReturn(request);

        ResponseObject<CancellationAuthorisationResponse> serviceResponse = ResponseObject.<CancellationAuthorisationResponse>builder()
                                                                                .body(new Xs2aUpdatePisCommonPaymentPsuDataResponse(de.adorsys.psd2.xs2a.core.sca.ScaStatus.PSUIDENTIFIED, CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, psuIdData))
                                                                                .build();
        when(paymentCancellationAuthorisationService.createPisCancellationAuthorisation(request))
            .thenReturn(serviceResponse);

        StartCancellationScaProcessResponse expectedResponse = new StartCancellationScaProcessResponse();
        // noinspection unchecked
        when(responseMapper.created(eq(serviceResponse), any(), eq(RESPONSE_HEADERS)))
            .thenReturn(new ResponseEntity(expectedResponse, CREATED));

        when(paymentCancellationHeadersBuilder.buildStartPaymentCancellationAuthorisationHeaders(CANCELLATION_AUTHORISATION_ID)).thenReturn(RESPONSE_HEADERS);

        // When
        ResponseEntity actual = paymentController.startPaymentInitiationCancellationAuthorisation(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                                  CORRECT_PAYMENT_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                                                  TPP_REDIRECT_PREFERRED_TRUE, REDIRECT_LINK, REDIRECT_LINK, null, null, null,
                                                                                                  null, null, null, null, null, null,
                                                                                                  null, null, null);

        // Then
        assertNotNull(actual);
        assertEquals(CREATED, actual.getStatusCode());
        assertEquals(expectedResponse, actual.getBody());

        verify(responseMapper).created(eq(serviceResponse), any(), eq(RESPONSE_HEADERS));
        verify(paymentCancellationHeadersBuilder).buildStartPaymentCancellationAuthorisationHeaders(CANCELLATION_AUTHORISATION_ID);

        verify(responseErrorMapper, never()).generateErrorResponse(any(), any());
    }

    @Test
    public void startPaymentInitiationCancellationAuthorisation_withServiceError_shouldReturnError() {
        // Given
        String password = "some password";

        MessageError serviceError = new MessageError(PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404));

        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(CORRECT_PAYMENT_ID, psuIdData, PRODUCT, CORRECT_PAYMENT_SERVICE, password);
        when(authorisationMapper.mapToXs2aCreatePisAuthorisationRequest(any(), anyString(), anyString(), anyString(), any()))
            .thenReturn(request);
        ResponseObject<CancellationAuthorisationResponse> serviceResponse = ResponseObject.<CancellationAuthorisationResponse>builder()
                                                                                .fail(serviceError)
                                                                                .build();
        when(paymentCancellationAuthorisationService.createPisCancellationAuthorisation(request))
            .thenReturn(serviceResponse);

        Object errorResponse = new Object();
        when(responseErrorMapper.generateErrorResponse(eq(serviceError)))
            .thenReturn(new ResponseEntity<>(errorResponse, NOT_FOUND));

        // When
        ResponseEntity actual = paymentController.startPaymentInitiationCancellationAuthorisation(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                                  CORRECT_PAYMENT_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                                                  TPP_REDIRECT_PREFERRED_TRUE, REDIRECT_LINK, REDIRECT_LINK, null, null, null,
                                                                                                  null, null, null, null, null, null,
                                                                                                  null, null, null);

        // Then
        assertNotNull(actual);
        assertEquals(NOT_FOUND, actual.getStatusCode());
        assertEquals(errorResponse, actual.getBody());

        verify(responseErrorMapper).generateErrorResponse(serviceError);

        verify(paymentCancellationHeadersBuilder, never()).buildStartPaymentCancellationAuthorisationHeaders(anyString());
        verify(responseMapper, never()).created(any(), any(), any());
    }

    @Test
    public void updatePaymentCancellationPsuData() {
        // Given
        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);

        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        when(consentModelMapper.mapToPisUpdatePsuData(psuIdData, CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, CORRECT_PAYMENT_SERVICE, PRODUCT, body))
            .thenReturn(request);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                        .body(new Xs2aUpdatePisCommonPaymentPsuDataResponse(de.adorsys.psd2.xs2a.core.sca.ScaStatus.PSUIDENTIFIED, CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, psuIdData))
                                                                                        .build();

        when(paymentCancellationAuthorisationService.updatePisCancellationPsuData(request))
            .thenReturn(serviceResponse);

        when(paymentCancellationHeadersBuilder.buildUpdatePaymentCancellationPsuDataHeaders(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(RESPONSE_HEADERS);

        UpdatePsuAuthenticationResponse expectedResponse = new UpdatePsuAuthenticationResponse();
        // noinspection unchecked
        when(responseMapper.ok(eq(serviceResponse), any(), eq(RESPONSE_HEADERS)))
            .thenReturn(new ResponseEntity(expectedResponse, OK));

        // When
        ResponseEntity actual = paymentController.updatePaymentCancellationPsuData(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                   CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                                   null, null, null, null,
                                                                                   null, null, null, null, null, null);

        // Then
        assertNotNull(actual);
        assertEquals(OK, actual.getStatusCode());
        assertEquals(expectedResponse, actual.getBody());

        verify(responseMapper).ok(eq(serviceResponse), any(), eq(RESPONSE_HEADERS));
        verify(paymentCancellationHeadersBuilder).buildUpdatePaymentCancellationPsuDataHeaders(CANCELLATION_AUTHORISATION_ID);

        verify(responseErrorMapper, never()).generateErrorResponse(any());
    }

    @Test
    public void updatePaymentCancellationPsuData_withServiceError_shouldReturnError() {
        // Given
        MessageError serviceError = new MessageError(PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404));

        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        when(consentModelMapper.mapToPisUpdatePsuData(psuIdData, CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, CORRECT_PAYMENT_SERVICE, PRODUCT, body))
            .thenReturn(request);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                        .fail(serviceError)
                                                                                        .build();

        when(paymentCancellationAuthorisationService.updatePisCancellationPsuData(request))
            .thenReturn(serviceResponse);

        Object errorResponse = new Object();
        when(responseErrorMapper.generateErrorResponse(eq(serviceError)))
            .thenReturn(new ResponseEntity<>(errorResponse, NOT_FOUND));

        // When
        ResponseEntity actual = paymentController.updatePaymentCancellationPsuData(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                   CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                                   null, null, null, null,
                                                                                   null, null, null, null, null, null);


        // Then
        assertNotNull(actual);
        assertEquals(NOT_FOUND, actual.getStatusCode());
        assertEquals(errorResponse, actual.getBody());

        verify(responseErrorMapper).generateErrorResponse(serviceError);

        verify(paymentCancellationHeadersBuilder, never()).buildUpdatePaymentCancellationPsuDataHeaders(anyString());
        verify(responseMapper, never()).created(any(), any(), any());
    }

    @Test
    public void updatePaymentPsuData() {
        // Given
        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);

        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        when(consentModelMapper.mapToPisUpdatePsuData(psuIdData, CORRECT_PAYMENT_ID, AUTHORISATION_ID, CORRECT_PAYMENT_SERVICE, PRODUCT, body))
            .thenReturn(request);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                        .body(new Xs2aUpdatePisCommonPaymentPsuDataResponse(de.adorsys.psd2.xs2a.core.sca.ScaStatus.PSUIDENTIFIED, CORRECT_PAYMENT_ID, AUTHORISATION_ID, psuIdData))
                                                                                        .build();

        when(paymentAuthorisationService.updatePisCommonPaymentPsuData(request))
            .thenReturn(serviceResponse);

        when(paymentInitiationHeadersBuilder.buildUpdatePaymentInitiationPsuDataHeaders(AUTHORISATION_ID))
            .thenReturn(RESPONSE_HEADERS);

        UpdatePsuAuthenticationResponse expectedResponse = new UpdatePsuAuthenticationResponse();
        // noinspection unchecked
        when(responseMapper.ok(eq(serviceResponse), any(), eq(RESPONSE_HEADERS)))
            .thenReturn(new ResponseEntity(expectedResponse, OK));

        // When
        ResponseEntity actual = paymentController.updatePaymentPsuData(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                       CORRECT_PAYMENT_ID, AUTHORISATION_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                       null, null, null, null,
                                                                       null, null, null, null, null, null);

        // Then
        assertNotNull(actual);
        assertEquals(OK, actual.getStatusCode());
        assertEquals(expectedResponse, actual.getBody());

        verify(responseMapper).ok(eq(serviceResponse), any(), eq(RESPONSE_HEADERS));
        verify(paymentInitiationHeadersBuilder).buildUpdatePaymentInitiationPsuDataHeaders(AUTHORISATION_ID);

        verify(responseErrorMapper, never()).generateErrorResponse(any());
    }

    @Test
    public void updatePaymentPsuData_withServiceError_shouldReturnError() {
        // Given
        MessageError serviceError = new MessageError(PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404));

        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        when(consentModelMapper.mapToPisUpdatePsuData(psuIdData, CORRECT_PAYMENT_ID, AUTHORISATION_ID, CORRECT_PAYMENT_SERVICE, PRODUCT, body))
            .thenReturn(request);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                        .fail(serviceError)
                                                                                        .build();

        when(paymentAuthorisationService.updatePisCommonPaymentPsuData(request))
            .thenReturn(serviceResponse);

        Object errorResponse = new Object();
        when(responseErrorMapper.generateErrorResponse(eq(serviceError)))
            .thenReturn(new ResponseEntity<>(errorResponse, NOT_FOUND));

        // When
        ResponseEntity actual = paymentController.updatePaymentPsuData(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                       CORRECT_PAYMENT_ID, AUTHORISATION_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                       null, null, null, null,
                                                                       null, null, null, null, null, null);


        // Then
        assertNotNull(actual);
        assertEquals(NOT_FOUND, actual.getStatusCode());
        assertEquals(errorResponse, actual.getBody());

        verify(responseErrorMapper).generateErrorResponse(serviceError);

        verify(paymentInitiationHeadersBuilder, never()).buildUpdatePaymentInitiationPsuDataHeaders(anyString());
        verify(responseMapper, never()).created(any(), any(), any());
    }

    private ResponseObject<CancelPaymentResponse> getCancelPaymentResponseObject(boolean startAuthorisationRequired) {
        CancelPaymentResponse response = new CancelPaymentResponse();
        response.setStartAuthorisationRequired(startAuthorisationRequired);
        return ResponseObject.<CancelPaymentResponse>builder().body(response).build();
    }

    private PaymentInitiationCancelResponse202 getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus transactionStatus) {
        PaymentInitiationCancelResponse202 response = new PaymentInitiationCancelResponse202();
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    private ResponseObject<CancelPaymentResponse> getErrorOnPaymentCancellation() {
        return ResponseObject.<CancelPaymentResponse>builder()
                   .fail(ErrorType.PIS_400, of(MessageErrorCode.FORMAT_ERROR))
                   .build();
    }

    private ScaStatusResponse buildScaStatusResponse(ScaStatus scaStatus) {
        return new ScaStatusResponse().scaStatus(scaStatus);
    }

    private ResponseObject<de.adorsys.psd2.xs2a.core.sca.ScaStatus> buildScaStatusError() {
        return ResponseObject.<de.adorsys.psd2.xs2a.core.sca.ScaStatus>builder()
                   .fail(PIS_403, of(MessageErrorCode.RESOURCE_UNKNOWN_403))
                   .build();
    }

    private MessageError createMessageError(ErrorType errorType, MessageErrorCode errorCode) {
        return new MessageError(errorType, of(errorCode));
    }

    private ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> getCancellationResponseList(List<String> paymentIds) {
        return ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                   .body(new Xs2aPaymentCancellationAuthorisationSubResource(paymentIds))
                   .build();
    }

    private ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> getCancellationResponseNullList() {
        return ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                   .body(new Xs2aPaymentCancellationAuthorisationSubResource(null))
                   .build();
    }

    private ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> getCancellationResponseWithError() {
        return ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                   .fail(new MessageError(ErrorHolder.builder(PIS_403).tppMessages(TppMessageInformation.of(PAYMENT_FAILED)).build()))
                   .build();
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
    }

    private ResponseObject buildFailResponseObject() {
        return ResponseObject.builder()
                   .fail(PIS_400_MESSAGE_ERROR)
                   .build();
    }

    private ResponseObject<PaymentInitiationResponse> buildSuccessResponseObject() {
        return ResponseObject.<PaymentInitiationResponse>builder()
                   .body(PAYMENT_INITIATION_RESPONSE)
                   .build();
    }

    private ResponseObject<PaymentInitiationResponse> buildSuccessResponseObjectWithLinks() {
        PaymentInitiationResponse initiationResponse = new SinglePaymentInitiationResponse();
        Links links = new Links();
        links.setSelf(new HrefType("type"));
        initiationResponse.setLinks(links);
        return ResponseObject.<PaymentInitiationResponse>builder()
                   .body(initiationResponse)
                   .build();
    }

    private ResponseObject<PaymentInitiationResponse> buildSuccessResponseObjectWithLinksNullType() {
        PaymentInitiationResponse initiationResponse = new SinglePaymentInitiationResponse();
        Links links = new Links();
        initiationResponse.setLinks(links);
        return ResponseObject.<PaymentInitiationResponse>builder()
                   .body(initiationResponse)
                   .build();
    }

    private Cancellations buildCancellations(List<String> cancellationsList) {
        Cancellations cancellations = new Cancellations();
        CancellationList cancellationList = new CancellationList();
        cancellationList.addAll(cancellationsList);
        cancellations.setCancellationIds(cancellationList);
        return cancellations;
    }
}
