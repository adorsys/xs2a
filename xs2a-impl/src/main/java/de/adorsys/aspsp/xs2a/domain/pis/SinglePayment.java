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

package de.adorsys.aspsp.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.aspsp.xs2a.domain.AccountReferenceCollector;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
@ApiModel(description = "Payment Initialisation Request", value = "SinglePayments")
public class SinglePayment implements AccountReferenceCollector {

    @Size(max = 35)
    @ApiModelProperty(value = "end to end identification", example = "RI-123456789")
    private String endToEndIdentification;

    @NotNull
    @ApiModelProperty(value = "debtor account", required = true)
    private AccountReference debtorAccount;

    @Deprecated // Since 1.2
    @Size(max = 70)
    @ApiModelProperty(value = "ultimate debtor", example = "Mueller")
    private String ultimateDebtor;

    @Valid
    @NotNull
    @ApiModelProperty(value = "instructed amount", required = true)
    private Amount instructedAmount;

    @NotNull
    @ApiModelProperty(value = "creditor account", required = true)
    private AccountReference creditorAccount;

    @ApiModelProperty(value = "creditor agent")
    private BICFI creditorAgent;

    @NotNull
    @Size(max = 70)
    @ApiModelProperty(value = "creditor name", required = true, example = "Telekom")
    private String creditorName;

    @Valid
    @ApiModelProperty(value = "creditor Address")
    private Address creditorAddress;

    @Deprecated // Since 1.2
    @Size(max = 70)
    @ApiModelProperty(value = "ultimate creditor", example = "Telekom")
    private String ultimateCreditor;

    @Deprecated // Since 1.2
    @ApiModelProperty(value = "purpose code")
    private PurposeCode purposeCode;

    @Size(max = 140)
    @ApiModelProperty(value = "remittance information unstructured", example = "Ref. Number TELEKOM-1222")
    private String remittanceInformationUnstructured;

    @Deprecated // Since 1.2
    @Valid
    @ApiModelProperty(value = "remittance information structured")
    private Remittance remittanceInformationStructured;

    @Deprecated // Since 1.2
    @ApiModelProperty(value = "requested execution date", example = "2020-01-01")
    private LocalDate requestedExecutionDate;

    @Deprecated // Since 1.2
    @ApiModelProperty(value = "requested execution time", example = "2020-01-01T15:30:35.035Z")
    private LocalDateTime requestedExecutionTime;

    @JsonIgnore
    public boolean isValidExecutionDateAndTime() { //TODO Should be removed with https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/167
        return Optional.ofNullable(this.requestedExecutionDate)
                   .map(d -> d.isEqual(LocalDate.now()) || d.isAfter(LocalDate.now()))
                   .orElse(true)
                   &&
                   Optional.ofNullable(this.requestedExecutionTime)
                       .map(d -> d.isAfter(LocalDate.now().atTime(0, 0)))
                       .orElse(true);
    }

    @JsonIgnore
    @Override
    public Set<AccountReference> getAccountReferences() {
        return new HashSet<>(Arrays.asList(this.debtorAccount, this.creditorAccount));
    }
}
