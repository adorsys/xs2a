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
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class TransactionInfo {
    @Size(max = 70)
    private final String creditorName;
    private final AccountReference creditorAccount;
    private final String creditorAgent;
    @Size(max = 70)
    private final String ultimateCreditor;
    private final String debtorName;
    private final AccountReference debtorAccount;
    private final String debtorAgent;
    @Size(max = 70)
    private final String ultimateDebtor;
    @Size(max = 140)
    private final String remittanceInformationUnstructured;
    private final List<String> remittanceInformationUnstructuredArray;
    @Size(max = 140)
    private final String remittanceInformationStructured;
    private final List<String> remittanceInformationStructuredArray;
    private final PurposeCode purposeCode;
}
