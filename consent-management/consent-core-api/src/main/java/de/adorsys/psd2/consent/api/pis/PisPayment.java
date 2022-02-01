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

package de.adorsys.psd2.consent.api.pis;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;

@Data
public class PisPayment {
    @ApiModelProperty(value = "External Payment Id", example = "32454656712432")
    private String paymentId;

    @ApiModelProperty(value = "ASPSP Payment Id", example = "32454656712432")
    private String executionId;

    @ApiModelProperty(value = "End to end identification", example = "RI-123456789")
    private String endToEndIdentification;

    @ApiModelProperty(value = "Instruction identification", example = "ABC/4562/2020-01-10")
    private String instructionIdentification;

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
    private CmsAddress creditorAddress;

    @ApiModelProperty(value = "remittance information unstructured", example = "Ref. Number TELEKOM-1222")
    private String remittanceInformationUnstructured;

    @ApiModelProperty(value = "remittance information structured")
    private CmsRemittance remittanceInformationStructured;

    @ApiModelProperty(value = "Requested execution date", example = "2020-01-01")
    private LocalDate requestedExecutionDate;

    @ApiModelProperty(value = "Requested execution time", example = "2020-01-01T15:30:35.035Z")
    private OffsetDateTime requestedExecutionTime;

    @ApiModelProperty(value = "Ultimate creditor", example = "Telekom")
    private String ultimateCreditor;

    @ApiModelProperty(value = "Purpose code", example = "BCENECEQ")
    private String purposeCode;

    @ApiModelProperty(name = "transactionStatus", example = "ACCP", required = true)
    private TransactionStatus transactionStatus;

    /**
     * Next fields are used in order to create periodic payment
     */
    @ApiModelProperty(name = "Start date", example = "2020-01-01")
    private LocalDate startDate;

    @ApiModelProperty(name = "End date", example = "2020-03-03")
    private LocalDate endDate;

    @ApiModelProperty(name = "Execution rule", example = "following")
    private PisExecutionRule executionRule;

    @ApiModelProperty(name = "Frequency", example = "ANNUAL")
    private String frequency;

    @ApiModelProperty(name = "Day of execution", example = "14")
    private PisDayOfExecution dayOfExecution; //Day here max 31

    @ApiModelProperty(value = "Timestamp of the last payment transaction status changing")
    private OffsetDateTime statusChangeTimestamp;

    private List<PsuIdData> psuDataList;

    @ApiModelProperty(value = "Batch booking preferred")
    private Boolean batchBookingPreferred;

    @ApiModelProperty(value = "Timestamp of the payment creation")
    private OffsetDateTime creationTimestamp;
}
