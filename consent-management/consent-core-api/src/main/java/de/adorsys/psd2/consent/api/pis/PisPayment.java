/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.api.pis;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;

@Data
public class PisPayment {
    @Schema(description = "External Payment Id", example = "32454656712432")
    private String paymentId;

    @Schema(description = "ASPSP Payment Id", example = "32454656712432")
    private String executionId;

    @Schema(description = "End to end identification", example = "RI-123456789")
    private String endToEndIdentification;

    @Schema(description = "Instruction identification", example = "ABC/4562/2020-01-10")
    private String instructionIdentification;

    @Schema(description = "Debtor account", required = true)
    private AccountReference debtorAccount;

    @Schema(description = "Name of the ultimate debtor", required = true, example = "Mueller")
    private String ultimateDebtor;

    @Schema(description = "Iso currency code", required = true, example = "EUR")
    private Currency currency;

    @Schema(description = "Payment amount", required = true, example = "1000")
    private BigDecimal amount;

    @Schema(description = "Creditor account", required = true)
    private AccountReference creditorAccount;

    @Schema(description = "Creditor agent", example = "Telekom")
    private String creditorAgent;

    @Schema(description = "Name of the creditor", required = true, example = "Telekom")
    private String creditorName;

    @Schema(description = "Creditor Address")
    private CmsAddress creditorAddress;

    @Schema(description = "Remittance information unstructured", example = "Ref. Number TELEKOM-1222")
    private String remittanceInformationUnstructured;

    @Schema(description = "List of remittance information unstructured", example = "[Ref. Number TELEKOM-1222]")
    private List<String> remittanceInformationUnstructuredArray;

    @Schema(description = "Remittance information structured")
    private CmsRemittance remittanceInformationStructured;

    @Schema(description = "List of remittance information structured")
    private List<CmsRemittance> remittanceInformationStructuredArray;

    @Schema(description = "Requested execution date", example = "2020-01-01")
    private LocalDate requestedExecutionDate;

    @Schema(description = "Requested execution time", example = "2020-01-01T15:30:35.035Z")
    private OffsetDateTime requestedExecutionTime;

    @Schema(description = "Ultimate creditor", example = "Telekom")
    private String ultimateCreditor;

    @Schema(description = "Purpose code", example = "BCENECEQ")
    private String purposeCode;

    @Schema(name = "transactionStatus", example = "ACCP", required = true)
    private TransactionStatus transactionStatus;

    /**
     * Next fields are used in order to create periodic payment
     */
    @Schema(name = "Start date", example = "2020-01-01")
    private LocalDate startDate;

    @Schema(name = "End date", example = "2020-03-03")
    private LocalDate endDate;

    @Schema(name = "Execution rule", example = "following")
    private PisExecutionRule executionRule;

    @Schema(name = "Frequency", example = "ANNUAL")
    private String frequency;

    @Schema(name = "Day of execution", example = "14")
    private PisDayOfExecution dayOfExecution; //Day here max 31

    @Schema(description = "Timestamp of the last payment transaction status changing")
    private OffsetDateTime statusChangeTimestamp;

    private List<PsuIdData> psuDataList;

    @Schema(description = "Batch booking preferred")
    private Boolean batchBookingPreferred;

    @Schema(description = "Timestamp of the payment creation")
    private OffsetDateTime creationTimestamp;
}
