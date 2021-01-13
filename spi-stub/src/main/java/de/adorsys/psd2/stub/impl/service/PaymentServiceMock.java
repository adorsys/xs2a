/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.stub.impl.service;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceMock {

    public SpiResponse<SpiPaymentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(boolean confirmationCodeValidationResult, SpiPayment payment, boolean isCancellation) {
        ScaStatus scaStatus = confirmationCodeValidationResult ? ScaStatus.FINALISED : ScaStatus.FAILED;
        TransactionStatus transactionStatus = isCancellation
                                                  ? getCancellationTransactionStatus(confirmationCodeValidationResult, payment)
                                                  : getTransactionStatus(confirmationCodeValidationResult);

        SpiPaymentConfirmationCodeValidationResponse response = new SpiPaymentConfirmationCodeValidationResponse(scaStatus, transactionStatus);

        return SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                   .payload(response)
                   .build();
    }

    private TransactionStatus getCancellationTransactionStatus(boolean confirmationCodeValidationResult, SpiPayment payment) {
        return confirmationCodeValidationResult ? TransactionStatus.CANC : payment.getPaymentStatus();
    }

    private TransactionStatus getTransactionStatus(boolean confirmationCodeValidationResult) {
        return confirmationCodeValidationResult ? TransactionStatus.ACSP : TransactionStatus.RJCT;
    }
}
