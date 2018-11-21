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

package de.adorsys.aspsp.xs2a.spi.domain.payment;

import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @deprecated since 1.8. Will be removed in 1.10
 * @see de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment
 */
@Data
public class SpiBulkPayment {
    private String paymentId;

    private Boolean batchBookingPreferred;

    private SpiAccountReference debtorAccount;

    private LocalDate requestedExecutionDate;

    List<SpiSinglePayment> payments;

    private SpiTransactionStatus paymentStatus;
}
