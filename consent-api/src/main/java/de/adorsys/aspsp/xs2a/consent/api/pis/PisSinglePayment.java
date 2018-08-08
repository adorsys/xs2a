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

package de.adorsys.aspsp.xs2a.consent.api.pis;

import de.adorsys.aspsp.xs2a.consent.api.AccountReference;
import de.adorsys.aspsp.xs2a.consent.api.Address;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;


@Data
public class PisSinglePayment {
    @ApiModelProperty(value = "Payment Id", example = "32454656712432")
    private String paymentId;
    @ApiModelProperty(value = "End to end authentication", example = "RI-123456789")
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
    @ApiModelProperty(value = "Creditor agent", required = true, example = "Telekom")
    private String creditorAgent;
    @ApiModelProperty(value = "Name of the creditor", required = true, example = "Telekom")
    private String creditorName;
    @ApiModelProperty(value = "Creditor Address")
    private Address creditorAddress;
    @ApiModelProperty(value = "remittance information unstructured", example = "Ref. Number TELEKOM-1222")
    private String remittanceInformationUnstructured;
    @ApiModelProperty(value = "remittance information structured", example = "Ref. Number TELEKOM-1222")
    private String remittanceInformationStructured;
    @ApiModelProperty(value = "Requested execution date", required = true, example = "2017-01-01")
    private LocalDate requestedExecutionDate;
    @ApiModelProperty(value = "Requested execution time", required = true, example = "2017-10-25T15:30:35.035")
    private LocalDateTime requestedExecutionTime;
    @ApiModelProperty(value = "Ultimate creditor", example = "Telekom")
    private String ultimateCreditor;
    @ApiModelProperty(value = "Purpose code", example = "BCENECEQ")
    private String purposeCode;
}
