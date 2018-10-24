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

package de.adorsys.psd2.aspsp.mock.api.account;

import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.util.List;

@Data
public class AspspTransaction {
    @Id
    private final String transactionId;
    private final String entryReference;
    private final String endToEndId;
    private final String mandateId;
    private final String checkId;
    private final String creditorId;
    private final LocalDate bookingDate;
    private final LocalDate valueDate;
    private final AspspAmount spiAmount;
    private final List<AspspExchangeRate> exchangeRate;
    private final String creditorName;
    private final AspspAccountReference creditorAccount;
    private final String ultimateCreditor;
    private final String debtorName;
    private final AspspAccountReference debtorAccount;
    private final String ultimateDebtor;
    private final String remittanceInformationUnstructured;
    private final String remittanceInformationStructured;
    private final String purposeCode;
    private final String bankTransactionCodeCode;
    private final String proprietaryBankTransactionCode;
}
