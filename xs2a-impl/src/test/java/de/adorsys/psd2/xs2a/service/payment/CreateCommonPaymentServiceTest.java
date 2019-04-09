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

import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisCommonPaymentRequestMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.payment.sca.ScaPaymentService;
import de.adorsys.psd2.xs2a.service.payment.sca.ScaPaymentServiceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateCommonPaymentServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final PsuIdData PSU_DATA = new PsuIdData("aspsp", null, null, null);
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final CommonPayment COMMON_PAYMENT = buildCommonPayment();
    private static final Xs2aPisCommonPayment PIS_COMMON_PAYMENT = new Xs2aPisCommonPayment(PAYMENT_ID, PSU_DATA);
    private static final Xs2aPisCommonPayment PIS_COMMON_PAYMENT_FAIL = new Xs2aPisCommonPayment(null, PSU_DATA);
    private static final PaymentInitiationParameters PARAM = buildPaymentInitiationParameters();
    private static final CreatePisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = new CreatePisCommonPaymentResponse(PAYMENT_ID);
    private static final PisPaymentInfo PAYMENT_INFO = buildPisPaymentInfoRequest();
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final CommonPaymentInitiationResponse RESPONSE = buildCommonPaymentInitiationResponse();
    private static final List<String> ERROR_MESSAGE_TEXT = Arrays.asList("message 1", "message 2", "message 3");
    private static final PsuIdData WRONG_PSU_DATA = new PsuIdData("wrong_psu", null, null, null);
    private static final TppInfo WRONG_TPP_INFO = new TppInfo();
    private static final Xs2aCreatePisAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new Xs2aCreatePisAuthorisationResponse(null, null, null);


    @InjectMocks
    private CreateCommonPaymentService createCommonPaymentService;
    @Mock
    private ScaPaymentService scaPaymentService;
    @Mock
    private Xs2aPisCommonPaymentService pisCommonPaymentService;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    @Mock
    private Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    @Mock
    private Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    @Mock
    private ScaPaymentServiceResolver scaPaymentServiceResolver;

    @Before
    public void init() {
        when(scaPaymentService.createCommonPayment(COMMON_PAYMENT, TPP_INFO, PRODUCT, PSU_DATA)).thenReturn(RESPONSE);
        when(scaPaymentService.createCommonPayment(COMMON_PAYMENT, WRONG_TPP_INFO, PRODUCT, WRONG_PSU_DATA)).thenReturn(buildSpiErrorForCommonPayment());
        when(pisAspspDataService.getInternalPaymentIdByEncryptedString(anyString())).thenReturn(PAYMENT_ID);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PSU_DATA)).thenReturn(PIS_COMMON_PAYMENT);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(PARAM, TPP_INFO, RESPONSE, COMMON_PAYMENT.getPaymentData()))
            .thenReturn(PAYMENT_INFO);
        when(scaPaymentServiceResolver.getService())
            .thenReturn(scaPaymentService);
    }

    @Test
    public void createPayment_success() {
        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(COMMON_PAYMENT, buildPaymentInitiationParameters(), TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
    }

    @Test
    public void createPayment_wrongPsuData_fail() {
        // Given
        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");

        PaymentInitiationParameters param = buildPaymentInitiationParameters();
        param.setPsuData(WRONG_PSU_DATA);

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(COMMON_PAYMENT, param, WRONG_TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        assertThat(actualResponse.getError().getTppMessage().getText()).isEqualTo(errorMessagesString);
    }

    @Test
    public void createPayment_emptyPaymentId_fail() {
        // Given
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PSU_DATA))
            .thenReturn(PIS_COMMON_PAYMENT_FAIL);

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(COMMON_PAYMENT, PARAM, TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.PAYMENT_FAILED);
    }

    @Test
    public void createPayment_authorisationMethodDecider_isImplicitMethod_success() {
        // Given
        when(authorisationMethodDecider.isImplicitMethod(false, false))
            .thenReturn(true);
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PaymentType.SINGLE, PARAM.getPsuData()))
            .thenReturn(Optional.of(CREATE_PIS_AUTHORISATION_RESPONSE));

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(COMMON_PAYMENT, PARAM, TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody()).isEqualTo(RESPONSE);
    }

    @Test
    public void createPayment_pisScaAuthorisationService_createCommonPaymentAuthorisation_fail() {
        // Given
        when(authorisationMethodDecider.isImplicitMethod(false, false))
            .thenReturn(true);
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PaymentType.SINGLE, PARAM.getPsuData()))
            .thenReturn(Optional.empty());

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(COMMON_PAYMENT, PARAM, TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.PAYMENT_FAILED);
    }

    private static CommonPayment buildCommonPayment() {
        CommonPayment request = new CommonPayment();
        request.setPaymentType(PaymentType.SINGLE);
        request.setPaymentProduct("sepa-credit-transfers");
        request.setPaymentData(new byte[16]);
        request.setTppInfo(TPP_INFO);

        return request;
    }

    private static PaymentInitiationParameters buildPaymentInitiationParameters() {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentProduct(PRODUCT);
        parameters.setPaymentType(PaymentType.SINGLE);
        parameters.setPsuData(PSU_DATA);
        return parameters;
    }

    private static CommonPaymentInitiationResponse buildCommonPaymentInitiationResponse() {
        CommonPaymentInitiationResponse response = new CommonPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspConsentData(ASPSP_CONSENT_DATA);
        return response;
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static CommonPaymentInitiationResponse buildSpiErrorForCommonPayment() {
        ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
            .errorType(ErrorType.PIIS_400)
            .messages(ERROR_MESSAGE_TEXT)
            .build();

        return new CommonPaymentInitiationResponse(errorHolder);
    }

    private static PisPaymentInfo buildPisPaymentInfoRequest() {
        return new PisPaymentInfo();
    }
}
