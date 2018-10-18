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

import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel(description = "BulkPayment Initialisation Request", value = "BulkPayment")
public class BulkPayment {
    @ApiModelProperty(value = "The unique identifier of the payment", required = true)
    private String paymentId;

    @ApiModelProperty(value = "If this element equals \"true\", the PSU prefers only one booking entry. If this element equals \"false\", the PSU prefers individual booking of all contained individual transactions. The ASPSP will follow this preference according to contracts agreed on with the PSU.", example = "true")
    private Boolean batchBookingPreferred;

    @NotNull
    @ApiModelProperty(value = "debtor account", required = true)
    private Xs2aAccountReference debtorAccount;

    @ApiModelProperty(value = "requested execution date", example = "2020-01-01")
    private LocalDate requestedExecutionDate;

    @ApiModelProperty(value = "The Bulk Entry Type is a type which follows the JSON formats for the supported products for single payments, see Section 11.1, excluding the data elements\n" +
                                  "\uF0B7 debtorAccount,\n" +
                                  "\uF0B7 requestedExecutionDate,\n" +
                                  "\uF0B7 requestedExecutionTime.\n" +
                                  "These three data elements may not be contained in any bulk entry.", required = true)
    List<SinglePayment> payments;

    @ApiModelProperty(value = "Transaction status", example = "Pending")
    private Xs2aTransactionStatus transactionStatus;
}
