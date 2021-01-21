/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.model.ChargeBearer;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.AccountReferenceCollector;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SinglePayment extends CommonPayment implements AccountReferenceCollector {

    private String endToEndIdentification;
    private String instructionIdentification;

    @NotNull
    private AccountReference debtorAccount;

    private String ultimateDebtor;

    @Valid
    @NotNull
    private Xs2aAmount instructedAmount;

    @NotNull
    private AccountReference creditorAccount;

    private String creditorAgent;

    @NotNull
    private String creditorName;

    @Valid
    private Xs2aAddress creditorAddress;

    private String ultimateCreditor;

    private PurposeCode purposeCode;

    @Size(max = 140)
    private String remittanceInformationUnstructured;

    @Valid
    private String remittanceInformationStructured;

    private List<String> remittanceInformationStructuredArray;

    private LocalDate requestedExecutionDate;

    private OffsetDateTime requestedExecutionTime;

    private String debtorName;

    private ChargeBearer chargeBearer;
    
    @JsonIgnore
    @Override
    public Set<AccountReference> getAccountReferences() {
        return new HashSet<>(Arrays.asList(this.debtorAccount, this.creditorAccount));
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.SINGLE;
    }
}
