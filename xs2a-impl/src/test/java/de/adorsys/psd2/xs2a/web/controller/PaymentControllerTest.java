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

package de.adorsys.psd2.xs2a.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppAttributes;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.*;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.header.PaymentCancellationHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.PaymentInitiationHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.*;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.function.Function;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_403;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_404;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    private static final String CORRECT_PAYMENT_ID = "33333-444444-55555-55555";
    private static final String CORRECT_PAYMENT_ID_2 = "33333-444444-55555-66666";
    private static final String WRONG_PAYMENT_ID = "wrong_payment_id";
    private static final String PSU_ID = "PSU ID";
    private static final String PSU_ID_TYPE = "PSU ID TYPE";
    private static final String PSU_CORPORATE_ID = "PSU CORPORATE ID";
    private static final String PSU_CORPORATE_ID_TYPE = "PSU CORPORATE ID TYPE";
    private static final String PSU_IP_ADDRESS = "PSU IP ADDRESS";
    private static final String REDIRECT_LINK = "http://localhost:4200/consent/confirmation/pis";
    private static final String TPP_NOTIFICATION_URI = "http://localhost/notifications";
    private static final String TPP_NOTIFICATION_CONTENT_PREFERRED = "status=SCA";
    private static final List<NotificationSupportedMode> TPP_NOTIFICATION_MODES = Collections.singletonList(NotificationSupportedMode.SCA);
    private static final TppNotificationData TPP_NOTIFICATION_DATA = new TppNotificationData(TPP_NOTIFICATION_MODES, TPP_NOTIFICATION_URI);
    private static final UUID REQUEST_ID = UUID.fromString("ddd36e05-d67a-4830-93ad-9462f71ae1e6");
    private static final String AUTHORISATION_ID = "3e96e9e0-9974-42aa-beb8-003e91416652";
    private static final String CANCELLATION_AUTHORISATION_ID = "d7ba791c-2231-4ed5-8232-cb1ad4cf7332";
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final String CORRECT_PAYMENT_SERVICE = "payments";
    private static final Object XML_SCT = new Object();
    private static final PeriodicPaymentInitiationXmlPart2StandingorderTypeJson JSON_STANDING_ORDER_TYPE = new PeriodicPaymentInitiationXmlPart2StandingorderTypeJson();
    private static final MessageError PIS_400_MESSAGE_ERROR = new MessageError(ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final MessageError PIS_404_MESSAGE_ERROR = new MessageError(PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404));
    private static final boolean TPP_REDIRECT_PREFERRED_TRUE = true;
    private static final byte[] PAYMENT_OBJECT = "payment object".getBytes();
    private static final ResponseHeaders RESPONSE_HEADERS = ResponseHeaders.builder().aspspScaApproach(ScaApproach.REDIRECT).build();
    private static final boolean EXPLICIT_PREFERRED_FALSE = false;
    private static final String PSU_DATA_PASSWORD_JSON_PATH = "json/web/controller/psuData-password.json";
    private static final String PSU_MESSAGE = "PSU message";
    private static final String SCA_APPROACH_HEADER = "Aspsp-Sca-Approach";
    private static final String DECOUPLED_APPROACH_VALUE = "DECOUPLED";

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private PaymentModelMapperPsd2 paymentModelMapperPsd2;
    @Mock
    private PaymentModelMapperXs2a paymentModelMapperXs2a;
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
    @Mock
    private NotificationSupportedModeService notificationSupportedModeService;
    @Mock
    private PaymentServiceForAuthorisationImpl paymentServiceForAuthorisation;
    @Mock
    private PaymentCancellationServiceForAuthorisationImpl paymentCancellationServiceForAuthorisation;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private PisPaymentCancellationRequest pisPaymentCancellationRequest;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void getPaymentById() {
        // Given
        when(xs2aPaymentService.getPaymentById(SINGLE, PRODUCT, CORRECT_PAYMENT_ID))
            .thenReturn(ResponseObject.<CommonPayment>builder().body(getXs2aPayment()).build());

        doReturn(new ResponseEntity<>(getPaymentInitiationResponse(), OK))
            .when(responseMapper).ok(any(), any());

        Object expectedBody = getPaymentInitiationResponse();

        // When
        ResponseEntity<?> response = paymentController.getPaymentInformation(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
                                                                             REQUEST_ID, null, null, null, null, null,
                                                                             null, null, null, null, null,
                                                                             null, null, null);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualToComparingFieldByField(expectedBody);
    }

    @Test
    void getPaymentById_Failure() {
        //Given
        MessageError messageError = createMessageError(PIS_404, RESOURCE_UNKNOWN_404);
        when(xs2aPaymentService.getPaymentById(SINGLE, PRODUCT, WRONG_PAYMENT_ID)).thenReturn(ResponseObject.<CommonPayment>builder().fail(messageError).build());
        when(responseErrorMapper.generateErrorResponse(messageError)).thenReturn(ResponseEntity.status(FORBIDDEN).build());
        // When
        ResponseEntity<?> response = paymentController.getPaymentInformation(CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                             WRONG_PAYMENT_ID, null, null, null, null, null,
                                                                             null, null, null, null, null,
                                                                             null, null, null, null);
        // Then
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    private PaymentInitiationStatusResponse200Json getPaymentInitiationResponse() {
        PaymentInitiationStatusResponse200Json response = new PaymentInitiationStatusResponse200Json();
        response.setTransactionStatus(de.adorsys.psd2.model.TransactionStatus.ACCP);
        return response;
    }

    private CommonPayment getXs2aPayment() {
        SinglePayment payment = new SinglePayment();
        payment.setEndToEndIdentification(CORRECT_PAYMENT_ID);
        return payment;
    }

    @Test
    void getPaymentInitiationStatus_withJsonContentType_success() {
        // Given
        doReturn(new ResponseEntity<>(getPaymentInitiationStatus(), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        when(xs2aPaymentService.getPaymentStatusById(SINGLE, PRODUCT, CORRECT_PAYMENT_ID))
            .thenReturn(ResponseObject.<GetPaymentStatusResponse>builder()
                            .body(new GetPaymentStatusResponse(TransactionStatus.ACCP, null, MediaType.APPLICATION_JSON, null, PSU_MESSAGE, null, null))
                            .build());

        PaymentInitiationStatusResponse200Json expectedBody = getPaymentInitiationStatus();

        // When
        //noinspection unchecked
        ResponseEntity<PaymentInitiationStatusResponse200Json> actualResponse =
            (ResponseEntity<PaymentInitiationStatusResponse200Json>) paymentController.getPaymentInitiationStatus(
                CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null);

        // Then
        HttpStatus actualHttpStatus = actualResponse.getStatusCode();
        assertThat(actualHttpStatus).isEqualTo(OK);
        assertThat(actualResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    void getPaymentInitiationStatus_withCustomContentType_success() {
        // Given
        byte[] rawPaymentStatus = "some raw value".getBytes();
        doReturn(new ResponseEntity<>(rawPaymentStatus, HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        when(xs2aPaymentService.getPaymentStatusById(SINGLE, PRODUCT, CORRECT_PAYMENT_ID))
            .thenReturn(ResponseObject.<GetPaymentStatusResponse>builder()
                            .body(new GetPaymentStatusResponse(TransactionStatus.ACCP, null, MediaType.APPLICATION_XML, rawPaymentStatus, PSU_MESSAGE, null, null))
                            .build());

        // When
        ResponseEntity<?> actualResponse = paymentController.getPaymentInitiationStatus(
            PaymentType.SINGLE.getValue(), PRODUCT, CORRECT_PAYMENT_ID, null, null,
            null, null, null, null, null,
            null, null, null, null, null,
            null, null);

        // Then
        HttpStatus actualHttpStatus = actualResponse.getStatusCode();
        assertThat(actualHttpStatus).isEqualTo(OK);
        assertThat(actualResponse.getBody()).isEqualTo(rawPaymentStatus);
    }

    @Test
    void getPaymentInitiationStatus_WrongId() {
        // Given
        when(responseErrorMapper.generateErrorResponse(createMessageError(PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());
        when(xs2aPaymentService.getPaymentStatusById(SINGLE, PRODUCT, WRONG_PAYMENT_ID)).thenReturn(ResponseObject.<GetPaymentStatusResponse>builder().fail(createMessageError(PIS_403, RESOURCE_UNKNOWN_403)).build());

        // When
        //noinspection unchecked
        ResponseEntity<PaymentInitiationStatusResponse200Json> actualResponse =
            (ResponseEntity<PaymentInitiationStatusResponse200Json>) paymentController.getPaymentInitiationStatus(
                CORRECT_PAYMENT_SERVICE, PRODUCT, WRONG_PAYMENT_ID, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null);

        // Then
        assertThat(actualResponse.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    void cancelPayment_WithoutAuthorisation_Success() {
        PisPaymentCancellationRequest paymentCancellationRequest = new PisPaymentCancellationRequest(SINGLE, PRODUCT, CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null);
        when(paymentModelMapperPsd2.mapToPaymentCancellationRequest(PRODUCT, CORRECT_PAYMENT_SERVICE, CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null, null))
            .thenReturn(paymentCancellationRequest);
        when(xs2aPaymentService.cancelPayment(paymentCancellationRequest)).thenReturn(getCancelPaymentResponseObject(false));

        // Given
        PaymentInitiationCancelResponse202 response = getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.CANC);
        ResponseEntity<PaymentInitiationCancelResponse202> expectedResult = new ResponseEntity<>(response, NO_CONTENT);

        when(paymentModelMapperPsd2.mapToPaymentInitiationCancelResponse(any())).thenReturn(response);
        when(responseMapper.delete(any())).thenReturn(expectedResult);

        // When
        //noinspection unchecked
        ResponseEntity<PaymentInitiationCancelResponse202> actualResult = (ResponseEntity<PaymentInitiationCancelResponse202>) paymentController.cancelPayment(CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                                                                                               CORRECT_PAYMENT_ID, null, null, null,
                                                                                                                                                               null, null, null, null, null, EXPLICIT_PREFERRED_FALSE, null,
                                                                                                                                                               null, null, null,
                                                                                                                                                               null, null, null,
                                                                                                                                                               null, null, null);

        // Then
        assertThat(actualResult.getStatusCode()).isEqualTo(NO_CONTENT);
        assertThat(actualResult.getBody()).isEqualTo(response);
    }

    @Test
    void cancelPayment_WithAuthorisation_Success() {
        PisPaymentCancellationRequest paymentCancellationRequest = new PisPaymentCancellationRequest(SINGLE, PRODUCT, CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(SCA_APPROACH_HEADER, Collections.singletonList(DECOUPLED_APPROACH_VALUE));

        ResponseEntity<PaymentInitiationCancelResponse202> response = new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.ACTC), headers, ACCEPTED);

        when(responseMapper.accepted(any(), any()))
            .thenReturn(response);
        when(paymentModelMapperPsd2.mapToPaymentCancellationRequest(PRODUCT, CORRECT_PAYMENT_SERVICE, CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null, null))
            .thenReturn(paymentCancellationRequest);
        when(xs2aPaymentService.cancelPayment(paymentCancellationRequest)).thenReturn(getCancelPaymentResponseObject(true));
        when(paymentCancellationHeadersBuilder.buildCancelPaymentHeaders(AUTHORISATION_ID))
            .thenReturn(ResponseHeaders.builder().aspspScaApproach(ScaApproach.DECOUPLED).build());

        // Given
        ResponseEntity<PaymentInitiationCancelResponse202> expectedResult = new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.ACTC), HttpStatus.ACCEPTED);

        // When
        //noinspection unchecked
        ResponseEntity<PaymentInitiationCancelResponse202> actualResult =
            (ResponseEntity<PaymentInitiationCancelResponse202>) paymentController.cancelPayment(CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                                 CORRECT_PAYMENT_ID, null, null, null,
                                                                                                 null, null, null, null, null, EXPLICIT_PREFERRED_FALSE, null,
                                                                                                 null, null, null,
                                                                                                 null, null, null,
                                                                                                 null, null, null);

        // Then
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
        assertTrue(actualResult.getHeaders().containsKey(SCA_APPROACH_HEADER));
        assertThat(actualResult.getHeaders().get(SCA_APPROACH_HEADER)).isEqualTo(Collections.singletonList(DECOUPLED_APPROACH_VALUE));
    }

    @Test
    void cancelPayment_WithoutAuthorisation_Fail_FinalisedStatus() {
        ResponseObject<CancelPaymentResponse> cancelPaymentResponse = getErrorOnPaymentCancellation404();
        ResponseEntity<?> expectedResult = ResponseEntity.status(NOT_FOUND).build();
        when(paymentModelMapperPsd2.mapToPaymentCancellationRequest(PRODUCT, CORRECT_PAYMENT_SERVICE, CORRECT_PAYMENT_ID, EXPLICIT_PREFERRED_FALSE, null, null))
            .thenReturn(pisPaymentCancellationRequest);
        when(responseErrorMapper.generateErrorResponse(cancelPaymentResponse.getError())).thenReturn(expectedResult);
        when(xs2aPaymentService.cancelPayment(pisPaymentCancellationRequest))
            .thenReturn(ResponseObject.<CancelPaymentResponse>builder().fail(cancelPaymentResponse.getError()).build());
        // Given
        ResponseEntity<?> actualResult = paymentController.cancelPayment(CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                         CORRECT_PAYMENT_ID, REQUEST_ID, null, null,
                                                                         null, null, null, null, null, EXPLICIT_PREFERRED_FALSE, null,
                                                                         null, null, null,
                                                                         null, null, null,
                                                                         null, null, null);
        // Then
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
    }

    @Test
    void cancelPayment_WithAuthorisation_Fail_FinalisedStatus() {
        // Given
        PisPaymentCancellationRequest paymentCancellationRequest = new PisPaymentCancellationRequest(SINGLE, PRODUCT, CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null);

        when(paymentModelMapperPsd2.mapToPaymentCancellationRequest(PRODUCT, CORRECT_PAYMENT_SERVICE, CORRECT_PAYMENT_ID, BooleanUtils.isTrue(EXPLICIT_PREFERRED_FALSE), null, null))
            .thenReturn(paymentCancellationRequest);
        when(xs2aPaymentService.cancelPayment(paymentCancellationRequest)).thenReturn(getErrorOnPaymentCancellation());
        when(responseErrorMapper.generateErrorResponse(createMessageError(ErrorType.PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());

        ResponseEntity<PaymentInitiationCancelResponse202> expectedResult = ResponseEntity.status(FORBIDDEN).build();
        // When
        ResponseEntity<?> actualResult = paymentController.cancelPayment(CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                         CORRECT_PAYMENT_ID, REQUEST_ID, null, null,
                                                                         null, null, null, null, null, EXPLICIT_PREFERRED_FALSE, null,
                                                                         null, null, null,
                                                                         null, null, null,
                                                                         null, null, null);

        // Then
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
    }

    @Test
    void getPaymentInitiationScaStatus_success() {
        // Given
        Xs2aScaStatusResponse xs2aScaStatusResponse = new Xs2aScaStatusResponse(de.adorsys.psd2.xs2a.core.sca.ScaStatus.RECEIVED, true, "psu message", null, null);
        ResponseObject<Xs2aScaStatusResponse> xs2aScaStatusResponseResponseObject = ResponseObject.<Xs2aScaStatusResponse>builder()
                                                                                        .body(xs2aScaStatusResponse)
                                                                                        .build();

        when(paymentServiceForAuthorisation.getAuthorisationScaStatus(any(String.class), any(String.class), any(PaymentType.class), any(String.class)))
            .thenReturn(xs2aScaStatusResponseResponseObject);
        doReturn(ResponseEntity.ok(buildScaStatusResponse()))
            .when(responseMapper).ok(eq(xs2aScaStatusResponseResponseObject), any());

        ScaStatusResponse expected = buildScaStatusResponse();

        // When
        ResponseEntity<?> actual = paymentController.getPaymentInitiationScaStatus(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
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
    void getPaymentInitiationScaStatus_failure() {
        // Given
        when(paymentServiceForAuthorisation.getAuthorisationScaStatus(WRONG_PAYMENT_ID, AUTHORISATION_ID, SINGLE, PRODUCT))
            .thenReturn(buildScaStatusError());
        when(responseErrorMapper.generateErrorResponse(createMessageError(PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());

        // When
        ResponseEntity<?> actual = paymentController.getPaymentInitiationScaStatus(CORRECT_PAYMENT_SERVICE, PRODUCT, WRONG_PAYMENT_ID,
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
    void getPaymentCancellationScaStatus_success() {
        // Given
        Xs2aScaStatusResponse xs2aScaStatusResponse = new Xs2aScaStatusResponse(de.adorsys.psd2.xs2a.core.sca.ScaStatus.RECEIVED, true, "psu message", null, null);
        ResponseObject<Xs2aScaStatusResponse> xs2aScaStatusResponseResponseObject = ResponseObject.<Xs2aScaStatusResponse>builder()
                                                                                        .body(xs2aScaStatusResponse)
                                                                                        .build();

        when(paymentCancellationServiceForAuthorisation.getAuthorisationScaStatus(any(String.class), any(String.class), any(PaymentType.class), any(String.class)))
            .thenReturn(xs2aScaStatusResponseResponseObject);
        doReturn(ResponseEntity.ok(buildScaStatusResponse()))
            .when(responseMapper).ok(eq(xs2aScaStatusResponseResponseObject), any());

        ScaStatusResponse expected = buildScaStatusResponse();

        // When
        ResponseEntity<?> actual = paymentController.getPaymentCancellationScaStatus(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
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
    void getPaymentCancellationScaStatus_failure() {
        // Given
        when(paymentCancellationServiceForAuthorisation.getAuthorisationScaStatus(WRONG_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, SINGLE, PRODUCT))
            .thenReturn(buildScaStatusError());
        when(responseErrorMapper.generateErrorResponse(createMessageError(PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());

        // When
        ResponseEntity<?> actual = paymentController.getPaymentCancellationScaStatus(CORRECT_PAYMENT_SERVICE, PRODUCT, WRONG_PAYMENT_ID,
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
    void getPaymentInitiationCancellationAuthorisationInformationClassCheck_success() {
        // Given
        List<String> cancellationsList = Collections.singletonList(CORRECT_PAYMENT_ID);
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseList(cancellationsList);
        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_ID, SINGLE, PRODUCT))
            .thenReturn(cancellationResponseList);

        ResponseEntity<Authorisations> authorisationsResponseEntity = ResponseEntity.ok(buildAuthorisations(cancellationsList));
        doReturn(authorisationsResponseEntity).when(responseMapper).ok(eq(cancellationResponseList), any());

        // When
        ResponseEntity<?> actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertTrue(actual.getBody() instanceof Authorisations);
    }

    @Test
    void getPaymentInitiationCancellationAuthorisationInformation_MethodMapperCheck() {
        // noinspection unchecked
        ArgumentCaptor<Function<Xs2aPaymentCancellationAuthorisationSubResource, Authorisations>> argumentCaptor = ArgumentCaptor.forClass(Function.class);

        List<String> cancellationsList = Collections.singletonList(CORRECT_PAYMENT_ID);
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseList(cancellationsList);
        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_ID, SINGLE, PRODUCT))
            .thenReturn(cancellationResponseList);

        ResponseEntity<Authorisations> authorisationsResponseEntity = ResponseEntity.ok(buildAuthorisations(cancellationsList));
        doReturn(authorisationsResponseEntity).when(responseMapper).ok(eq(cancellationResponseList), argumentCaptor.capture());

        // When
        paymentController.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null,
                                                                                   null, null);

        // Then
        argumentCaptor.getValue().apply(null);
        verify(consentModelMapper).mapToAuthorisations(any());
    }

    @Test
    void getPaymentInitiationCancellationAuthorisationInformation_success() {
        // Given
        List<String> cancellationsList = Collections.singletonList(CORRECT_PAYMENT_ID);
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseList(cancellationsList);

        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_ID, SINGLE, PRODUCT))
            .thenReturn(cancellationResponseList);

        ResponseEntity<?> authorisationsResponseEntity = ResponseEntity.ok(buildAuthorisations(cancellationsList));
        doReturn(authorisationsResponseEntity).when(responseMapper).ok(eq(cancellationResponseList), any());

        // When
        ResponseEntity<?> actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        Authorisations actualBody = (Authorisations) actual.getBody();
        assertThat(actualBody).isNotNull();
        assertThat(actualBody.getAuthorisationIds().size()).isEqualTo(1);
    }

    @Test
    void getPaymentInitiationCancellationAuthorisationInformationManyIds_success() {
        // Given
        List<String> cancellationsList = Arrays.asList(CORRECT_PAYMENT_ID, CORRECT_PAYMENT_ID_2);
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseList(cancellationsList);

        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_ID, SINGLE, PRODUCT))
            .thenReturn(cancellationResponseList);

        ResponseEntity<?> authorisationsResponseEntity = ResponseEntity.ok(buildAuthorisations(cancellationsList));
        doReturn(authorisationsResponseEntity).when(responseMapper).ok(eq(cancellationResponseList), any());

        // When
        ResponseEntity<?> actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null);

        // Then
        Authorisations actualBody = (Authorisations) actual.getBody();
        assertThat(actualBody).isNotNull();
        assertThat(actualBody.getAuthorisationIds().size()).isEqualTo(2);
    }

    @Test
    void getPaymentInitiationCancellationAuthorisationInformationWithNull_success() {
        // Given
        List<String> cancellationsList = new ArrayList<>();
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> cancellationResponseList = getCancellationResponseNullList();
        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(anyString(), any(PaymentType.class), any(String.class)))
            .thenReturn(cancellationResponseList);

        ResponseEntity<?> authorisationsResponseEntity = ResponseEntity.ok(buildAuthorisations(cancellationsList));
        doReturn(authorisationsResponseEntity).when(responseMapper).ok(eq(cancellationResponseList), any());

        // When
        ResponseEntity<?> actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null,
                                                                                                              null, null);
        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        Authorisations authorisations = (Authorisations) actual.getBody();
        assertThat(authorisations).isNotNull();
        assertTrue(CollectionUtils.isEmpty(authorisations.getAuthorisationIds()));
    }

    @Test
    void getPaymentInitiationCancellationAuthorisationInformation_error() {
        // Given
        when(responseErrorMapper.generateErrorResponse(any()))
            .thenReturn(ResponseEntity.status(FORBIDDEN).build());

        when(paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(anyString(), any(PaymentType.class), any(String.class)))
            .thenReturn(getCancellationResponseWithError());

        // When
        ResponseEntity<?> actual = paymentController.getPaymentInitiationCancellationAuthorisationInformation(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
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
    void initiatePayment_Failure_ErrorInServiceResponse() {
        // Given
        when(notificationSupportedModeService.getTppNotificationData(any(), any()))
            .thenReturn(new TppNotificationData(Collections.singletonList(NotificationSupportedMode.SCA), TPP_NOTIFICATION_URI));
        TppAttributes tppAttributes = new TppAttributes(null, REDIRECT_LINK, REDIRECT_LINK, true, TPP_NOTIFICATION_DATA, null);
        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, tppAttributes, buildPsuIdData(), null))
            .thenReturn(paymentInitiationParameters);

        String rawRequestObject = "some body";

        when(xs2aPaymentService.createPayment(rawRequestObject.getBytes(), paymentInitiationParameters))
            .thenReturn(buildFailResponseObject());

        when(responseErrorMapper.generateErrorResponse(PIS_400_MESSAGE_ERROR))
            .thenReturn(new ResponseEntity<>(PIS_400_MESSAGE_ERROR, BAD_REQUEST));


        // When
        ResponseEntity<?> actual = paymentController.initiatePayment(rawRequestObject, REQUEST_ID, PSU_IP_ADDRESS, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                     null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                     null, TPP_REDIRECT_PREFERRED_TRUE, null, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                     null, TPP_NOTIFICATION_URI, TPP_NOTIFICATION_CONTENT_PREFERRED, null, null, null, null,
                                                                     null, null, null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(actual.getBody()).isEqualTo(PIS_400_MESSAGE_ERROR);
    }

    @Test
    void initiatePaymentForRaw_Success() {
        // Given
        when(notificationSupportedModeService.getTppNotificationData(any(), any()))
            .thenReturn(new TppNotificationData(Collections.singletonList(NotificationSupportedMode.SCA), TPP_NOTIFICATION_URI));
        TppAttributes tppAttributes = new TppAttributes(null, REDIRECT_LINK, REDIRECT_LINK, true, TPP_NOTIFICATION_DATA, null);
        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, tppAttributes, buildPsuIdData(), null))
            .thenReturn(paymentInitiationParameters);

        String rawRequestObject = "some body";

        when(xs2aPaymentService.createPayment(rawRequestObject.getBytes(), paymentInitiationParameters))
            .thenReturn(buildSuccessResponseObjectWithLinks());

        ResponseObject<?> expectedResponseObject = buildSuccessResponseObjectWithLinks();

        // noinspection unchecked
        when(responseMapper.created(any(ResponseObject.class), eq(RESPONSE_HEADERS)))
            .thenReturn(new ResponseEntity<>(expectedResponseObject, CREATED));

        when(paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(any(), any(), any())).thenReturn(RESPONSE_HEADERS);

        // When
        ResponseEntity<?> actual = paymentController.initiatePayment(rawRequestObject, REQUEST_ID, PSU_IP_ADDRESS, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                     null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                     null, TPP_REDIRECT_PREFERRED_TRUE, null, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                     null, TPP_NOTIFICATION_URI, TPP_NOTIFICATION_CONTENT_PREFERRED, null, null, null, null,
                                                                     null, null, null, null, null);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);
        assertThat(actual.getBody()).isEqualTo(expectedResponseObject);
    }

    @Test
    void initiatePayment_Failure_CreatePaymentHasError() {
        // Given
        when(notificationSupportedModeService.getTppNotificationData(any(), any()))
            .thenReturn(new TppNotificationData(Collections.singletonList(NotificationSupportedMode.SCA), TPP_NOTIFICATION_URI));
        TppAttributes tppAttributes = new TppAttributes(null, REDIRECT_LINK, REDIRECT_LINK, true, TPP_NOTIFICATION_DATA, null);

        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, tppAttributes, buildPsuIdData(), null))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aPayment())
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildFailResponseObject());

        when(responseErrorMapper.generateErrorResponse(PIS_400_MESSAGE_ERROR))
            .thenReturn(new ResponseEntity<>(BAD_REQUEST));

        Object jsonRequestObject = new Object();

        // When
        ResponseEntity<?> actual = paymentController.initiatePayment(jsonRequestObject, REQUEST_ID, PSU_IP_ADDRESS, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                     null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                     null, TPP_REDIRECT_PREFERRED_TRUE, null, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                     null, TPP_NOTIFICATION_URI, TPP_NOTIFICATION_CONTENT_PREFERRED, null, null, null, null,
                                                                     null, null, null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    void initiatePayment_Success() {
        // Given
        when(notificationSupportedModeService.getTppNotificationData(any(), any()))
            .thenReturn(new TppNotificationData(Collections.singletonList(NotificationSupportedMode.SCA), TPP_NOTIFICATION_URI));
        TppAttributes tppAttributes = new TppAttributes(null, REDIRECT_LINK, REDIRECT_LINK, true, TPP_NOTIFICATION_DATA, null);

        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, tppAttributes, buildPsuIdData(), null))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aPayment())
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildSuccessResponseObjectWithLinks());

        ResponseObject<?> expectedResponseObject = buildSuccessResponseObjectWithLinks();
        // noinspection unchecked
        when(responseMapper.created(any(ResponseObject.class), eq(RESPONSE_HEADERS)))
            .thenReturn(new ResponseEntity<>(expectedResponseObject, CREATED));

        when(paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(any(), any(), any())).thenReturn(RESPONSE_HEADERS);

        Object jsonRequestObject = new Object();

        // When
        ResponseEntity<?> actual = paymentController.initiatePayment(jsonRequestObject, REQUEST_ID, PSU_IP_ADDRESS, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                     null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                     null, TPP_REDIRECT_PREFERRED_TRUE, null, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                     null, TPP_NOTIFICATION_URI, TPP_NOTIFICATION_CONTENT_PREFERRED, null, null, null, null,
                                                                     null, null, null, null, null);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);
        assertThat(actual.getBody()).isEqualTo(expectedResponseObject);
    }

    @Test
    void initiatePayment_XML_Failure_CreatePaymentHasError() {
        // Given
        when(notificationSupportedModeService.getTppNotificationData(any(), any()))
            .thenReturn(new TppNotificationData(Collections.singletonList(NotificationSupportedMode.SCA), TPP_NOTIFICATION_URI));
        TppAttributes tppAttributes = new TppAttributes(null, REDIRECT_LINK, REDIRECT_LINK, true, TPP_NOTIFICATION_DATA, null);

        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, tppAttributes, buildPsuIdData(), null))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aRawPayment(paymentInitiationParameters, XML_SCT, JSON_STANDING_ORDER_TYPE))
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildFailResponseObject());

        when(responseErrorMapper.generateErrorResponse(PIS_400_MESSAGE_ERROR))
            .thenReturn(new ResponseEntity<>(BAD_REQUEST));

        // When
        ResponseEntity<?> actual = paymentController.initiatePayment(REQUEST_ID, PSU_IP_ADDRESS, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                     XML_SCT, JSON_STANDING_ORDER_TYPE, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                     null, TPP_REDIRECT_PREFERRED_TRUE, null, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                     null, TPP_NOTIFICATION_URI, TPP_NOTIFICATION_CONTENT_PREFERRED, null, null, null, null,
                                                                     null, null, null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    void initiatePayment_XML_Success() {
        // Given
        when(notificationSupportedModeService.getTppNotificationData(any(), any()))
            .thenReturn(new TppNotificationData(Collections.singletonList(NotificationSupportedMode.SCA), TPP_NOTIFICATION_URI));
        TppAttributes tppAttributes = new TppAttributes(null, REDIRECT_LINK, REDIRECT_LINK, true, TPP_NOTIFICATION_DATA, null);

        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, tppAttributes, buildPsuIdData(), null))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aRawPayment(paymentInitiationParameters, XML_SCT, JSON_STANDING_ORDER_TYPE))
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildSuccessResponseObjectWithLinks());

        ResponseObject<PaymentInitiationResponse> expectedResponseObject = buildSuccessResponseObjectWithLinks();

        when(paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(any(), any(), any())).thenReturn(RESPONSE_HEADERS);

        //noinspection unchecked
        when(responseMapper.created(any(ResponseObject.class), eq(RESPONSE_HEADERS)))
            .thenReturn(new ResponseEntity<>(expectedResponseObject, CREATED));

        // When
        ResponseEntity<?> actual = paymentController.initiatePayment(REQUEST_ID, PSU_IP_ADDRESS, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                     XML_SCT, JSON_STANDING_ORDER_TYPE, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                     null, TPP_REDIRECT_PREFERRED_TRUE, null, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                     null, TPP_NOTIFICATION_URI, TPP_NOTIFICATION_CONTENT_PREFERRED, null, null, null, null,
                                                                     null, null, null, null, null);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(CREATED);
        assertThat(actual.getBody()).isEqualTo(expectedResponseObject);
    }

    @Test
    void initiatePayment_XML_WithException() {
        when(notificationSupportedModeService.getTppNotificationData(any(), any()))
            .thenReturn(new TppNotificationData(Collections.singletonList(NotificationSupportedMode.SCA), TPP_NOTIFICATION_URI));
        TppAttributes tppAttributes = new TppAttributes(null, REDIRECT_LINK, REDIRECT_LINK, true, TPP_NOTIFICATION_DATA, null);

        when(paymentModelMapperPsd2.mapToPaymentRequestParameters(PRODUCT, CORRECT_PAYMENT_SERVICE, tppAttributes, buildPsuIdData(), null))
            .thenReturn(paymentInitiationParameters);

        when(paymentModelMapperXs2a.mapToXs2aRawPayment(paymentInitiationParameters, XML_SCT, JSON_STANDING_ORDER_TYPE))
            .thenReturn(PAYMENT_OBJECT);

        when(xs2aPaymentService.createPayment(PAYMENT_OBJECT, paymentInitiationParameters))
            .thenReturn(buildSuccessResponseObjectWithLinksNullType());

        assertThrows(IllegalArgumentException.class, () -> paymentController.initiatePayment(REQUEST_ID, PSU_IP_ADDRESS, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                             XML_SCT, JSON_STANDING_ORDER_TYPE, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                                             null, TPP_REDIRECT_PREFERRED_TRUE, null, REDIRECT_LINK, REDIRECT_LINK, true, null,
                                                                                             null, TPP_NOTIFICATION_URI, TPP_NOTIFICATION_CONTENT_PREFERRED, null, null, null, null,
                                                                                             null, null, null, null, null));
    }

    @Test
    void startPaymentInitiationCancellationAuthorisation() {
        // Given
        String password = "some password";
        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);

        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(CORRECT_PAYMENT_ID, psuIdData, PRODUCT, SINGLE, password);
        when(authorisationMapper.mapToXs2aCreatePisAuthorisationRequest(any(), anyString(), any(PaymentType.class), anyString(), any()))
            .thenReturn(request);

        ResponseObject<CancellationAuthorisationResponse> serviceResponse = ResponseObject.<CancellationAuthorisationResponse>builder()
                                                                                .body(new Xs2aUpdatePisCommonPaymentPsuDataResponse(de.adorsys.psd2.xs2a.core.sca.ScaStatus.PSUIDENTIFIED, CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, psuIdData, null))
                                                                                .build();
        when(paymentCancellationAuthorisationService.createPisCancellationAuthorisation(request))
            .thenReturn(serviceResponse);

        StartScaprocessResponse expectedResponse = new StartScaprocessResponse();
        ResponseEntity<StartScaprocessResponse> value = new ResponseEntity<>(expectedResponse, CREATED);
        doReturn(value).when(responseMapper).created(eq(serviceResponse), any(), eq(RESPONSE_HEADERS));

        when(paymentCancellationHeadersBuilder.buildStartAuthorisationHeaders(CANCELLATION_AUTHORISATION_ID)).thenReturn(RESPONSE_HEADERS);

        // When
        ResponseEntity<?> actual = paymentController.startPaymentInitiationCancellationAuthorisation(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                                     CORRECT_PAYMENT_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                                                     TPP_REDIRECT_PREFERRED_TRUE, null, REDIRECT_LINK, REDIRECT_LINK, null, null, null,
                                                                                                     null, null, null, null, null, null,
                                                                                                     null, null, null);

        // Then
        assertNotNull(actual);
        assertEquals(CREATED, actual.getStatusCode());
        assertEquals(expectedResponse, actual.getBody());

        verify(responseMapper).created(eq(serviceResponse), any(), eq(RESPONSE_HEADERS));
        verify(paymentCancellationHeadersBuilder).buildStartAuthorisationHeaders(CANCELLATION_AUTHORISATION_ID);

        verify(responseErrorMapper, never()).generateErrorResponse(any(), any());
    }

    @Test
    void startPaymentInitiationCancellationAuthorisation_withServiceError_shouldReturnError() {
        // Given
        String password = "some password";

        MessageError serviceError = new MessageError(PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404));

        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(CORRECT_PAYMENT_ID, psuIdData, PRODUCT, SINGLE, password);
        when(authorisationMapper.mapToXs2aCreatePisAuthorisationRequest(any(), anyString(), any(PaymentType.class), anyString(), any()))
            .thenReturn(request);
        ResponseObject<CancellationAuthorisationResponse> serviceResponse = ResponseObject.<CancellationAuthorisationResponse>builder()
                                                                                .fail(serviceError)
                                                                                .build();
        when(paymentCancellationAuthorisationService.createPisCancellationAuthorisation(request))
            .thenReturn(serviceResponse);

        Object errorResponse = new Object();
        when(responseErrorMapper.generateErrorResponse(serviceError))
            .thenReturn(new ResponseEntity<>(errorResponse, NOT_FOUND));

        // When
        ResponseEntity<?> actual = paymentController.startPaymentInitiationCancellationAuthorisation(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                                     CORRECT_PAYMENT_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                                                     TPP_REDIRECT_PREFERRED_TRUE, null, REDIRECT_LINK, REDIRECT_LINK, null, null, null,
                                                                                                     null, null, null, null, null, null,
                                                                                                     null, null, null);

        // Then
        assertNotNull(actual);
        assertEquals(NOT_FOUND, actual.getStatusCode());
        assertEquals(errorResponse, actual.getBody());

        verify(responseErrorMapper).generateErrorResponse(serviceError);

        verify(paymentCancellationHeadersBuilder, never()).buildStartAuthorisationHeaders(anyString());
        verify(responseMapper, never()).created(any(), any(), any());
    }

    @Test
    void updatePaymentCancellationPsuData() {
        // Given
        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);

        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        when(consentModelMapper.mapToPisUpdatePsuData(psuIdData, CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, SINGLE, PRODUCT, body))
            .thenReturn(request);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                        .body(new Xs2aUpdatePisCommonPaymentPsuDataResponse(de.adorsys.psd2.xs2a.core.sca.ScaStatus.PSUIDENTIFIED, CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, psuIdData, null))
                                                                                        .build();

        when(paymentCancellationAuthorisationService.updatePisCancellationPsuData(request))
            .thenReturn(serviceResponse);

        when(paymentCancellationHeadersBuilder.buildUpdatePsuDataHeaders(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(RESPONSE_HEADERS);

        UpdatePsuAuthenticationResponse expectedResponse = new UpdatePsuAuthenticationResponse();
        doReturn(new ResponseEntity<>(expectedResponse, OK)).when(responseMapper).ok(eq(serviceResponse), any(), eq(RESPONSE_HEADERS));

        // When
        ResponseEntity<?> actual = paymentController.updatePaymentCancellationPsuData(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                      CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                                      PSU_IP_ADDRESS, null, null, null,
                                                                                      null, null, null, null, null, null);

        // Then
        assertNotNull(actual);
        assertEquals(OK, actual.getStatusCode());
        assertEquals(expectedResponse, actual.getBody());

        verify(responseMapper).ok(eq(serviceResponse), any(), eq(RESPONSE_HEADERS));
        verify(paymentCancellationHeadersBuilder).buildUpdatePsuDataHeaders(CANCELLATION_AUTHORISATION_ID);

        verify(responseErrorMapper, never()).generateErrorResponse(any());
    }

    @Test
    void updatePaymentCancellationPsuData_withServiceError_shouldReturnError() {
        // Given
        MessageError serviceError = new MessageError(PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404));

        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        when(consentModelMapper.mapToPisUpdatePsuData(psuIdData, CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, SINGLE, PRODUCT, body))
            .thenReturn(request);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                        .fail(serviceError)
                                                                                        .build();

        when(paymentCancellationAuthorisationService.updatePisCancellationPsuData(request))
            .thenReturn(serviceResponse);

        Object errorResponse = new Object();
        when(responseErrorMapper.generateErrorResponse(serviceError))
            .thenReturn(new ResponseEntity<>(errorResponse, NOT_FOUND));

        // When
        ResponseEntity<?> actual = paymentController.updatePaymentCancellationPsuData(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                                      CORRECT_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                                      PSU_IP_ADDRESS, null, null, null,
                                                                                      null, null, null, null, null, null);


        // Then
        assertNotNull(actual);
        assertEquals(NOT_FOUND, actual.getStatusCode());
        assertEquals(errorResponse, actual.getBody());

        verify(responseErrorMapper).generateErrorResponse(serviceError);

        verify(paymentCancellationHeadersBuilder, never()).buildUpdatePsuDataHeaders(anyString());
        verify(responseMapper, never()).created(any(), any(), any());
    }

    @Test
    void updatePaymentPsuData() {
        // Given
        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);

        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        when(consentModelMapper.mapToPisUpdatePsuData(psuIdData, CORRECT_PAYMENT_ID, AUTHORISATION_ID, SINGLE, PRODUCT, body))
            .thenReturn(request);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                        .body(new Xs2aUpdatePisCommonPaymentPsuDataResponse(de.adorsys.psd2.xs2a.core.sca.ScaStatus.PSUIDENTIFIED, CORRECT_PAYMENT_ID, AUTHORISATION_ID, psuIdData, null))
                                                                                        .build();

        when(paymentAuthorisationService.updatePisCommonPaymentPsuData(request))
            .thenReturn(serviceResponse);

        when(paymentInitiationHeadersBuilder.buildUpdatePsuDataHeaders(AUTHORISATION_ID))
            .thenReturn(RESPONSE_HEADERS);

        UpdatePsuAuthenticationResponse expectedResponse = new UpdatePsuAuthenticationResponse();
        doReturn(new ResponseEntity<>(expectedResponse, OK))
            .when(responseMapper).ok(eq(serviceResponse), any(), eq(RESPONSE_HEADERS));

        // When
        ResponseEntity<?> actual = paymentController.updatePaymentPsuData(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                          CORRECT_PAYMENT_ID, AUTHORISATION_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                          PSU_IP_ADDRESS, null, null, null,
                                                                          null, null, null, null, null, null);

        // Then
        assertNotNull(actual);
        assertEquals(OK, actual.getStatusCode());
        assertEquals(expectedResponse, actual.getBody());

        verify(responseMapper).ok(eq(serviceResponse), any(), eq(RESPONSE_HEADERS));
        verify(paymentInitiationHeadersBuilder).buildUpdatePsuDataHeaders(AUTHORISATION_ID);

        verify(responseErrorMapper, never()).generateErrorResponse(any());
    }

    @Test
    void updatePaymentPsuData_withServiceError_shouldReturnError() {
        // Given
        MessageError serviceError = new MessageError(PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404));

        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        when(consentModelMapper.mapToPisUpdatePsuData(psuIdData, CORRECT_PAYMENT_ID, AUTHORISATION_ID, SINGLE, PRODUCT, body))
            .thenReturn(request);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                        .fail(serviceError)
                                                                                        .build();

        when(paymentAuthorisationService.updatePisCommonPaymentPsuData(request))
            .thenReturn(serviceResponse);

        Object errorResponse = new Object();
        when(responseErrorMapper.generateErrorResponse(serviceError))
            .thenReturn(new ResponseEntity<>(errorResponse, NOT_FOUND));

        // When
        ResponseEntity<?> actual = paymentController.updatePaymentPsuData(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT,
                                                                          CORRECT_PAYMENT_ID, AUTHORISATION_ID, body, null, null, null, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
                                                                          PSU_IP_ADDRESS, null, null, null,
                                                                          null, null, null, null, null, null);


        // Then
        assertNotNull(actual);
        assertEquals(NOT_FOUND, actual.getStatusCode());
        assertEquals(errorResponse, actual.getBody());

        verify(responseErrorMapper).generateErrorResponse(serviceError);

        verify(paymentInitiationHeadersBuilder, never()).buildUpdatePsuDataHeaders(anyString());
        verify(responseMapper, never()).created(any(), any(), any());
    }

    @Test
    void getPaymentInitiationAuthorisation_Success() {
        // Given
        ResponseObject<Xs2aAuthorisationSubResources> responseObject = ResponseObject.<Xs2aAuthorisationSubResources>builder()
                                                                           .body(new Xs2aAuthorisationSubResources(getAuthorisations()))
                                                                           .build();
        when(paymentAuthorisationService.getPaymentInitiationAuthorisations(CORRECT_PAYMENT_ID, PRODUCT, SINGLE)).thenReturn(responseObject);
        doReturn(ResponseEntity.ok(getAuthorisations()))
            .when(responseMapper).ok(eq(responseObject), any());

        // When
        ResponseEntity<?> actual = paymentController.getPaymentInitiationAuthorisation(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
                                                                                       REQUEST_ID, null, null,
                                                                                       null, null,
                                                                                       null, null, null,
                                                                                       null, null, null,
                                                                                       null, null, null);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody()).isEqualTo(getAuthorisations());
    }

    @Test
    void getPaymentInitiationAuthorisation_ErrorInServiceResponse() {
        // Given
        ResponseObject<Xs2aAuthorisationSubResources> responseObject = ResponseObject.<Xs2aAuthorisationSubResources>builder()
                                                                           .fail(PIS_404_MESSAGE_ERROR)
                                                                           .build();
        when(paymentAuthorisationService.getPaymentInitiationAuthorisations(CORRECT_PAYMENT_ID, PRODUCT, SINGLE)).thenReturn(responseObject);
        when(responseErrorMapper.generateErrorResponse(PIS_404_MESSAGE_ERROR))
            .thenReturn(new ResponseEntity<>(PIS_404_MESSAGE_ERROR, HttpStatus.NOT_FOUND));

        // When
        ResponseEntity<?> actual = paymentController.getPaymentInitiationAuthorisation(CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID,
                                                                                       REQUEST_ID, null, null,
                                                                                       null, null,
                                                                                       null, null, null,
                                                                                       null, null, null,
                                                                                       null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(NOT_FOUND);
        assertThat(actual.getBody()).isEqualTo(PIS_404_MESSAGE_ERROR);
    }

    @Test
    void startPaymentAuthorisation_Success() {
        // Given
        String password = "some password";
        Map<String, Map<String, String>> body = jsonReader.getObjectFromFile(PSU_DATA_PASSWORD_JSON_PATH, new TypeReference<Map<String, Map<String, String>>>() {
        });

        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);

        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(CORRECT_PAYMENT_ID, psuIdData, PRODUCT, SINGLE, password);
        when(authorisationMapper.mapToXs2aCreatePisAuthorisationRequest(any(), anyString(), any(PaymentType.class), anyString(), any()))
            .thenReturn(request);

        ResponseObject<AuthorisationResponse> serviceResponse = ResponseObject.<AuthorisationResponse>builder()
                                                                    .body(buildXs2aCreatePisAuthorisationResponse())
                                                                    .build();
        when(paymentAuthorisationService.createPisAuthorisation(request))
            .thenReturn(serviceResponse);

        StartScaprocessResponse expectedResponse = new StartScaprocessResponse();

        doReturn(new ResponseEntity<>(expectedResponse, CREATED)).when(responseMapper).created(any(), eq(RESPONSE_HEADERS));

        when(paymentInitiationHeadersBuilder.buildStartAuthorisationHeaders(AUTHORISATION_ID)).thenReturn(RESPONSE_HEADERS);

        // When
        ResponseEntity<?> actual = paymentController.startPaymentAuthorisation(REQUEST_ID, CORRECT_PAYMENT_SERVICE, PRODUCT, CORRECT_PAYMENT_ID, body,
                                                                               PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, TPP_REDIRECT_PREFERRED_TRUE, null,
                                                                               REDIRECT_LINK, null, null, null, null, null, null,
                                                                               null, null, null, null, null, null, null, null,
                                                                               null, null);

        // Then
        assertNotNull(actual);
        assertEquals(CREATED, actual.getStatusCode());
        assertEquals(expectedResponse, actual.getBody());

        verify(responseMapper).created(any(), eq(RESPONSE_HEADERS));
        verify(paymentInitiationHeadersBuilder).buildStartAuthorisationHeaders(AUTHORISATION_ID);

        verify(responseErrorMapper, never()).generateErrorResponse(any(), any());
    }

    private Xs2aCreatePisAuthorisationResponse buildXs2aCreatePisAuthorisationResponse() {
        return new Xs2aCreatePisAuthorisationResponse(AUTHORISATION_ID, de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED, SINGLE, REQUEST_ID.toString(), null);
    }

    private List<String> getAuthorisations() {
        return Collections.singletonList(AUTHORISATION_ID);
    }

    private PaymentInitiationStatusResponse200Json getPaymentInitiationStatus() {
        PaymentInitiationStatusResponse200Json response = new PaymentInitiationStatusResponse200Json();
        response.setTransactionStatus(de.adorsys.psd2.model.TransactionStatus.ACCP);
        return response;
    }

    private ResponseObject<CancelPaymentResponse> getCancelPaymentResponseObject(boolean startAuthorisationRequired) {
        CancelPaymentResponse response = new CancelPaymentResponse();
        response.setAuthorizationId(AUTHORISATION_ID);
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
                   .fail(ErrorType.PIS_403, of(RESOURCE_UNKNOWN_403))
                   .build();
    }

    private ResponseObject<CancelPaymentResponse> getErrorOnPaymentCancellation404() {
        return ResponseObject.<CancelPaymentResponse>builder()
                   .fail(ErrorType.PIS_404, of(RESOURCE_UNKNOWN_404))
                   .build();
    }

    private ScaStatusResponse buildScaStatusResponse() {
        ScaStatusResponse response = new ScaStatusResponse().scaStatus(de.adorsys.psd2.model.ScaStatus.RECEIVED);
        response.setTrustedBeneficiaryFlag(true);
        return response;
    }

    private ResponseObject<Xs2aScaStatusResponse> buildScaStatusError() {
        return ResponseObject.<Xs2aScaStatusResponse>builder()
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
        return new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS, buildEmptyAdditionalPsuIdData());
    }

    private AdditionalPsuIdData buildEmptyAdditionalPsuIdData() {
        return new AdditionalPsuIdData(null, null, null, null, null, null, null, null, null);
    }

    private <T> ResponseObject<T> buildFailResponseObject() {
        return ResponseObject.<T>builder()
                   .fail(PIS_400_MESSAGE_ERROR)
                   .build();
    }

    private ResponseObject<PaymentInitiationResponse> buildSuccessResponseObjectWithLinks() {
        PaymentInitiationResponse initiationResponse = new SinglePaymentInitiationResponse();
        initiationResponse.setTppNotificationContentPreferred(Collections.singletonList(NotificationSupportedMode.SCA));
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

    private Authorisations buildAuthorisations(List<String> authorisationIds) {
        Authorisations authorisations = new Authorisations();
        AuthorisationsList authorisationsList = new AuthorisationsList();
        authorisationsList.addAll(authorisationIds);
        authorisations.setAuthorisationIds(authorisationsList);
        return authorisations;
    }
}
