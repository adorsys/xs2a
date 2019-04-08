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

package de.adorsys.psd2.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.AccountReferenceCollector;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.code.Xs2aPurposeCode;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class SinglePayment implements AccountReferenceCollector {
    private String paymentId;
    @Size(max = 35)
    private String endToEndIdentification;

    @NotNull
    private AccountReference debtorAccount;

    @Deprecated // Since 1.2
    @Size(max = 70)
    private String ultimateDebtor;

    @Valid
    @NotNull
    private Xs2aAmount instructedAmount;

    @NotNull
    private AccountReference creditorAccount;

    private String creditorAgent;

    @NotNull
    @Size(max = 70)
    private String creditorName;

    @Valid
    private Xs2aAddress creditorAddress;

    @Deprecated // Since 1.2
    @Size(max = 70)
    private String ultimateCreditor;

    @Deprecated // Since 1.2
    private Xs2aPurposeCode purposeCode;

    @Size(max = 140)
    private String remittanceInformationUnstructured;

    @Deprecated // Since 1.2
    @Valid
    private Remittance remittanceInformationStructured;

    private LocalDate requestedExecutionDate;

    private OffsetDateTime requestedExecutionTime;

    private TransactionStatus transactionStatus;

    private List<PsuIdData> psuDataList;

    private OffsetDateTime statusChangeTimestamp;

    @JsonIgnore
    @Override
    public Set<AccountReference> getAccountReferences() {
        return new HashSet<>(Arrays.asList(this.debtorAccount, this.creditorAccount));
    }
}
