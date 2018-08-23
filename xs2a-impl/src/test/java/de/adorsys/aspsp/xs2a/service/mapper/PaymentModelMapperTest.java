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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.model.TppMessageGeneric;
import de.adorsys.psd2.model.TppMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PaymentModelMapperTest {

    private static final String PAYMENT_ID = "123456789";
    private static final String AMOUNT = "1000";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String PSU_MSG = "Payment is success";
    private static final String PATH = "https:\\test.com";
    private static final String MSG_TEXT = "Some error message";

    @Test
    public void mapToTransactionStatus12() {
        //Given
        TransactionStatus[] xs2aStatuses = TransactionStatus.values();
        de.adorsys.psd2.model.TransactionStatus[] statuses12 = de.adorsys.psd2.model.TransactionStatus.values();
        //When
        assertThat(xs2aStatuses.length).isEqualTo(statuses12.length);
        for (int i = 0; i < xs2aStatuses.length; i++) {
            testTransactionStatus12(xs2aStatuses[i], statuses12[i]);
        }
    }

    @Test
    public void mapToPaymentInitiationResponse12() {
        //Given
        PaymentInitialisationResponse givenResponse = getXs2aPaymentResponse();
        PaymentInitationRequestResponse201 expectedResponse = getPaymentResponse12();
        //When
        PaymentInitationRequestResponse201 result = PaymentModelMapper.mapToPaymentInitiationResponse12(givenResponse, PaymentType.SINGLE, PaymentProduct.SCT);
        //Then
        assertThat(result).isEqualTo(expectedResponse);
    }

    private PaymentInitationRequestResponse201 getPaymentResponse12() {
        PaymentInitationRequestResponse201 response = new PaymentInitationRequestResponse201();

        response.setTransactionStatus(de.adorsys.psd2.model.TransactionStatus.ACCP);
        response.setPaymentId(PAYMENT_ID);
        de.adorsys.psd2.model.Amount amount = new de.adorsys.psd2.model.Amount();
        amount.setAmount(AMOUNT);
        amount.setCurrency(EUR.getCurrencyCode());
        response.setTransactionFees(amount);
        response.setTransactionFeeIndicator(true);
        response.setScaMethods(null);
        response.setChosenScaMethod(null);
        response.setChallengeData(null);
        response.setLinks(null);
        response.setPsuMessage(PSU_MSG);

        TppMessageGeneric tppMessage = new TppMessageGeneric();
        tppMessage.setCategory(TppMessageCategory.ERROR);
        tppMessage.setPath(PATH);
        tppMessage.setText(MSG_TEXT);
        tppMessage.setCode(null);
        TppMessages messages = new TppMessages();
        messages.add(tppMessage);

        response.setTppMessages(null); //TODO fix this along with creating TppMessage mapper
        return response;
    }

    private PaymentInitialisationResponse getXs2aPaymentResponse() {
        PaymentInitialisationResponse response = new PaymentInitialisationResponse();
        response.setTransactionStatus(TransactionStatus.ACCP);
        response.setPaymentId(PAYMENT_ID);

        response.setTransactionFees(getXs2aAmount());
        response.setTransactionFeeIndicator(true);
        response.setScaMethods(null);
        response.setPsuMessage(PSU_MSG);
        response.setTppMessages(null); //TODO fix this along with creating TppMessage mapper
        response.setLinks(null);
        response.setTppRedirectPreferred(false);
        return response;
    }

    private Amount getXs2aAmount() {
        Amount amount = new Amount();
        amount.setContent(AMOUNT);
        amount.setCurrency(EUR);
        return amount;
    }

    private void testTransactionStatus12(TransactionStatus status, de.adorsys.psd2.model.TransactionStatus expected) {
        //When
        de.adorsys.psd2.model.TransactionStatus result = PaymentModelMapper.mapToTransactionStatus12(status);
        //Then
        assertThat(result).isEqualTo(expected);
    }


}
