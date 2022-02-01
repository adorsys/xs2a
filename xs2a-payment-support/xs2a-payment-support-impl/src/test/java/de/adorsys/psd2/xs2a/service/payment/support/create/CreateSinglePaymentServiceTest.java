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


package de.adorsys.psd2.xs2a.service.payment.support.create;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
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
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aToCmsPisCommonPaymentRequestMapper;
import de.adorsys.psd2.xs2a.service.payment.create.PisPaymentInfoCreationObject;
import de.adorsys.psd2.xs2a.service.payment.support.create.spi.SinglePaymentInitiationService;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.RawToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSinglePaymentServiceTest {
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String IBAN = "DE123456789";
    private static final PsuIdData PSU_DATA = new PsuIdData("correct_psu", null, null, null, null);
    private static final PsuIdData WRONG_PSU_DATA = new PsuIdData("wrong_psu", null, null, null, null);
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final TppInfo WRONG_TPP_INFO = new TppInfo();
    private static final String DEB_ACCOUNT_ID = "11111_debtorAccount";
    private static final String CRED_ACCOUNT_ID = "2222_creditorAccount";
    private static final Xs2aPisCommonPayment PIS_COMMON_PAYMENT = new Xs2aPisCommonPayment(PAYMENT_ID, PSU_DATA);
    private static final PaymentInitiationParameters PARAM = buildPaymentInitiationParameters();
    private static final CreatePisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = new CreatePisCommonPaymentResponse(PAYMENT_ID, null);
    private static final PisPaymentInfo PAYMENT_INFO = buildPisPaymentInfoRequest();
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private final Xs2aPisCommonPayment PIS_COMMON_PAYMENT_FAIL = new Xs2aPisCommonPayment(null, PSU_DATA);
    private SinglePaymentInitiationResponse singlePaymentInitiationResponse;
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final byte[] PAYMENT_BODY = "some payment body".getBytes();
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
    private static final String TEST_PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";
    private static final Xs2aCreatePisAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new Xs2aCreatePisAuthorisationResponse(null, SCA_STATUS, null, null, null, null, null, null);

    @InjectMocks
    private CreateSinglePaymentService createSinglePaymentService;
    @Mock
    private SinglePaymentInitiationService singlePaymentInitiationService;
    @SuppressWarnings("unused") //mocks boolean value that returns false by default
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    @Mock
    private Xs2aPisCommonPaymentService pisCommonPaymentService;
    @Mock
    private Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    @Mock
    private AspspDataService aspspDataService;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private RawToXs2aPaymentMapper rawToXs2aPaymentMapper;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private LoggingContextService loggingContextService;

    @BeforeEach
    void init() {
        singlePaymentInitiationResponse = buildSinglePaymentInitiationResponse(new SpiAspspConsentDataProviderFactory(aspspDataService).getInitialAspspConsentDataProvider());
        when(rawToXs2aPaymentMapper.mapToSinglePayment(PAYMENT_BODY)).thenReturn(buildSinglePayment());
    }

    @Test
    void createPayment_success() {
        // Given
        when(singlePaymentInitiationService.initiatePayment(any(SinglePayment.class), eq(PAYMENT_PRODUCT), eq(PSU_DATA))).thenReturn(singlePaymentInitiationResponse);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PARAM.getPsuData())).thenReturn(PIS_COMMON_PAYMENT);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(any(PisPaymentInfoCreationObject.class))).thenReturn(PAYMENT_INFO);
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createSinglePaymentService.createPayment(PAYMENT_BODY, PARAM, TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
    }

    @Test
    void createPayment_success_checkSettingCreationTimestamp() {
        // Given
        when(singlePaymentInitiationService.initiatePayment(any(SinglePayment.class), eq(PAYMENT_PRODUCT), eq(PSU_DATA))).thenReturn(singlePaymentInitiationResponse);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PARAM.getPsuData())).thenReturn(PIS_COMMON_PAYMENT);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(any(PisPaymentInfoCreationObject.class))).thenReturn(PAYMENT_INFO);
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);

        //When
        ArgumentCaptor<SinglePayment> argumentCaptor = ArgumentCaptor.forClass(SinglePayment.class);
        createSinglePaymentService.createPayment(PAYMENT_BODY, PARAM, TPP_INFO);

        //Then
        verify(singlePaymentInitiationService).initiatePayment(argumentCaptor.capture(), eq(PAYMENT_PRODUCT), eq(PSU_DATA));
        SinglePayment singlePayment = argumentCaptor.getValue();
        assertNotNull(singlePayment.getCreationTimestamp());
    }

    @Test
    void createPayment_wrongPsuData_fail() {
        // Given
        when(singlePaymentInitiationService.initiatePayment(any(SinglePayment.class), eq(PAYMENT_PRODUCT), eq(WRONG_PSU_DATA))).thenReturn(buildSpiErrorForSinglePayment());

        PaymentInitiationParameters param = buildPaymentInitiationParameters();
        param.setPsuData(WRONG_PSU_DATA);

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createSinglePaymentService.createPayment(PAYMENT_BODY, param, WRONG_TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }

    @Test
    void createPayment_emptyPaymentId_fail() {
        // Given
        when(singlePaymentInitiationService.initiatePayment(any(SinglePayment.class), eq(PAYMENT_PRODUCT), eq(PSU_DATA))).thenReturn(singlePaymentInitiationResponse);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PARAM.getPsuData())).thenReturn(PIS_COMMON_PAYMENT);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(any(PisPaymentInfoCreationObject.class))).thenReturn(PAYMENT_INFO);
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);

        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PARAM.getPsuData()))
            .thenReturn(PIS_COMMON_PAYMENT_FAIL);

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createSinglePaymentService.createPayment(PAYMENT_BODY, PARAM, TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.PAYMENT_FAILED);
    }

    @Test
    void createPayment_pisScaAuthorisationService_createCommonPaymentAuthorisation_fail() {
        // Given
        when(singlePaymentInitiationService.initiatePayment(any(SinglePayment.class), eq(PAYMENT_PRODUCT), eq(PSU_DATA))).thenReturn(singlePaymentInitiationResponse);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PARAM.getPsuData())).thenReturn(PIS_COMMON_PAYMENT);
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
        ResponseObject<PaymentInitiationResponse> actualResponse = createSinglePaymentService.createPayment(PAYMENT_BODY, PARAM, TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.PAYMENT_FAILED);
    }

    @Test
    void createPayment_authorisationMethodDecider_isImplicitMethod_success() {
        // Given
        when(singlePaymentInitiationService.initiatePayment(any(SinglePayment.class), eq(PAYMENT_PRODUCT), eq(PSU_DATA))).thenReturn(singlePaymentInitiationResponse);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PARAM.getPsuData())).thenReturn(PIS_COMMON_PAYMENT);
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
        ResponseObject<PaymentInitiationResponse> actualResponse = createSinglePaymentService.createPayment(PAYMENT_BODY, buildPaymentInitiationParameters(), buildTppInfo());

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody()).isEqualTo(singlePaymentInitiationResponse);
    }


    private static SinglePayment buildSinglePayment() {
        SinglePayment payment = new SinglePayment();
        Xs2aAmount amount = buildXs2aAmount();
        payment.setPaymentId(PAYMENT_ID);
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(buildReference(DEB_ACCOUNT_ID));
        payment.setCreditorAccount(buildReference(CRED_ACCOUNT_ID));
        payment.setTransactionStatus(TransactionStatus.RCVD);
        payment.setPaymentProduct(PAYMENT_PRODUCT);
        return payment;
    }

    private static Xs2aAmount buildXs2aAmount() {
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(EUR_CURRENCY);
        amount.setAmount("100");
        return amount;
    }

    private static AccountReference buildReference(String accountId) {
        AccountReference reference = new AccountReference();
        reference.setIban(IBAN);
        reference.setCurrency(EUR_CURRENCY);
        reference.setAspspAccountId(accountId);
        return reference;
    }

    private static PaymentInitiationParameters buildPaymentInitiationParameters() {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentProduct(PAYMENT_PRODUCT);
        parameters.setPaymentType(PaymentType.SINGLE);
        parameters.setPsuData(PSU_DATA);
        return parameters;
    }

    private static SinglePaymentInitiationResponse buildSinglePaymentInitiationResponse(InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider) {
        SinglePaymentInitiationResponse response = new SinglePaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspConsentDataProvider(initialSpiAspspConsentDataProvider);
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

    private static SinglePaymentInitiationResponse buildSpiErrorForSinglePayment() {
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                      .build();

        return new SinglePaymentInitiationResponse(errorHolder);
    }

    private static PisPaymentInfo buildPisPaymentInfoRequest() {
        return new PisPaymentInfo();
    }
}
