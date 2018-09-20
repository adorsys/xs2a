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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RCVD;
import static de.adorsys.aspsp.xs2a.domain.consent.Xs2aAuthorisationStartType.EXPLICIT;
import static de.adorsys.aspsp.xs2a.domain.consent.Xs2aAuthorisationStartType.IMPLICIT;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.*;
import static de.adorsys.psd2.model.ScaStatus.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmbeddedScaPaymentServiceTest {

    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final String IBAN = "DE123456789";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1234-4567-8900-0000";
    private static final String CONSENT_ID = "Consent 123456";
    private static final String AUTH_ID = "AUTH ID";
    @InjectMocks
    private EmbeddedScaPaymentService paymentService;
    @Mock
    private PisConsentService consentService;
    @Mock
    private Xs2aPisConsentMapper pisConsentMapper;
    @Mock
    private AspspProfileService profileService;
    @Mock
    private PisAuthorisationService pisAuthorizationService;
    @Mock
    private PaymentSpi paymentSpi;
    @Mock
    private PaymentMapper paymentMapper;

    @Before
    public void setUp() {
        when(paymentMapper.mapToSpiSinglePayment(any())).thenReturn(new SpiSinglePayment());
        when(paymentMapper.mapToSpiPeriodicPayment(any())).thenReturn(new SpiPeriodicPayment());
        when(paymentMapper.mapToSpiSinglePaymentList(any())).thenReturn(Collections.singletonList(new SpiSinglePayment()));
        when(paymentMapper.mapToPaymentInitializationResponse(any())).thenReturn(getPaymentInitResponse());
        when(pisConsentMapper.mapToCmsPisConsentRequestForPeriodicPayment(any(), any()))
            .thenReturn(getPisConsentRequest());
        when(pisConsentMapper.mapToCmsPisConsentRequestForSinglePayment(any(), any()))
            .thenReturn(getPisConsentRequest());
        when(pisConsentMapper.mapToCmsPisConsentRequestForBulkPayment(any()))
            .thenReturn(getPisConsentRequest());
        when(consentService.createPisConsentForPeriodicPayment(any(), any()))
            .thenReturn(getCreateConsentResponse());
        when(consentService.createPisConsentForSinglePayment(any(), any()))
            .thenReturn(getCreateConsentResponse());
        when(consentService.createPisConsentForBulkPayment(any()))
            .thenReturn(getCreateConsentResponse());
        when(paymentSpi.createPaymentInitiation(any(), any())).thenReturn(getSpiPaymentInitResponse());
        when(paymentSpi.initiatePeriodicPayment(any(), any())).thenReturn(getSpiPaymentInitResponse());
        when(paymentSpi.createBulkPayments(any(), any())).thenReturn(new SpiResponse<>(Collections.singletonList(getSpiPaymentInitResponse().getPayload()),new AspspConsentData()));
    }

    private SpiResponse<SpiPaymentInitialisationResponse> getSpiPaymentInitResponse() {
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(SpiTransactionStatus.RCVD);
        return new SpiResponse<>(response, new AspspConsentData());
    }

    private PaymentInitialisationResponse getPaymentInitResponse() {
        PaymentInitialisationResponse response = new PaymentInitialisationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(RCVD);
        return response;
    }

    @Test
    public void createPeriodicPayment_Explicit() {
        when(profileService.getAuthorisationStartType())
            .thenReturn(EXPLICIT);
        createPeriodicPayment(false);
    }

    @Test
    public void createPeriodicPayment_Implicit() {
        when(profileService.getAuthorisationStartType())
            .thenReturn(IMPLICIT);
        createPeriodicPayment(true);
    }

    private void createPeriodicPayment(boolean isImplicit) {
        when(pisAuthorizationService.createConsentAuthorisation(anyString(), any()))
            .thenReturn(getCreateAuth(PERIODIC));
        //When
        PaymentInitialisationResponse response = paymentService.createPeriodicPayment(getPeriodic(), getTppInfo(), PAYMENT_PRODUCT);
        //Then
        assertThat(response.getTransactionStatus()).isEqualTo(RCVD);
        assertThat(response.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(response.getPisConsentId()).isEqualTo(CONSENT_ID);
        assertThat(response.getAuthorizationId()).isEqualTo(isImplicit ? AUTH_ID : null);
        assertThat(response.getScaStatus()).isEqualTo(isImplicit ? STARTED.name() : null);
        assertThat(response.getPaymentType()).isEqualTo(PERIODIC.name());
    }

    @Test
    public void createSinglePayment_Explicit() {
        when(profileService.getAuthorisationStartType())
            .thenReturn(EXPLICIT);
        createSinglePayment(false);
    }

    @Test
    public void createSinglePayment_Implicit() {
        when(profileService.getAuthorisationStartType())
            .thenReturn(IMPLICIT);
        createSinglePayment(true);
    }

    private void createSinglePayment(boolean isImplicit) {
        when(pisAuthorizationService.createConsentAuthorisation(anyString(), any()))
            .thenReturn(getCreateAuth(SINGLE));
        //When
        PaymentInitialisationResponse response = paymentService.createSinglePayment(getSinglePayment(), getTppInfo(), PAYMENT_PRODUCT);
        //Then
        assertThat(response.getTransactionStatus()).isEqualTo(RCVD);
        assertThat(response.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(response.getPisConsentId()).isEqualTo(CONSENT_ID);
        assertThat(response.getAuthorizationId()).isEqualTo(isImplicit ? AUTH_ID : null);
        assertThat(response.getScaStatus()).isEqualTo(isImplicit ? STARTED.name() : null);
        assertThat(response.getPaymentType()).isEqualTo(SINGLE.name());
    }

    @Test
    public void createBulkPayment_Explicit() {
        when(profileService.getAuthorisationStartType())
            .thenReturn(EXPLICIT);
        createBulkPayment(false);
    }

    @Test
    public void createBulkPayment_Implicit() {
        when(profileService.getAuthorisationStartType())
            .thenReturn(IMPLICIT);
        createBulkPayment(true);
    }

    private void createBulkPayment(boolean isImplicit) {
        when(pisAuthorizationService.createConsentAuthorisation(anyString(), any()))
            .thenReturn(getCreateAuth(BULK));
        //When
        List<PaymentInitialisationResponse> serviceResponse = paymentService.createBulkPayment(getBulkPayment(getSinglePayment(), getReference()), getTppInfo(), PAYMENT_PRODUCT);

        //Then
        PaymentInitialisationResponse response = serviceResponse.iterator().next();
        assertThat(response.getTransactionStatus()).isEqualTo(RCVD);
        assertThat(response.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(response.getPisConsentId()).isEqualTo(CONSENT_ID);
        assertThat(response.getAuthorizationId()).isEqualTo(isImplicit ? AUTH_ID : null);
        assertThat(response.getScaStatus()).isEqualTo(isImplicit ? STARTED.name() : null);
        assertThat(response.getPaymentType()).isEqualTo(BULK.name());
    }

    private BulkPayment getBulkPayment(SinglePayment singlePayment1, AccountReference reference) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setPayments(Collections.singletonList(singlePayment1));
        bulkPayment.setRequestedExecutionDate(LocalDate.now());
        bulkPayment.setDebtorAccount(reference);
        bulkPayment.setBatchBookingPreferred(false);

        return bulkPayment;
    }

    private Optional<Xsa2CreatePisConsentAuthorisationResponse> getCreateAuth(PaymentType paymentType) {
        return Optional.of(new Xsa2CreatePisConsentAuthorisationResponse(AUTH_ID, STARTED.name(), paymentType.name()));
    }

    private CreatePisConsentResponse getCreateConsentResponse() {
        return new CreatePisConsentResponse(CONSENT_ID, PAYMENT_ID);
    }

    private SinglePayment getSinglePayment() {
        return getPeriodic();
    }

    private PeriodicPayment getPeriodic() {
        PeriodicPayment payment = new PeriodicPayment();
        payment.setEndToEndIdentification("ABCD123456789");
        payment.setDebtorAccount(getReference());
        payment.setCreditorAccount(getReference());
        payment.setCreditorName("CreditorName");
        payment.setCreditorAddress(getAddress());
        payment.setCreditorAgent("CreditorAgent");
        payment.setInstructedAmount(getAmount());
        payment.setRemittanceInformationUnstructured("Single payment Remittance");
        payment.setStartDate(LocalDate.now());
        payment.setEndDate(LocalDate.now().plusMonths(2));
        payment.setDayOfExecution(1);
        payment.setFrequency(Xs2aFrequencyCode.MONTHLY);
        return payment;
    }

    private Xs2aAmount getAmount() {
        Xs2aAmount amount = new Xs2aAmount();
        amount.setAmount("1000.00");
        amount.setCurrency(CURRENCY);
        return amount;
    }

    private AccountReference getReference() {
        AccountReference reference = new AccountReference();
        reference.setIban(IBAN);
        reference.setCurrency(CURRENCY);
        return reference;
    }

    private Xs2aAddress getAddress() {
        Xs2aAddress address = new Xs2aAddress();
        Xs2aCountryCode countryCode = new Xs2aCountryCode();
        countryCode.setCode("Ukraine");
        address.setCountry(countryCode);
        address.setPostalCode("321654");
        address.setCity("Kiev");
        address.setStreet("Vladimirskaya");
        address.setBuildingNumber("61/11");
        return address;
    }

    private TppInfo getTppInfo() {
        TppInfo info = new TppInfo();
        info.setTppName("TPP");
        return info;
    }

    private PisConsentRequest getPisConsentRequest() {
        return new PisConsentRequest();
    }
}
