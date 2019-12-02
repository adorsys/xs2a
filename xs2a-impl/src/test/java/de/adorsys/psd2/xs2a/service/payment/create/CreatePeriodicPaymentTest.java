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


package de.adorsys.psd2.xs2a.service.payment.create;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisCommonPaymentRequestMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.payment.create.spi.PeriodicPaymentInitiationService;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RCVD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreatePeriodicPaymentTest {
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String IBAN = "DE123456789";
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("correct_psu", null, null, null);
    private static final PsuIdData WRONG_PSU_DATA = new PsuIdData("wrong_psu", null, null, null);
    private static final TppInfo WRONG_TPP_INFO = new TppInfo();
    private static final Xs2aPisCommonPayment PIS_COMMON_PAYMENT = buildXs2aPisCommonPayment();
    private static final PaymentInitiationParameters PARAM = buildPaymentInitiationParameters();
    private static final CreatePisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = new CreatePisCommonPaymentResponse(PAYMENT_ID,null);
    private static final PisPaymentInfo PAYMENT_INFO = buildPisPaymentInfoRequest();
    private final Xs2aPisCommonPayment PIS_COMMON_PAYMENT_FAIL = new Xs2aPisCommonPayment(null, PSU_ID_DATA);
    private static final Xs2aCreatePisAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new Xs2aCreatePisAuthorisationResponse(null, null, null, null, null, null);
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private CreatePeriodicPaymentService createPeriodicPaymentService;
    @Mock
    private PeriodicPaymentInitiationService periodicPaymentInitiationService;
    @Mock
    private Xs2aPisCommonPaymentService pisCommonPaymentService;
    @Mock
    private Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    @Mock
    private Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    @Mock
    private AspspDataService aspspDataService;
    @SuppressWarnings("unused") //mocks boolean value that returns false by default
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;

    private PeriodicPaymentInitiationResponse periodicPaymentInitiationResponse;

    @Mock
    private PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private RequestProviderService requestProviderService;


    @Before
    public void setUp() {
        periodicPaymentInitiationResponse = buildPeriodicPaymentInitiationResponse(new SpiAspspConsentDataProviderFactory(aspspDataService).getInitialAspspConsentDataProvider());
        when(periodicPaymentInitiationService.initiatePayment(any(PeriodicPayment.class), eq("sepa-credit-transfers"), eq(PSU_ID_DATA))).thenReturn(periodicPaymentInitiationResponse);
        when(periodicPaymentInitiationService.initiatePayment(any(PeriodicPayment.class), eq("sepa-credit-transfers"), eq(WRONG_PSU_DATA))).thenReturn(buildSpiErrorForPeriodicPayment());
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO)).thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PARAM.getPsuData())).thenReturn(PIS_COMMON_PAYMENT);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(eq(PARAM), eq(TPP_INFO), eq(periodicPaymentInitiationResponse), eq(null), eq(INTERNAL_REQUEST_ID), any(OffsetDateTime.class)))
            .thenReturn(PAYMENT_INFO);
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
    }

    @Test
    public void success_initiate_periodic_payment() {
        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createPeriodicPaymentService.createPayment(buildPeriodicPayment(), buildPaymentInitiationParameters(), buildTppInfo());

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }


    @Test
    public void createPayment_success() {
        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createPeriodicPaymentService.createPayment(buildPeriodicPayment(), buildPaymentInitiationParameters(), buildTppInfo());

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void createPayment_wrongPsuData_fail() {
        // Given
        PaymentInitiationParameters param = buildPaymentInitiationParameters();
        param.setPsuData(WRONG_PSU_DATA);

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createPeriodicPaymentService.createPayment(buildPeriodicPayment(), param, WRONG_TPP_INFO);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }

    @Test
    public void createPayment_emptyPaymentId_fail() {
        // Given
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PARAM.getPsuData()))
            .thenReturn(PIS_COMMON_PAYMENT_FAIL);

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createPeriodicPaymentService.createPayment(buildPeriodicPayment(), buildPaymentInitiationParameters(), buildTppInfo());

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.PAYMENT_FAILED);
    }

    @Test
    public void createPayment_authorisationMethodDecider_isImplicitMethod_fail() {
        // Given
        when(authorisationMethodDecider.isImplicitMethod(false, false))
            .thenReturn(true);
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PaymentType.PERIODIC, PARAM.getPsuData()))
            .thenReturn(Optional.of(CREATE_PIS_AUTHORISATION_RESPONSE));

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createPeriodicPaymentService.createPayment(buildPeriodicPayment(), buildPaymentInitiationParameters(), buildTppInfo());

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
    }

    @Test
    public void createPayment_pisScaAuthorisationService_createCommonPaymentAuthorisation_fail() {
        // Given
        when(authorisationMethodDecider.isImplicitMethod(false, false))
            .thenReturn(true);
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PaymentType.PERIODIC, PARAM.getPsuData()))
            .thenReturn(Optional.empty());

        //When
        ResponseObject<PaymentInitiationResponse> actualResponse = createPeriodicPaymentService.createPayment(buildPeriodicPayment(), buildPaymentInitiationParameters(), buildTppInfo());

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.PAYMENT_FAILED);
    }

    private static PeriodicPayment buildPeriodicPayment() {
        PeriodicPayment payment = new PeriodicPayment();
        Xs2aAmount amount = buildXs2aAmount();
        payment.setPaymentId(PAYMENT_ID);
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(buildReference());
        payment.setCreditorAccount(buildReference());
        payment.setStartDate(LocalDate.now());
        payment.setEndDate(LocalDate.now().plusMonths(4));
        payment.setTransactionStatus(TransactionStatus.RCVD);
        return payment;
    }

    private static Xs2aAmount buildXs2aAmount() {
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(EUR_CURRENCY);
        amount.setAmount("100");
        return amount;
    }

    private static AccountReference buildReference() {
        AccountReference reference = new AccountReference();
        reference.setIban(IBAN);
        reference.setCurrency(EUR_CURRENCY);
        return reference;
    }

    private static Xs2aPisCommonPayment buildXs2aPisCommonPayment() {
        return new Xs2aPisCommonPayment(PAYMENT_ID, PSU_ID_DATA);
    }

    private static PaymentInitiationParameters buildPaymentInitiationParameters() {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentProduct("sepa-credit-transfers");
        parameters.setPaymentType(PaymentType.PERIODIC);
        parameters.setPsuData(PSU_ID_DATA);
        return parameters;
    }

    private static PeriodicPaymentInitiationResponse buildPeriodicPaymentInitiationResponse(InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider) {
        PeriodicPaymentInitiationResponse response = new PeriodicPaymentInitiationResponse();
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

    private static PeriodicPaymentInitiationResponse buildSpiErrorForPeriodicPayment() {
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                      .build();

        return new PeriodicPaymentInitiationResponse(errorHolder);
    }

    private static PisPaymentInfo buildPisPaymentInfoRequest() {
        return new PisPaymentInfo();
    }
}
