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

package de.adorsys.psd2.xs2a.spi.domain.payment.response;

import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiTransactionStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A response object that is returned by the ASPSP after the successful execution of payment
 */
@EqualsAndHashCode(callSuper = true)
public final class SpiPaymentExecutionResponse extends SpiPaymentResponse {
    @Getter
    private SpiTransactionStatus transactionStatus;

    public SpiPaymentExecutionResponse(SpiTransactionStatus transactionStatus) {
        this(null, transactionStatus);
    }

    public SpiPaymentExecutionResponse(SpiAuthorisationStatus spiAuthorisationStatus) {
        this(spiAuthorisationStatus, null);
    }

    public SpiPaymentExecutionResponse(SpiAuthorisationStatus spiAuthorisationStatus,
                                       SpiTransactionStatus transactionStatus) {
        super(spiAuthorisationStatus);
        this.transactionStatus = transactionStatus;
    }
}
