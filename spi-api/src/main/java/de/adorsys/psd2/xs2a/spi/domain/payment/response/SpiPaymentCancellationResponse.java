/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.spi.domain.payment.response;


import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpiPaymentCancellationResponse extends SpiPaymentResponse {
    /**
     * This parameter is used as a possibility to tell XS2A that authorisation for payment cancellation is needed.
     * Besides this parameter the same parameter can be set generally in ASPSP Profile.
     * If any of these two would equal <code>true</code> an authorisation shall be started for cancellation of payment.
     */
    private boolean cancellationAuthorisationMandated;
    /**
     * Provides a possibility to update status to an actual one. If not provided status will not be updated.
     * If TransactionStatus.CANC is returned, payment will be cancelled immediately without further processing.
     */
    private TransactionStatus transactionStatus;
}
