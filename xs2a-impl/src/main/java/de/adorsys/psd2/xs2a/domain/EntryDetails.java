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

package de.adorsys.psd2.xs2a.domain;

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.Data;

import java.util.List;

@Data
public class EntryDetails {
    private String endToEndId;
    private String mandateId;
    private String checkId;
    private String creditorId;
    private Xs2aAmount transactionAmount;
    private List<Xs2aExchangeRate> currencyExchange;
    private String creditorName;
    private AccountReference creditorAccount;
    private String creditorAgent;
    private String ultimateCreditor;
    private String debtorName;
    private AccountReference debtorAccount;
    private String debtorAgent;
    private String ultimateDebtor;
    private String remittanceInformationUnstructured;
    private List<String> remittanceInformationUnstructuredArray;
    private String remittanceInformationStructured;
    private List<String> remittanceInformationStructuredArray;
    private PurposeCode purposeCode;
}

