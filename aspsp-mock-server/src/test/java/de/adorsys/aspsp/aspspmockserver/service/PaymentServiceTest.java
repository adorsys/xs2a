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
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentServiceTest {
    private static final String PAYMENT_ID = "123456789";
    private static final String WRONG_PAYMENT_ID = "0";

    @Autowired
    private PaymentService paymentService;
    @MockBean
    private PaymentRepository paymentRepository;

    @Before
    public void setUp() {
        when(paymentRepository.save(any(SpiSinglePayments.class)))
            .thenReturn(getSpiSinglePayment());
        when(paymentRepository.exists(PAYMENT_ID))
            .thenReturn(true);
        when(paymentRepository.exists(WRONG_PAYMENT_ID))
            .thenReturn(false);
    }

    @Test
    public void addPayment() {
        //Given
        SpiSinglePayments expectedPayment = getSpiSinglePayment();

        //When
        SpiSinglePayments actualPayment = paymentService.addPayment(expectedPayment).get();

        //Then
        assertThat(actualPayment).isNotNull();
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
        expectedPayments.add(getSpiSinglePayment());

        //When
        List<SpiSinglePayments> actualPayments = paymentService.addBulkPayments(expectedPayments);

        //Then
        assertThat(actualPayments).isNotNull();
    }

    private SpiSinglePayments getSpiSinglePayment(){
        SpiSinglePayments payment = new SpiSinglePayments();
        SpiAmount amount = new SpiAmount(Currency.getInstance("EUR"),"20");
        SpiAccountReference accountReference = new SpiAccountReference("11234","DE23100120020123456789",null,null,null,null,Currency.getInstance("EUR"));
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(accountReference);
        payment.setCreditorName("Merchant123");
        payment.setPurposeCode("BEQNSD");
        payment.setCreditorAgent("sdasd");
        payment.setCreditorAccount(accountReference);
        payment.setRemittanceInformationUnstructured("Ref Number Merchant");

        return payment;
    }
}
