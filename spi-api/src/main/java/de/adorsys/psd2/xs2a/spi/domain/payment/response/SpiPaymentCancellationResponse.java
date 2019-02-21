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

package de.adorsys.psd2.xs2a.spi.domain.payment.response;


import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import lombok.Data;

@Data
public class SpiPaymentCancellationResponse {
    /**
     * This parameter is used as a possibility to tell XS2A that authorisation for payment cancellation is needed.
     * Besides this parameter the same parameter can be set generally in ASPSP Profile.
     * If any of these two would equal <code>true</code> an authorisation shall be started for cancellation of payment.
     */
    private boolean cancellationAuthorisationMandated;
    /**
     * Provides a possiblity to update status to an actual one. If not provided status will not be updated.
     * If TransactionStatus.CANC is returned, payment will be cancelled immediately without further processing.
     */
    private TransactionStatus transactionStatus;
}
