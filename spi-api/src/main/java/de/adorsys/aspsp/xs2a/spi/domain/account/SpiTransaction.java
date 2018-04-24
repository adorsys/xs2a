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

package de.adorsys.aspsp.xs2a.spi.domain.account;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import lombok.Data;

import java.util.Date;

@Data
public class SpiTransaction {
    private final String transactionId;
    private final String endToEndId;
    private final String mandateId;
    private final String creditorId;
    private final Date bookingDate;
    private final Date valueDate;
    private final SpiAmount spiAmount;
    private final String creditorName;
    private final SpiAccountReference creditorAccount;
    private final String ultimateCreditor;
    private final String debtorName;
    private final SpiAccountReference debtorAccount;
    private final String ultimateDebtor;
    private final String remittanceInformationUnstructured;
    private final String remittanceInformationStructured;
    private final String purposeCode;
    private final String bankTransactionCodeCode;

}
