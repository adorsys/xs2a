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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.AccountReferenceValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Currency;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RCVD;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RJCT;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentValidationServiceTest {

    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final String PAYMENT_ID = "12345";
    private static final String IBAN = "DE15500105172295759744";
    private static final String WRONG_IBAN = "ZZ33300105172295759744";
    private static final String AMOUNT = "100";

    @InjectMocks
    private PaymentValidationService paymentValidationService;
    @Mock
    private AccountReferenceValidationService referenceValidationService;

    @Test
    public void validateSinglePayment_Success() {
        // When
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(getValidResponse());

        ResponseObject<SinglePaymentInitiationResponse> actualResponse = paymentValidationService.validateSinglePayment(getSinglePayment(IBAN, AMOUNT));

        // Then
        assertThat(actualResponse.hasError()).isFalse();
    }

    @Test
    public void validateSinglePaymentWrongIban_Error() {
        // When
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(buildFailedSinglePaymentInitiationResponse());

        ResponseObject<SinglePaymentInitiationResponse> actualResponse = paymentValidationService.validateSinglePayment(getSinglePayment(WRONG_IBAN, AMOUNT));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(PIS_400);
    }

    private SinglePayment getSinglePayment(String iban, String amountToPay) {
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

    private static AccountReference getReference(String iban) {
        AccountReference reference = new AccountReference();
        reference.setIban(iban);
        reference.setCurrency(CURRENCY);

        return reference;
    }

    private ResponseObject<BulkPaymentInitiationResponse> getValidResponse() {
        return ResponseObject.<BulkPaymentInitiationResponse>builder().body(getBulkResponses(RCVD, null)).build();
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

    private ResponseObject<SinglePaymentInitiationResponse> buildFailedSinglePaymentInitiationResponse() {
        return ResponseObject.<SinglePaymentInitiationResponse>builder().fail(PIS_400).build();
    }

}
