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

package de.adorsys.aspsp.xs2a.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

@Data
@Embeddable
@ApiModel(description = "pis payment entity", value = "PisPaymentData")
public class PisPaymentData {

    @Column(name = "end_to_end_identification")
    @ApiModelProperty(value = "End to end authentication", example = "RI-123456789")
    private String endToEndIdentification;

    @Column(name = "debtor_iban", nullable = false)
    @ApiModelProperty(value = "Iban of the debtor", required = true, example = "DE2310010010123")
    private String debtorIban;

    @Column(name = "ultimate_debtor", nullable = false)
    @ApiModelProperty(value = "Name of the ultimate debtor", required = true, example = "Mueller")
    private String ultimateDebtor;

    @Column(name = "currency", nullable = false)
    @ApiModelProperty(value = "Iso currency code", required = true, example = "EUR")
    private Currency currency;

    @Column(name = "balanceAmount", nullable = false)
    @ApiModelProperty(value = "Payment balanceAmount", required = true, example = "1000")
    private BigDecimal amount;

    @Column(name = "creditor_iban", nullable = false)
    @ApiModelProperty(value = "Iban of the creditor", required = true, example = "DE2310010010123")
    private String creditorIban;

    @Column(name = "creditor_agent", nullable = false)
    @ApiModelProperty(value = "Creditor agent", required = true, example = "Telekom")
    private String creditorAgent;

    @Column(name = "creditor_name", nullable = false)
    @ApiModelProperty(value = "Name of the creditor", required = true, example = "Telekom")
    private String creditorName;

    @Column(name = "requested_execution_date", nullable = false)
    @ApiModelProperty(value = "Requested execution referenceDate", required = true, example = "2017-01-01")
    private LocalDate requestedExecutionDate;

    @Column(name = "requested_execution_time", nullable = false)
    @ApiModelProperty(value = "Requested execution time", required = true, example = "2017-10-25T15:30:35.035")
    private LocalDateTime requestedExecutionTime;

    @Column(name = "ultimate_creditor")
    @ApiModelProperty(value = "Ultimate creditor", example = "Telekom")
    private String ultimateCreditor;

    @Column(name = "purpose_code")
    @ApiModelProperty(value = "Purpose code", example = "BCENECEQ")
    private String purposeCode;
}
