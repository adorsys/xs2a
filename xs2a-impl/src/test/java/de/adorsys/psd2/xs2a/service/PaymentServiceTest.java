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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentFactory;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisConsent;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.consent.PisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aTransactionalStatusMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.payment.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {
    private static final String PAYMENT_ID = "12345";
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
    private static final String AMOUNT = "100";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(null, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(null, null, null, null);

    private final SinglePayment SINGLE_PAYMENT_OK = getSinglePayment(IBAN, AMOUNT);

    private final BulkPayment BULK_PAYMENT_OK = getBulkPayment(SINGLE_PAYMENT_OK, IBAN);

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private SpiToXs2aTransactionalStatusMapper paymentMapper;
    @Mock
    private CancelPaymentService cancelPaymentService;
    @Mock
    private ReadPaymentFactory readPaymentFactory;
    @Mock
    private Xs2aPisConsentService pisConsentService;
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
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private Xs2aToSpiPsuDataMapper psuDataMapper;
    @Mock
    private PisPsuDataService pisPsuDataService;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private ReadPaymentService<PaymentInformationResponse> readPaymentService;
    @Mock
    private SpiPaymentFactory spiPaymentFactory;

    @Mock
    private PisConsentResponse pisConsentResponse;
    @Mock
    private PisPayment pisPayment;
    @Mock
    private SpiPayment spiPayment;

    @Before
    public void setUp() {
        //Mapper
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.RCVD)).thenReturn(RCVD);
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.ACCP)).thenReturn(TransactionStatus.ACCP);
        when(paymentMapper.mapToTransactionStatus(SpiTransactionStatus.RJCT)).thenReturn(TransactionStatus.RJCT);
        when(paymentMapper.mapToTransactionStatus(null)).thenReturn(null);
        when(xs2aPisConsentMapper.mapToXs2aPisConsent(any(), any())).thenReturn(getXs2aPisConsent());
        when(psuDataMapper.mapToSpiPsuData(PSU_ID_DATA))
            .thenReturn(SPI_PSU_DATA);
        when(xs2aPisConsentMapper.mapToXs2aPisConsent(new CreatePisConsentResponse("TEST"), PSU_ID_DATA)).thenReturn(getXs2aPisConsent());
        when(pisConsentDataService.getInternalPaymentIdByEncryptedString("TEST")).thenReturn("TEST");

        //Status by ID
        when(createBulkPaymentService.createPayment(BULK_PAYMENT_OK, getBulkPaymentInitiationParameters(), getTppInfoServiceModified(), getXs2aPisConsent()))
            .thenReturn(getValidResponse());

        when(pisConsentDataService.getAspspConsentData(anyString())).thenReturn(ASPSP_CONSENT_DATA);
        when(tppService.getTppInfo()).thenReturn(getTppInfo());

        when(cancelPaymentService.initiatePaymentCancellation(any(), any(), any()))
            .thenReturn(ResponseObject.<CancelPaymentResponse>builder()
                            .body(getCancelPaymentResponse(true, ACTC))
                            .build());
        when(cancelPaymentService.cancelPaymentWithoutAuthorisation(any(), any(), any()))
            .thenReturn(ResponseObject.<CancelPaymentResponse>builder()
                            .body(getCancelPaymentResponse(false, CANC))
                            .build());

        when(readPaymentFactory.getService(anyString())).thenReturn(readPaymentService);
    }

    // TODO Update tests after rearranging order of payment creation with pis consent https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/159
    //Bulk Tests
    @Test
    public void createBulkPayments() {
        //When
        ResponseObject<BulkPaymentInitiationResponse> actualResponse = paymentService.createPayment(BULK_PAYMENT_OK, getBulkPaymentInitiationParameters());
        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getBody().getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(actualResponse.getBody().getTransactionStatus()).isEqualTo(RCVD);
    }

    @Test
    public void cancelPayment_Success_WithAuthorisation() {
        when(aspspProfileService.isPaymentCancellationAuthorizationMandated()).thenReturn(Boolean.TRUE);
        when(pisPsuDataService.getPsuDataByPaymentId(PAYMENT_ID))
            .thenReturn(PSU_ID_DATA);
        when(pisConsentService.getPisConsentById(anyString())).thenReturn(Optional.of(pisConsentResponse));
        when(pisConsentResponse.getPayments()).thenReturn(Collections.singletonList(pisPayment));
        when(pisConsentResponse.getPaymentProduct()).thenReturn(PaymentProduct.SEPA);
        when(spiPaymentFactory.getSpiPaymentByPaymentType(eq(pisPayment), eq(PaymentProduct.SEPA), any(PaymentType.class))).thenReturn(Optional.of(spiPayment));

        // When
        ResponseObject<CancelPaymentResponse> actual = paymentService.cancelPayment(PaymentType.SINGLE, PAYMENT_ID);

        // Then
        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody().isStartAuthorisationRequired()).isEqualTo(true);
    }

    @Test
    public void cancelPayment_Success_WithoutAuthorisation() {
        when(aspspProfileService.isPaymentCancellationAuthorizationMandated()).thenReturn(Boolean.FALSE);
        when(pisPsuDataService.getPsuDataByPaymentId(PAYMENT_ID))
            .thenReturn(PSU_ID_DATA);
        when(pisConsentService.getPisConsentById(anyString())).thenReturn(Optional.of(pisConsentResponse));
        when(pisConsentResponse.getPayments()).thenReturn(Collections.singletonList(pisPayment));
        when(pisConsentResponse.getPaymentProduct()).thenReturn(PaymentProduct.SEPA);
        when(spiPaymentFactory.getSpiPaymentByPaymentType(eq(pisPayment), eq(PaymentProduct.SEPA), any(PaymentType.class))).thenReturn(Optional.of(spiPayment));

        // When
        ResponseObject<CancelPaymentResponse> actual = paymentService.cancelPayment(PaymentType.SINGLE, PAYMENT_ID);

        // Then
        assertThat(actual.getBody()).isNotNull();
        assertThat(actual.getBody().isStartAuthorisationRequired()).isEqualTo(false);
    }

    @Test
    public void cancelPayment_Success_ShouldRecordEvent() {
        when(aspspProfileService.isPaymentCancellationAuthorizationMandated()).thenReturn(Boolean.FALSE);
        when(pisPsuDataService.getPsuDataByPaymentId(PAYMENT_ID))
            .thenReturn(PSU_ID_DATA);
        when(pisConsentService.getPisConsentById(anyString())).thenReturn(Optional.of(pisConsentResponse));
        when(pisConsentResponse.getPayments()).thenReturn(Collections.singletonList(pisPayment));
        when(pisConsentResponse.getPaymentProduct()).thenReturn(PaymentProduct.SEPA);
        when(spiPaymentFactory.getSpiPaymentByPaymentType(eq(pisPayment), eq(PaymentProduct.SEPA), any(PaymentType.class))).thenReturn(Optional.of(spiPayment));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentService.cancelPayment(PaymentType.SINGLE, PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.PAYMENT_CANCELLATION_REQUEST_RECEIVED);
    }

    @Test
    public void createPayment_Success_ShouldRecordEvent() {
        // Given:
        PaymentInitiationParameters parameters = buildPaymentInitiationParameters();
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentService.createPayment(SINGLE_PAYMENT_OK, parameters);

        // Then
        verify(xs2aEventService, times(1)).recordTppRequest(argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.PAYMENT_INITIATION_REQUEST_RECEIVED);
    }

    @Test
    public void getPaymentById_Success_ShouldRecordEvent() {
        when(readPaymentService.getPayment(any(), any(), any(), any()))
            .thenReturn(new PaymentInformationResponse(SINGLE_PAYMENT_OK));
        when(pisConsentService.getPisConsentById(anyString()))
            .thenReturn(Optional.of(new PisConsentResponse()));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentService.getPaymentById(PaymentType.SINGLE, PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_REQUEST_RECEIVED);
    }

    @Test
    public void getPaymentStatusById_Success_ShouldRecordEvent() {
        SpiResponse<SpiTransactionStatus> spiResponse = buildSpiResponseTransactionStatus();
        when(singlePaymentSpi.getPaymentStatusById(any(), any(), any())).thenReturn(spiResponse);
        when(pisConsentService.getPisConsentById(anyString())).thenReturn(Optional.of(pisConsentResponse));
        when(pisConsentResponse.getPayments()).thenReturn(Collections.singletonList(pisPayment));
        when(pisConsentResponse.getPaymentProduct()).thenReturn(PaymentProduct.SEPA);

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentService.getPaymentStatusById(PaymentType.SINGLE, PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_TRANSACTION_STATUS_REQUEST_RECEIVED);
    }

    private SpiResponse<SpiTransactionStatus> buildSpiResponseTransactionStatus() {
        return new SpiResponse<>(SpiTransactionStatus.ACCP, ASPSP_CONSENT_DATA);
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

    private BulkPaymentInitiationResponse getBulkResponses(TransactionStatus status, MessageErrorCode errorCode) {
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
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
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

    private BulkPayment getBulkPayment(SinglePayment singlePayment1, String iban) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setPayments(Collections.singletonList(singlePayment1));
        bulkPayment.setRequestedExecutionDate(LocalDate.now());
        bulkPayment.setDebtorAccount(getReference(iban));
        bulkPayment.setBatchBookingPreferred(false);

        return bulkPayment;
    }

    private Xs2aPisConsent getXs2aPisConsent() {
        return new Xs2aPisConsent("TEST", PSU_ID_DATA);
    }


    private PaymentInitiationParameters getBulkPaymentInitiationParameters() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(PaymentType.BULK);

        return requestParameters;
    }

    private PaymentInitiationParameters buildPaymentInitiationParameters() {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(PaymentType.SINGLE);

        return requestParameters;
    }

    private ResponseObject<BulkPaymentInitiationResponse> getValidResponse() {
        return ResponseObject.<BulkPaymentInitiationResponse>builder().body(getBulkResponses(RCVD, null)).build();
    }

    private CancelPaymentResponse getCancelPaymentResponse(boolean authorisationRequired, TransactionStatus transactionStatus) {
        CancelPaymentResponse response = new CancelPaymentResponse();
        response.setStartAuthorisationRequired(authorisationRequired);
        response.setTransactionStatus(transactionStatus);
        return response;
    }
}
