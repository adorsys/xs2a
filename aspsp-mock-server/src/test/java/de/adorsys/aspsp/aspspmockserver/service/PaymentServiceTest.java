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

import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.aspspmockserver.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.payment.AspspPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {
    private static final String PAYMENT_ID = "123456789";
    private static final String WRONG_PAYMENT_ID = "0";
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
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
            .thenReturn(getAspspPayment());
        when(paymentRepository.exists(PAYMENT_ID))
            .thenReturn(true);
        when(paymentRepository.exists(WRONG_PAYMENT_ID))
            .thenReturn(false);
        when(accountService.getAccountsByIban(IBAN)).thenReturn(getAccountDetails());
        when(accountService.getAccountsByIban(WRONG_IBAN)).thenReturn(null);
    }

    /*
    TODO Will be fix in the next sprint
    @Test
    public void addPayment_Success() {
        //Given
        SpiSinglePayments expectedPayment = getSpiSinglePayment(50);

        //When
        SpiSinglePayments actualPayment = paymentService.addPayment(expectedPayment).get();

        //Then
        assertThat(actualPayment).isNotNull();
    }
    */

    @Test
    public void addPayment_Failure() {
        //Given
        SpiSinglePayments expectedPayment = getSpiSinglePayment(100);

        //When
        Optional<SpiSinglePayments> actualPayment = paymentService.addPayment(expectedPayment);

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
    public void addBulkPayments() {
        //Given
        List<SpiSinglePayments> expectedPayments = new ArrayList<>();
        expectedPayments.add(getSpiSinglePayment(50));

        //When
        List<SpiSinglePayments> actualPayments = paymentService.addBulkPayments(expectedPayments);

        //Then
        assertThat(actualPayments).isNotNull();
    }

    private SpiSinglePayments getSpiSinglePayment(long amountToTransfer) {
        SpiSinglePayments payment = new SpiSinglePayments();
        SpiAmount amount = new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(amountToTransfer));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");

        return payment;
    }

    private AspspPayment getAspspPayment() {
        AspspPayment payment = new AspspPayment();
        SpiAmount amount = new SpiAmount(Currency.getInstance("EUR"), new BigDecimal((long) 50));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(getReference());
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(getReference());
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");
        return payment;
    }

    private List<SpiAccountDetails> getAccountDetails() {
        return Collections.singletonList(
            new SpiAccountDetails("12345", IBAN, null, null, null, null, CURRENCY, "Peter", null, null, null, getBalances())
        );
    }

    private List<SpiBalances> getBalances() {
        SpiBalances balances = new SpiBalances();
        SpiAccountBalance balance = new SpiAccountBalance();
        balance.setSpiAmount(new SpiAmount(CURRENCY, BigDecimal.valueOf(100)));
        balances.setInterimAvailable(balance);
        return Collections.singletonList(balances);
    }

    private SpiAccountReference getReference() {
        SpiAccountDetails details = getAccountDetails().get(0);
        return new SpiAccountReference(details.getIban(), null, null, null, null, details.getCurrency());
    }
}
