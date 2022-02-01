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


package de.adorsys.psd2.xs2a.service.payment.create;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.CreatePaymentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aToCmsPisCommonPaymentRequestMapper;
import de.adorsys.psd2.xs2a.service.payment.create.spi.CommonPaymentInitiationService;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCommonPaymentServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final PsuIdData PSU_DATA = new PsuIdData("aspsp", null, null, null, null);
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final byte[] PAYMENT_DATA_IN_BYTES = "payment_data".getBytes();
    private static final CommonPayment COMMON_PAYMENT = buildCommonPayment();
    private static final Xs2aPisCommonPayment PIS_COMMON_PAYMENT = new Xs2aPisCommonPayment(PAYMENT_ID, PSU_DATA);
    private static final PaymentInitiationParameters PARAM = buildPaymentInitiationParameters();
    private static final CreatePisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = new CreatePisCommonPaymentResponse(PAYMENT_ID, null);
    private static final PisPaymentInfo PAYMENT_INFO = buildPisPaymentInfoRequest();
    private static final Xs2aPisCommonPayment PIS_COMMON_PAYMENT_FAIL = new Xs2aPisCommonPayment(null, PSU_DATA);
    private static final PsuIdData WRONG_PSU_DATA = new PsuIdData("wrong_psu", null, null, null, null);
    private static final TppInfo WRONG_TPP_INFO = new TppInfo();
    private static final Xs2aCreatePisAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new Xs2aCreatePisAuthorisationResponse(null, null, null, null, null, null, null, null);
    private CommonPaymentInitiationResponse commonPaymentInitiationResponse;
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
    private static final String TEST_PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";

    @InjectMocks
    private CreateCommonPaymentService createCommonPaymentService;
    @Mock
    private CommonPaymentInitiationService commonPaymentInitiationService;
    @Mock
    private Xs2aPisCommonPaymentService pisCommonPaymentService;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private AspspDataService aspspDataService;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    @Mock
    private Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    @Mock
    private Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    @SuppressWarnings("unused") //mocks boolean value that returns false by default
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    @Mock
    private ScaApproachResolver scaApproachResolver;

    @BeforeEach
    void init() {
        InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider =
            new SpiAspspConsentDataProviderFactory(aspspDataService).getInitialAspspConsentDataProvider();
        commonPaymentInitiationResponse = buildCommonPaymentInitiationResponse(initialSpiAspspConsentDataProvider);
    }

    @Test
    void createPayment_success() {
        ArgumentCaptor<PisPaymentInfo> pisPaymentInfoArgumentCaptor = ArgumentCaptor.forClass(PisPaymentInfo.class);
        when(commonPaymentInitiationService.initiatePayment(any(CommonPayment.class), eq(PRODUCT), eq(PSU_DATA))).thenReturn(commonPaymentInitiationResponse);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PSU_DATA)).thenReturn(PIS_COMMON_PAYMENT);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(any(PisPaymentInfoCreationObject.class))).thenReturn(PAYMENT_INFO);
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        String contentTypeHeader = MediaType.APPLICATION_JSON_VALUE;
        when(requestProviderService.getContentTypeHeader()).thenReturn(contentTypeHeader);

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(PAYMENT_DATA_IN_BYTES, buildPaymentInitiationParameters(), TPP_INFO);

        //Then
        verify(pisCommonPaymentService).createCommonPayment(pisPaymentInfoArgumentCaptor.capture());
        PisPaymentInfo pisPaymentInfo = pisPaymentInfoArgumentCaptor.getValue();
        assertThat(pisPaymentInfo.getContentType()).isEqualTo(contentTypeHeader);
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
        assertThat(actualResponse.getBody().getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);
    }

    @Test
    void createPayment_wrongPsuData_fail() {
        // Given

        PaymentInitiationParameters param = buildPaymentInitiationParameters();
        param.setPsuData(WRONG_PSU_DATA);

        CommonPayment commonPayment = buildCommonPayment();
        commonPayment.setPsuDataList(Collections.singletonList(WRONG_PSU_DATA));
        when(commonPaymentInitiationService.initiatePayment(any(CommonPayment.class), eq(PRODUCT), eq(WRONG_PSU_DATA))).thenReturn(buildSpiErrorForCommonPayment());

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(PAYMENT_DATA_IN_BYTES, param, WRONG_TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }

    @Test
    void createPayment_emptyPaymentId_fail() {
        // Given
        when(commonPaymentInitiationService.initiatePayment(any(CommonPayment.class), eq(PRODUCT), eq(PSU_DATA))).thenReturn(commonPaymentInitiationResponse);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PSU_DATA))
            .thenReturn(PIS_COMMON_PAYMENT_FAIL);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(any(PisPaymentInfoCreationObject.class))).thenReturn(PAYMENT_INFO);
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);


        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(PAYMENT_DATA_IN_BYTES, PARAM, TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.PAYMENT_FAILED);
    }

    @Test
    void createPayment_authorisationMethodDecider_isImplicitMethod_success() {
        // Given
        when(commonPaymentInitiationService.initiatePayment(any(CommonPayment.class), eq(PRODUCT), eq(PSU_DATA))).thenReturn(commonPaymentInitiationResponse);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PSU_DATA)).thenReturn(PIS_COMMON_PAYMENT);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(any(PisPaymentInfoCreationObject.class))).thenReturn(PAYMENT_INFO);
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);

        when(authorisationMethodDecider.isImplicitMethod(false, false))
            .thenReturn(true);
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);

        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(any(), eq(PaymentType.SINGLE)))
            .thenReturn(Optional.of(CREATE_PIS_AUTHORISATION_RESPONSE));

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(PAYMENT_DATA_IN_BYTES, PARAM, TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody()).isEqualTo(commonPaymentInitiationResponse);
    }

    @Test
    void createPayment_pisScaAuthorisationService_createCommonPaymentAuthorisation_fail() {
        // Given
        when(commonPaymentInitiationService.initiatePayment(any(CommonPayment.class), eq(PRODUCT), eq(PSU_DATA))).thenReturn(commonPaymentInitiationResponse);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PSU_DATA)).thenReturn(PIS_COMMON_PAYMENT);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(any(PisPaymentInfoCreationObject.class))).thenReturn(PAYMENT_INFO);
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);

        when(authorisationMethodDecider.isImplicitMethod(false, false))
            .thenReturn(true);
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(any(), eq(PaymentType.SINGLE)))
            .thenReturn(Optional.empty());

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createCommonPaymentService.createPayment(PAYMENT_DATA_IN_BYTES, PARAM, TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.PAYMENT_FAILED);
    }


    private static CommonPayment buildCommonPayment() {
        CommonPayment request = new CommonPayment();
        request.setPaymentType(PaymentType.SINGLE);
        request.setPaymentProduct("sepa-credit-transfers");
        request.setPaymentData(PAYMENT_DATA_IN_BYTES);
        request.setPsuDataList(Collections.singletonList(PSU_DATA));

        return request;
    }

    private static PaymentInitiationParameters buildPaymentInitiationParameters() {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentProduct(PRODUCT);
        parameters.setPaymentType(PaymentType.SINGLE);
        parameters.setPsuData(PSU_DATA);
        return parameters;
    }

    private static CommonPaymentInitiationResponse buildCommonPaymentInitiationResponse(InitialSpiAspspConsentDataProvider initialSpiAspcpConsentDataProvider) {
        CommonPaymentInitiationResponse response = new CommonPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspConsentDataProvider(initialSpiAspcpConsentDataProvider);
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
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                      .build();

        return new CommonPaymentInitiationResponse(errorHolder);
    }

    private static PisPaymentInfo buildPisPaymentInfoRequest() {
        PisPaymentInfo pisPaymentInfo = new PisPaymentInfo();
        pisPaymentInfo.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return pisPaymentInfo;
    }
}
