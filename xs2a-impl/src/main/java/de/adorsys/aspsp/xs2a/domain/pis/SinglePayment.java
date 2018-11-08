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
import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Data
@ApiModel(description = "Payment Initialisation Request", value = "SinglePayments")
public class SinglePayment implements AccountReferenceCollector {

    private String paymentId;

    @Size(max = 35)
    @ApiModelProperty(value = "end to end identification", example = "RI-123456789")
    private String endToEndIdentification;

    @NotNull
    @ApiModelProperty(value = "debtor account", required = true)
    private Xs2aAccountReference debtorAccount;

    @Deprecated // Since 1.2
    @Size(max = 70)
    @ApiModelProperty(value = "ultimate debtor", example = "Mueller")
    private String ultimateDebtor;

    @Valid
    @NotNull
    @ApiModelProperty(value = "instructed amount", required = true)
    private Xs2aAmount instructedAmount;

    @NotNull
    @ApiModelProperty(value = "creditor account", required = true)
    private Xs2aAccountReference creditorAccount;

    @ApiModelProperty(value = "creditor agent")
    private String creditorAgent;

    @NotNull
    @Size(max = 70)
    @ApiModelProperty(value = "creditor name", required = true, example = "Telekom")
    private String creditorName;

    @Valid
    @ApiModelProperty(value = "creditor Address")
    private Xs2aAddress creditorAddress;

    @Deprecated // Since 1.2
    @Size(max = 70)
    @ApiModelProperty(value = "ultimate creditor", example = "Telekom")
    private String ultimateCreditor;

    @Deprecated // Since 1.2
    @ApiModelProperty(value = "purpose code")
    private Xs2aPurposeCode purposeCode;

    @Size(max = 140)
    @ApiModelProperty(value = "remittance information unstructured", example = "Ref. Number TELEKOM-1222")
    private String remittanceInformationUnstructured;

    @Deprecated // Since 1.2
    @Valid
    @ApiModelProperty(value = "remittance information structured")
    private Remittance remittanceInformationStructured;

    @ApiModelProperty(value = "requested execution date", example = "2020-01-01")
    private LocalDate requestedExecutionDate;

    @ApiModelProperty(value = "requested execution time", example = "2020-01-01T15:30:35.035Z")
    private OffsetDateTime requestedExecutionTime;

    @ApiModelProperty(value = "Transaction status", example = "Pending")
    private TransactionStatus transactionStatus;

    @JsonIgnore
    @Override
    public Set<Xs2aAccountReference> getAccountReferences() {
        return new HashSet<>(Arrays.asList(this.debtorAccount, this.creditorAccount));
    }
}
