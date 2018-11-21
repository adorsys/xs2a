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

package de.adorsys.psd2.xs2a.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "Balance Information", value = "Balance")
public class Xs2aBalance {

    @ApiModelProperty(value = "balance amount", required = true)
    private Xs2aAmount balanceAmount;

    @ApiModelProperty(value = "balance type", required = true)
    private BalanceType balanceType;

    @ApiModelProperty(value = "This data element might be used to indicate e.g. with the expected or booked balance that no action is known on the account, which is not yet booked.", example = "2017-10-25T15:30:35.035")
    private LocalDateTime lastChangeDateTime;

    @ApiModelProperty(value = "Reference date of the balance", example = "2017-03-26")
    private LocalDate referenceDate;

    @ApiModelProperty(value = "entryReference of the last commited transaction to support the TPP in identifying whether all PSU transactions are already known.")
    @Size(max = 35)
    private String lastCommittedTransaction;
}
