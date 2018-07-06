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

import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.RCVD;
import static de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus.RJCT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OauthScaPaymentServiceTest {
    private static final String OK_CREDITOR = "OK";
    private static final String WRONG_CREDITOR = "NOK";
    private static final String PAYMENT_ID = "123456789";


    @InjectMocks
    OauthScaPaymentService oauthScaPaymentService;
    @Mock
    PaymentMapper paymentMapper;
    @Mock
    PaymentSpi paymentSpi;

    @Before
    public void setUp() {
        when(paymentMapper.mapToSpiSinglePaymentList(getBulk(true, true))).thenReturn(getSpiBulk(true,true));
        when(paymentMapper.mapToSpiSinglePaymentList()).thenReturn();
        when(paymentSpi.createBulkPayments(getSpiBulk(true,true))).thenReturn(getSpiRespList(true,true));
        when(paymentSpi.).thenReturn();
    }

    private List<SpiPaymentInitialisationResponse> getSpiRespList(boolean firstOk, boolean secondOk) {
    return Arrays.asList(getSpiResp(firstOk),getSpiResp(secondOk));
    }

    private SpiPaymentInitialisationResponse getSpiResp(boolean firstOk) {
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        if (firstOk) {
            response.setPaymentId(PAYMENT_ID);
            response.setTransactionStatus(RCVD);
        }
        else {
            response.setTransactionStatus(RJCT);
        }
    }

    @Test
    public void createPeriodicPayment() {
        //Nothing to be tested here
    }

    @Test
    public void createBulkPayment() {
        //Given
        List<SinglePayments> payments = getBulk(true, true);
        //When
        List<PaymentInitialisationResponse> actualResponse = oauthScaPaymentService.createBulkPayment(payments);
        assertThat(actualResponse).isEqualTo();

    }

    private List<SinglePayments> getBulk(boolean firstOk, boolean secondOk) {
        return Arrays.asList(getPayment(firstOk), getPayment(secondOk));
    }

    @Test
    public void createSinglePayment() {
        //Nothing to be tested here
    }

    private SinglePayments getPayment(boolean creditorName) {
        SinglePayments payment = new SinglePayments();
        payment.setCreditorName(creditorName
                                    ? OK_CREDITOR
                                    : WRONG_CREDITOR);
        return payment;
    }

    private List<SpiSinglePayments> getSpiBulk(boolean firstOk, boolean secondOk) {
        return Arrays.asList(getSpiPayment(firstOk), getSpiPayment(secondOk));
    }

    private SpiSinglePayments getSpiPayment(boolean creditorName) {
        SpiSinglePayments payment = new SpiSinglePayments();
        payment.setCreditorName(creditorName
                                    ? OK_CREDITOR
                                    : WRONG_CREDITOR);
        return payment;
    }
}
