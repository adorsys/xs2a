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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.domain.pis.AspspPayment;
import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.aspspmockserver.service.mapper.PaymentMapper;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountBalance;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.account.AspspBalanceType;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspBulkPayment;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPaymentCancellationResponse;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspSinglePayment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {
    private static final String PAYMENT_ID = "123456789";
    private static final String WRONG_PAYMENT_ID = "0";
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
    private static final long BALANCE_AMOUNT = 100;
    private static final long AMOUNT_TO_TRANSFER = 50;
    private static final long EXCEEDING_AMOUNT_TO_TRANSFER = BALANCE_AMOUNT + 1;
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @InjectMocks
    private PaymentService paymentService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private PaymentMapper paymentMapper;

    @Before
    public void setUp() {
        when(paymentRepository.save(any(AspspPayment.class)))
            .thenReturn(getAspspPayment(AMOUNT_TO_TRANSFER));
        when(paymentRepository.save(anyListOf(AspspPayment.class)))
            .thenReturn(Collections.singletonList(getAspspPayment(AMOUNT_TO_TRANSFER)));
        when(paymentRepository.exists(PAYMENT_ID))
            .thenReturn(true);
        when(paymentRepository.exists(WRONG_PAYMENT_ID))
            .thenReturn(false);
        when(accountService.getPsuIdByIban(IBAN)).thenReturn(Optional.of(getAccountDetails().get(0).getId()));
        when(accountService.getAccountsByIban(IBAN)).thenReturn(getAccountDetails());
        when(accountService.getAccountsByIban(WRONG_IBAN)).thenReturn(null);
        when(paymentMapper.mapToAspspPayment(any(), any())).thenReturn(new AspspPayment());
        when(paymentMapper.mapToAspspSinglePayment(any(AspspPayment.class))).thenReturn(getAspspSinglePayment(AMOUNT_TO_TRANSFER));
        when(paymentRepository.findOne(PAYMENT_ID)).thenReturn(getAspspPayment(AMOUNT_TO_TRANSFER));
    }

    @Test
    public void addPayment_Success() {
        when(accountService.getAccountsByIban(IBAN)).thenReturn(getAccountDetails());
        //Given
        AspspSinglePayment expectedPayment = getAspspSinglePayment(AMOUNT_TO_TRANSFER);

        //When
        Optional<AspspSinglePayment> aspspSinglePayment = paymentService.addPayment(expectedPayment);
        AspspSinglePayment actualPayment = aspspSinglePayment.orElse(null);

        //Then
        assertThat(actualPayment).isNotNull();
    }

    @Test
    public void addPayment_AmountsAreEqual() {
        //Given
        AspspSinglePayment expectedPayment = getAspspSinglePayment(BALANCE_AMOUNT);

        //When
        Optional<AspspSinglePayment> actualPayment = paymentService.addPayment(expectedPayment);

        //Then
        assertThat(actualPayment).isNotNull();
    }

    @Test
    public void addPayment_Failure() {
        //Given
        AspspSinglePayment expectedPayment = getAspspSinglePayment(EXCEEDING_AMOUNT_TO_TRANSFER);

        //When
        Optional<AspspSinglePayment> actualPayment = paymentService.addPayment(expectedPayment);

        //Then
        assertThat(actualPayment).isEqualTo(Optional.empty());
    }

    @Test
    public void getPaymentStatusById() {
        //Then
        assertThat(paymentService.isPaymentExist(PAYMENT_ID)).isTrue();
        assertThat(paymentService.isPaymentExist(WRONG_PAYMENT_ID)).isFalse();
    }

    @Test
    public void addBulkPayments_Success() {
        List<AspspPayment> payments = Collections.singletonList(getAspspPayment(AMOUNT_TO_TRANSFER));
        when(paymentMapper.mapToAspspPaymentList(any())).thenReturn(payments);
        when(paymentRepository.save(anyListOf(AspspPayment.class))).thenReturn(payments);
        when(paymentMapper.mapToAspspSinglePaymentList(anyListOf(AspspPayment.class)))
            .thenReturn(Collections.singletonList(getAspspSinglePayment(AMOUNT_TO_TRANSFER)));

        //Given
        AspspBulkPayment expectedPayment = new AspspBulkPayment();
        expectedPayment.setPayments(new ArrayList<>());
        expectedPayment.getPayments().add(getAspspSinglePayment(AMOUNT_TO_TRANSFER));

        //When
        Optional<AspspBulkPayment> actualPayment = paymentService.addBulkPayments(expectedPayment);

        //Then
        assertThat(actualPayment.isPresent()).isTrue();
        AspspBulkPayment bulkPayment = actualPayment.get();
        assertThat(bulkPayment.getPayments().size()).isEqualTo(1);
    }

    @Test
    public void addBulkPayments_Failure_InsufficientFunds() {
        when(paymentMapper.mapToAspspPaymentList(any()))
            .thenReturn(Arrays.asList(getAspspPayment(AMOUNT_TO_TRANSFER), getAspspPayment(EXCEEDING_AMOUNT_TO_TRANSFER)));

        //Given
        AspspBulkPayment spiBulkPayment = new AspspBulkPayment();
        List<AspspSinglePayment> payments = Arrays.asList(getAspspSinglePayment(AMOUNT_TO_TRANSFER),
                                                          getAspspSinglePayment(EXCEEDING_AMOUNT_TO_TRANSFER));
        spiBulkPayment.setPayments(payments);

        //When
        Optional<AspspBulkPayment> actualPayment = paymentService.addBulkPayments(spiBulkPayment);

        //Then
        assertThat(actualPayment.isPresent()).isFalse();
    }

    @Test
    public void cancelPayment_Success() {
        when(paymentRepository.save(getAspspPayment(AspspTransactionStatus.CANC)))
            .thenReturn(getAspspPayment(AspspTransactionStatus.CANC));

        //Given
        Optional<AspspPaymentCancellationResponse> expected = buildAspspPaymentCancellationResponse(AspspTransactionStatus.CANC, false);

        //When
        Optional<AspspPaymentCancellationResponse> actual = paymentService.cancelPayment(PAYMENT_ID);

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void cancelPayment_Failure_WrongId() {
        //When
        Optional<AspspPaymentCancellationResponse> actual = paymentService.cancelPayment(WRONG_PAYMENT_ID);

        //Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void initiatePaymentCancellation_Success() {
        when(paymentRepository.save(getAspspPayment(AspspTransactionStatus.ACTC)))
            .thenReturn(getAspspPayment(AspspTransactionStatus.ACTC));

        //Given
        Optional<AspspPaymentCancellationResponse> expected = buildAspspPaymentCancellationResponse(AspspTransactionStatus.ACTC, true);

        //When
        Optional<AspspPaymentCancellationResponse> actual = paymentService.initiatePaymentCancellation(PAYMENT_ID);

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void initiatePaymentCancellation_Failure_WrongId() {
        //When
        Optional<AspspPaymentCancellationResponse> actual = paymentService.initiatePaymentCancellation(WRONG_PAYMENT_ID);

        //Then
        assertThat(actual.isPresent()).isFalse();
    }

    private Optional<AspspPaymentCancellationResponse> buildAspspPaymentCancellationResponse(AspspTransactionStatus transactionStatus, boolean startAuthorisationRequired) {
        AspspPaymentCancellationResponse response = new AspspPaymentCancellationResponse();
        response.setTransactionStatus(transactionStatus);
        response.setCancellationAuthorisationMandated(startAuthorisationRequired);
        return Optional.of(response);
    }

    private Optional<AspspPaymentCancellationResponse> buildAspspPaymentCancellationResponse() {
        return Optional.of(new AspspPaymentCancellationResponse());
    }

    private AspspSinglePayment getAspspSinglePayment(long amountToTransfer) {
        AspspSinglePayment payment = new AspspSinglePayment();
        AspspAmount amount = new AspspAmount(Currency.getInstance("EUR"), new BigDecimal(amountToTransfer));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");

        return payment;
    }

    private AspspPayment getAspspPayment(AspspTransactionStatus transactionStatus, long amountToTransfer) {
        AspspPayment payment = new AspspPayment();
        AspspAmount amount = new AspspAmount(Currency.getInstance("EUR"), new BigDecimal(amountToTransfer));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");
        payment.setPaymentStatus(transactionStatus);
        return payment;
    }

    private AspspPayment getAspspPayment(long amountToTransfer) {
        return getAspspPayment(null, amountToTransfer);
    }

    private AspspPayment getAspspPayment(AspspTransactionStatus transactionStatus) {
        return getAspspPayment(transactionStatus, AMOUNT_TO_TRANSFER);
    }

    private List<AspspAccountDetails> getAccountDetails() {
        return Collections.singletonList(
            new AspspAccountDetails("12345", IBAN, null, null, null, null, CURRENCY, "Peter", null, null, null, null, null, null, null, getBalances())
        );
    }

    private List<AspspAccountBalance> getBalances() {
        AspspAccountBalance balance = new AspspAccountBalance();
        balance.setSpiBalanceAmount(new AspspAmount(CURRENCY, BigDecimal.valueOf(BALANCE_AMOUNT)));
        balance.setSpiBalanceType(AspspBalanceType.INTERIM_AVAILABLE);
        return Collections.singletonList(balance);
    }

    private AspspAccountReference getReference() {
        AspspAccountDetails details = getAccountDetails().get(0);
        return new AspspAccountReference(details.getIban(), null, null, null, null, details.getCurrency());
    }
}
