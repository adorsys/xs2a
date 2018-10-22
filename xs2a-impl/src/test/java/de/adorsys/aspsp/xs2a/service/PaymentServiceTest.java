/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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


package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.config.factory.ReadPaymentFactory;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aPisConsent;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.service.payment.CreateBulkPaymentService;
import de.adorsys.aspsp.xs2a.service.payment.CreatePeriodicPaymentService;
import de.adorsys.aspsp.xs2a.service.payment.CreateSinglePaymentService;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_400;
import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RCVD;
import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RJCT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {
    private static final String PAYMENT_ID = "12345";
    private static final String WRONG_PAYMENT_ID = "";
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
    private static final String AMOUNT = "100";
    private static final String EXCESSIVE_AMOUNT = "10000";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData();

    private final SinglePayment SINGLE_PAYMENT_OK = getSinglePayment(IBAN, AMOUNT);
    private final SinglePayment SINGLE_PAYMENT_NOK_IBAN = getSinglePayment(WRONG_IBAN, AMOUNT);

    private final BulkPayment BULK_PAYMENT_OK = getBulkPayment(SINGLE_PAYMENT_OK, IBAN);

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private ReadPaymentFactory readPaymentFactory;
    @Mock
    private PisConsentService pisConsentService;
    @Mock
    private PisConsentDataService pisConsentDataService;
    @Mock
    private TppService tppService;
    @Mock
    private CreateSinglePaymentService createSinglePaymentService;
    @Mock
    private CreatePeriodicPaymentService createPeriodicPaymentService;
    @Mock
    private CreateBulkPaymentService createBulkPaymentService;
    @Mock
    private Xs2aPisConsentMapper xs2aPisConsentMapper;
    @Mock
    private SinglePaymentSpi singlePaymentSpi;
    @Mock
    private PeriodicPaymentSpi periodicPaymentSpi;
    @Mock
    private BulkPaymentSpi bulkPaymentSpi;

    @Before
    public void setUp() {
        //Mapper
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.RCVD)).thenReturn(RCVD);
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.ACCP)).thenReturn(Xs2aTransactionStatus.ACCP);
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.RJCT)).thenReturn(Xs2aTransactionStatus.RJCT);
        when(paymentMapper.mapToTransactionStatus(null)).thenReturn(null);
        when(paymentMapper.mapToPaymentInitResponseFailedPayment(SINGLE_PAYMENT_NOK_IBAN, RESOURCE_UNKNOWN_400))
            .thenReturn(getPaymentResponse(RJCT, RESOURCE_UNKNOWN_400));
        when(xs2aPisConsentMapper.mapToXs2aPisConsent(any())).thenReturn(getXs2aPisConsent());

        //Status by ID
        when(createBulkPaymentService.createPayment(BULK_PAYMENT_OK, getBulkPaymentInitiationParameters(), getTppInfoServiceModified(), getXs2aPisConsent()))
            .thenReturn(getValidResponse());

        when(pisConsentDataService.getAspspConsentDataByPaymentId(anyString())).thenReturn(ASPSP_CONSENT_DATA);
        when(tppService.getTppInfo()).thenReturn(getTppInfo());
    }

    // TODO Update tests after rearranging order of payment creation with pis consent https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/159
    //Bulk Tests
    @Test
    public void createBulkPayments() {
        BulkPayment payment = BULK_PAYMENT_OK;
        //When
        ResponseObject<BulkPaymentInitiationResponse> actualResponse = paymentService.createPayment(payment, getBulkPaymentInitiationParameters());
        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }

    //Test additional methods
    private PaymentInitialisationResponse getPaymentResponse(Xs2aTransactionStatus status, MessageErrorCode errorCode) {
        PaymentInitialisationResponse paymentInitialisationResponse = new PaymentInitialisationResponse();
        paymentInitialisationResponse.setTransactionStatus(status);

        paymentInitialisationResponse.setPaymentId(status == RJCT ? null : PAYMENT_ID);
        if (status == RJCT) {
            paymentInitialisationResponse.setTppMessages(new MessageErrorCode[]{errorCode});
        }
        return paymentInitialisationResponse;
    }

    private static SinglePayment getSinglePayment(String iban, String amountToPay) {
        SinglePayment singlePayments = new SinglePayment();
        singlePayments.setEndToEndIdentification(PAYMENT_ID);
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(CURRENCY);
        amount.setAmount(amountToPay);
        singlePayments.setInstructedAmount(amount);
        singlePayments.setDebtorAccount(getReference(iban));
        singlePayments.setCreditorAccount(getReference(iban));
        singlePayments.setRequestedExecutionDate(LocalDate.now());
        singlePayments.setRequestedExecutionTime(OffsetDateTime.now());
        return singlePayments;
    }

    private static Xs2aAccountReference getReference(String iban) {
        Xs2aAccountReference reference = new Xs2aAccountReference();
        reference.setIban(iban);
        reference.setCurrency(CURRENCY);

        return reference;
    }

    private BulkPaymentInitiationResponse getBulkResponses(Xs2aTransactionStatus status, MessageErrorCode errorCode) {
        BulkPaymentInitiationResponse response = new BulkPaymentInitiationResponse();
        response.setTransactionStatus(status);

        response.setPaymentId(status == RJCT ? null : PAYMENT_ID);
        if (status == RJCT) {
            response.setTppMessages(new MessageErrorCode[]{errorCode});
        }
        return response;
    }

    private static TppInfo getTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(Xs2aTppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        tppInfo.setAuthorityName("authorityName");
        tppInfo.setCountry("country");
        tppInfo.setOrganisation("organisation");
        tppInfo.setOrganisationUnit("organisationUnit");
        tppInfo.setCity("city");
        tppInfo.setState("state");
        tppInfo.setRedirectUri("redirectUri");
        tppInfo.setNokRedirectUri("nokRedirectUri");
        return tppInfo;
    }

    private static TppInfo getTppInfoServiceModified() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(Xs2aTppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        tppInfo.setAuthorityName("authorityName");
        tppInfo.setCountry("country");
        tppInfo.setOrganisation("organisation");
        tppInfo.setOrganisationUnit("organisationUnit");
        tppInfo.setCity("city");
        tppInfo.setState("state");

        return tppInfo;
    }

    private BulkPayment getBulkPayment(SinglePayment singlePayment1, String iban) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setPayments(Collections.singletonList(singlePayment1));
        bulkPayment.setRequestedExecutionDate(LocalDate.now());
        bulkPayment.setDebtorAccount(getReference(iban));
        bulkPayment.setBatchBookingPreferred(false);

        return bulkPayment;
    }

    private Xs2aPisConsent getXs2aPisConsent() {
        return new Xs2aPisConsent("TEST");
    }


    private PaymentInitiationParameters getBulkPaymentInitiationParameters() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(PaymentType.BULK);

        return requestParameters;
    }

    private ResponseObject<BulkPaymentInitiationResponse> getValidResponse() {
        return ResponseObject.<BulkPaymentInitiationResponse>builder().body(getBulkResponses(RCVD, null)).build();
    }
}
