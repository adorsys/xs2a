/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.stub.impl.service;

import de.adorsys.psd2.xs2a.spi.domain.payment.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaStatus;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceMock {

    public SpiResponse<SpiPaymentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(boolean confirmationCodeValidationResult, SpiPayment payment, boolean isCancellation) {
        SpiScaStatus scaStatus = confirmationCodeValidationResult
                                     ? SpiScaStatus.FINALISED
                                     : SpiScaStatus.FAILED;
        SpiTransactionStatus transactionStatus = isCancellation
                                                     ? getCancellationTransactionStatus(confirmationCodeValidationResult, payment)
                                                     : getTransactionStatus(confirmationCodeValidationResult);

        SpiPaymentConfirmationCodeValidationResponse response = new SpiPaymentConfirmationCodeValidationResponse(scaStatus, transactionStatus);

        return SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                   .payload(response)
                   .build();
    }

    private SpiTransactionStatus getCancellationTransactionStatus(boolean confirmationCodeValidationResult, SpiPayment payment) {
        return confirmationCodeValidationResult
                   ? SpiTransactionStatus.CANC
                   : payment.getPaymentStatus();
    }

    private SpiTransactionStatus getTransactionStatus(boolean confirmationCodeValidationResult) {
        return confirmationCodeValidationResult
                   ? SpiTransactionStatus.ACSP
                   : SpiTransactionStatus.RJCT;
    }
}
