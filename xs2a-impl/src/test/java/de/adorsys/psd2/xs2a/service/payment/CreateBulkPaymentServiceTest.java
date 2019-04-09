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
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateBulkPaymentServiceTest {
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String IBAN = "DE123456789";
    private static final PsuIdData PSU_DATA = new PsuIdData("correct_psu", null, null, null);
    private static final PsuIdData WRONG_PSU_DATA = new PsuIdData("wrong_psu", null, null, null);
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final TppInfo WRONG_TPP_INFO = new TppInfo();
    private static final Xs2aPisCommonPayment PIS_COMMON_PAYMENT = new Xs2aPisCommonPayment(PAYMENT_ID, PSU_DATA);
    private static final Xs2aPisCommonPayment PIS_COMMON_PAYMENT_FAIL = new Xs2aPisCommonPayment(null, PSU_DATA);
    private static final PaymentInitiationParameters PARAM = buildPaymentInitiationParameters();
    private static final CreatePisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = new CreatePisCommonPaymentResponse(PAYMENT_ID);
    private static final PisPaymentInfo PAYMENT_INFO = buildPisPaymentInfoRequest();
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final BulkPaymentInitiationResponse RESPONSE = buildBulkPaymentInitiationResponse();
    private static final List<String> ERROR_MESSAGE_TEXT = Arrays.asList("message 1", "message 2", "message 3");
    private static final Xs2aCreatePisAuthorisationResponse CREATE_PIS_AUTHORISATION_RESPONSE = new Xs2aCreatePisAuthorisationResponse(null, null, null);

    @InjectMocks
    private CreateBulkPaymentService createBulkPaymentService;
    @Mock
    private ScaPaymentService scaPaymentService;
    @Mock
    private ScaPaymentServiceResolver scaPaymentServiceResolver;
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


    @Before
    public void init() {
        when(scaPaymentService.createBulkPayment(buildBulkPayment(), TPP_INFO, "sepa-credit-transfers", PSU_DATA))
            .thenReturn(RESPONSE);
        when(scaPaymentService.createBulkPayment(buildBulkPayment(), WRONG_TPP_INFO, "sepa-credit-transfers", WRONG_PSU_DATA))
            .thenReturn(buildSpiErrorForBulkPayment());
        when(pisAspspDataService.getInternalPaymentIdByEncryptedString(anyString()))
            .thenReturn(PAYMENT_ID);
        when(pisCommonPaymentService.createCommonPayment(PAYMENT_INFO))
            .thenReturn(PIS_COMMON_PAYMENT_RESPONSE);
        when(xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(PIS_COMMON_PAYMENT_RESPONSE, PSU_DATA))
            .thenReturn(PIS_COMMON_PAYMENT);
        when(xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(PARAM, TPP_INFO, RESPONSE))
            .thenReturn(PAYMENT_INFO);
        when(scaPaymentServiceResolver.getService())
            .thenReturn(scaPaymentService);
    }

    @Test
    public void createPayment_success() {
        //When
        ResponseObject<BulkPaymentInitiationResponse> actualResponse = createBulkPaymentService.createPayment(buildBulkPayment(), buildPaymentInitiationParameters(), buildTppInfo());

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
    }

    @Test
    public void createPayment_wrongPsuData() {
        // Given
        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");

        PaymentInitiationParameters param = buildPaymentInitiationParameters();
        param.setPsuData(WRONG_PSU_DATA);

        //When
        ResponseObject<BulkPaymentInitiationResponse> actualResponse = createBulkPaymentService.createPayment(buildBulkPayment(), param, WRONG_TPP_INFO);

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
        ResponseObject<BulkPaymentInitiationResponse> actualResponse = createBulkPaymentService.createPayment(buildBulkPayment(), buildPaymentInitiationParameters(), buildTppInfo());

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.PAYMENT_FAILED);
    }

    @Test
    public void createPayment_pisScaAuthorisationService_createCommonPaymentAuthorisation_fail() {
        // Given
        when(authorisationMethodDecider.isImplicitMethod(false, false))
            .thenReturn(true);
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PaymentType.BULK, PARAM.getPsuData()))
            .thenReturn(Optional.empty());

        //When
        ResponseObject<BulkPaymentInitiationResponse> actualResponse = createBulkPaymentService.createPayment(buildBulkPayment(), buildPaymentInitiationParameters(), buildTppInfo());

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
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PaymentType.BULK, PARAM.getPsuData()))
            .thenReturn(Optional.of(CREATE_PIS_AUTHORISATION_RESPONSE));

        //When
        ResponseObject<BulkPaymentInitiationResponse> actualResponse = createBulkPaymentService.createPayment(buildBulkPayment(), buildPaymentInitiationParameters(), buildTppInfo());

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody()).isEqualTo(RESPONSE);
    }

    private static BulkPayment buildBulkPayment() {
        BulkPayment payment = new BulkPayment();
        payment.setPaymentId(PAYMENT_ID);
        payment.setPayments(buildListSinglePayment());
        payment.setDebtorAccount(buildReference());
        payment.setTransactionStatus(TransactionStatus.RCVD);
        return payment;
    }

    private static List<SinglePayment> buildListSinglePayment() {
        List<SinglePayment> list = new ArrayList<>();
        SinglePayment payment = new SinglePayment();
        Xs2aAmount amount = buildXs2aAmount();
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(buildReference());
        payment.setCreditorAccount(buildReference());
        payment.setTransactionStatus(TransactionStatus.RCVD);
        list.add(payment);
        return list;
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

    private static PaymentInitiationParameters buildPaymentInitiationParameters() {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentProduct("sepa-credit-transfers");
        parameters.setPaymentType(PaymentType.BULK);
        parameters.setPsuData(PSU_DATA);
        return parameters;
    }

    private static BulkPaymentInitiationResponse buildBulkPaymentInitiationResponse() {
        BulkPaymentInitiationResponse response = new BulkPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspConsentData(ASPSP_CONSENT_DATA);
        return response;
    }

    private static BulkPaymentInitiationResponse buildSpiErrorForBulkPayment() {
        ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
            .errorType(ErrorType.PIIS_400)
            .messages(ERROR_MESSAGE_TEXT)
            .build();

        return new BulkPaymentInitiationResponse(errorHolder);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static PisPaymentInfo buildPisPaymentInfoRequest() {
        return new PisPaymentInfo();
    }
}
