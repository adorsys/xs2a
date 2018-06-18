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

import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "Payment Initialisation Request", value = "SinglePayments")
public class SinglePayments {

    @ApiModelProperty(value = "end to end authentication", example = "RI-123456789")
    @Size(max = 35)
    private String endToEndIdentification;

    @ApiModelProperty(value = "debtor account", required = true)
    private AccountReference debtorAccount;

    @ApiModelProperty(value = "ultimate debtor", required = false, example = "Mueller")
    @Size(max = 70)
    private String ultimateDebtor;

    @ApiModelProperty(value = "instructed amount", required = true)
    private Amount instructedAmount;

    @ApiModelProperty(value = "creditor account", required = true)
    private AccountReference creditorAccount;

    @ApiModelProperty(value = "creditor agent", required = false)
    private BICFI creditorAgent;

    @ApiModelProperty(value = "creditor name", required = true, example = "Telekom")
    @Size(max = 70)
    private String creditorName;

    @ApiModelProperty(value = "creditor Address", required = false)
    private Address creditorAddress;

    @ApiModelProperty(value = "ultimate creditor", required = false, example = "Telekom")
    @Size(max = 70)
    private String ultimateCreditor;

    @ApiModelProperty(value = "purpose code", required = false)
    private PurposeCode purposeCode;

    @ApiModelProperty(value = "remittance information unstructured", required = false, example = "Ref. Number TELEKOM-1222")
    @Size(max = 140)
    private String remittanceInformationUnstructured;

    @ApiModelProperty(value = "remittance information structured", required = false)
    private Remittance remittanceInformationStructured;

    @ApiModelProperty(value = "requested execution date", required = false, example = "2017-01-01")
    private LocalDate requestedExecutionDate;

    @ApiModelProperty(value = "requested execution time", required = false, example = "2017-10-25T15:30:35.035")
    private LocalDateTime requestedExecutionTime;

}
