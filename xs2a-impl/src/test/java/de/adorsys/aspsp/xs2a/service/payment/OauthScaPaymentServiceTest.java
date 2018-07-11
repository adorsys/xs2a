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

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
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
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.PAYMENT_FAILED;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
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
        when(paymentMapper.mapToSpiSinglePaymentList(getBulk(true, true))).thenReturn(getSpiBulk(true, true));
        when(paymentMapper.mapToSpiSinglePaymentList(getBulk(true,false))).thenReturn(getSpiBulk(true,false));
        when(paymentMapper.mapToSpiSinglePaymentList(getBulk(false,false))).thenReturn(getSpiBulk(false,false));
        when(paymentMapper.mapToPaymentInitializationResponse(getSpiResp(true))).thenReturn(getResp(true));
        when(paymentMapper.mapToPaymentInitializationResponse(getSpiResp(false))).thenReturn(getResp(false));
        when(paymentMapper.mapToPaymentInitResponseFailedPayment(getPayment(false), PAYMENT_FAILED, false))
            .thenReturn(getResp(false));
        when(paymentSpi.createBulkPayments(getSpiBulk(true, true))).thenReturn(getSpiRespList(true, true));
        when(paymentSpi.createBulkPayments(getSpiBulk(true, false))).thenReturn(getSpiRespList(true, false));
        when(paymentSpi.createBulkPayments(getSpiBulk(false, false))).thenReturn(getSpiRespList(false, false));
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
        assertNotNull(actualResponse);
        assertTrue(actualResponse.get(0).getPaymentId().equals(PAYMENT_ID) && actualResponse.get(1).getPaymentId().equals(PAYMENT_ID));
        assertTrue(actualResponse.get(0).getTransactionStatus().equals(TransactionStatus.RCVD) && actualResponse.get(1).getTransactionStatus().equals(TransactionStatus.RCVD));
        assertTrue(actualResponse.get(0).getTppMessages() == null && actualResponse.get(1).getTppMessages() == null);
    }

    @Test
    public void createBulkPayment_Failure_partial() {
        //Given
        List<SinglePayments> payments = getBulk(true, false);
        //When
        List<PaymentInitialisationResponse> actualResponse = oauthScaPaymentService.createBulkPayment(payments);
        assertNotNull(actualResponse);
        assertTrue(actualResponse.get(0).getPaymentId().equals(PAYMENT_ID) && actualResponse.get(1).getPaymentId()==null);
        assertTrue(actualResponse.get(0).getTransactionStatus().equals(TransactionStatus.RCVD) && actualResponse.get(1).getTransactionStatus().equals(TransactionStatus.RJCT));
        assertTrue(actualResponse.get(0).getTppMessages() == null && actualResponse.get(1).getTppMessages()[0]==PAYMENT_FAILED);
    }

    @Test
    public void createBulkPayment_Failure_total() {
        //Given
        List<SinglePayments> payments = getBulk(false, false);
        //When
        List<PaymentInitialisationResponse> actualResponse = oauthScaPaymentService.createBulkPayment(payments);
        assertNotNull(actualResponse);
        assertTrue(actualResponse.get(0).getPaymentId()==null && actualResponse.get(1).getPaymentId()==null);
        assertTrue(actualResponse.get(0).getTransactionStatus().equals(TransactionStatus.RJCT) && actualResponse.get(1).getTransactionStatus().equals(TransactionStatus.RJCT));
        assertTrue(actualResponse.get(0).getTppMessages()[0]==PAYMENT_FAILED && actualResponse.get(1).getTppMessages()[0]==PAYMENT_FAILED);
    }

    @Test
    public void createSinglePayment() {
        //Nothing to be tested here
    }

    private List<SinglePayments> getBulk(boolean firstOk, boolean secondOk) {
        return Arrays.asList(getPayment(firstOk), getPayment(secondOk));
    }

    private Optional<PaymentInitialisationResponse> getResp(boolean paymentPassed) {
        PaymentInitialisationResponse response = new PaymentInitialisationResponse();
        if (paymentPassed) {
            response.setPaymentId(PAYMENT_ID);
            response.setTransactionStatus(TransactionStatus.RCVD);
        } else {
            response.setTppMessages(new MessageErrorCode[]{PAYMENT_FAILED});
            response.setTransactionStatus(TransactionStatus.RJCT);
        }
        return Optional.of(response);
    }

    private List<SpiPaymentInitialisationResponse> getSpiRespList(boolean firstOk, boolean secondOk) {
        return Arrays.asList(getSpiResp(firstOk), getSpiResp(secondOk));
    }

    private SpiPaymentInitialisationResponse getSpiResp(boolean paymentOk) {
        SpiPaymentInitialisationResponse response = new SpiPaymentInitialisationResponse();
        if (paymentOk) {
            response.setPaymentId(PAYMENT_ID);
            response.setTransactionStatus(SpiTransactionStatus.RCVD);
        } else {
            response.setTransactionStatus(SpiTransactionStatus.RJCT);
        }

        return response;
    }

    private SinglePayments getPayment(boolean paymentOk) {
        SinglePayments payment = new SinglePayments();
        payment.setCreditorName(paymentOk
                                    ? OK_CREDITOR
                                    : WRONG_CREDITOR);
        return payment;
    }

    private List<SpiSinglePayments> getSpiBulk(boolean firstOk, boolean secondOk) {
        return Arrays.asList(getSpiPayment(firstOk), getSpiPayment(secondOk));
    }

    private SpiSinglePayments getSpiPayment(boolean paymentOk) {
        SpiSinglePayments payment = new SpiSinglePayments();
        payment.setCreditorName(paymentOk
                                    ? OK_CREDITOR
                                    : WRONG_CREDITOR);
        return payment;
    }
}
