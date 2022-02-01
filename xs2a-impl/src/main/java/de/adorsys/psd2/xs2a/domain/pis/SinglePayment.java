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
