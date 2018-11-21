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

package de.adorsys.aspsp.aspspmockserver.domain.pis;

import de.adorsys.aspsp.aspspmockserver.domain.AccountReference;
import de.adorsys.aspsp.aspspmockserver.domain.Address;
import de.adorsys.aspsp.aspspmockserver.domain.Remittance;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

@Data
public class PisPayment {
    @ApiModelProperty(value = "External Payment Id", example = "32454656712432")
    private String paymentId;

    @ApiModelProperty(value = "ASPSP Payment Id", example = "32454656712432")
    private String executionId;

    @ApiModelProperty(value = "End to end identification", example = "RI-123456789")
    private String endToEndIdentification;

    @ApiModelProperty(value = "Debtor account", required = true)
    private AccountReference debtorAccount;

    @ApiModelProperty(value = "Name of the ultimate debtor", required = true, example = "Mueller")
    private String ultimateDebtor;

    @ApiModelProperty(value = "Iso currency code", required = true, example = "EUR")
    private Currency currency;

    @ApiModelProperty(value = "Payment amount", required = true, example = "1000")
    private BigDecimal amount;

    @ApiModelProperty(value = "Creditor account", required = true)
    private AccountReference creditorAccount;

    @ApiModelProperty(value = "Creditor agent", example = "Telekom")
    private String creditorAgent;

    @ApiModelProperty(value = "Name of the creditor", required = true, example = "Telekom")
    private String creditorName;

    @ApiModelProperty(value = "Creditor Address")
    private Address creditorAddress;

    @ApiModelProperty(value = "remittance information unstructured", example = "Ref. Number TELEKOM-1222")
    private String remittanceInformationUnstructured;

    @ApiModelProperty(value = "remittance information structured")
    private Remittance remittanceInformationStructured;

    @ApiModelProperty(value = "Requested execution date", example = "2020-01-01")
    private LocalDate requestedExecutionDate;

    @ApiModelProperty(value = "Requested execution time", example = "2020-01-01T15:30:35.035Z")
    private LocalDateTime requestedExecutionTime;

    @ApiModelProperty(value = "Ultimate creditor", example = "Telekom")
    private String ultimateCreditor;

    @ApiModelProperty(value = "Purpose code", example = "BCENECEQ")
    private String purposeCode;

    /**
     * Next fields are used in order to create periodic payment
     */
    @ApiModelProperty(name = "Start date", example = "2020-01-01")
    private LocalDate startDate;

    @ApiModelProperty(name = "End date", example = "2020-03-03")
    private LocalDate endDate;

    @ApiModelProperty(name = "Execution rule", example = "latest")
    private String executionRule;

    @ApiModelProperty(name = "Frequency", example = "ANNUAL")
    private String frequency;

    @ApiModelProperty(name = "Day of execution", example = "14")
    private int dayOfExecution; //Day here max 31
}
