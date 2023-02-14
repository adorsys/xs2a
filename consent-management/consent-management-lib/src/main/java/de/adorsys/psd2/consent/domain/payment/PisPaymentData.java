/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.domain.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Currency;

@Data
@ToString(exclude = "paymentData")
@Entity(name = "pis_payment_data")
@Schema(description = "pis payment entity", name = "PisPaymentData")
public class PisPaymentData extends InstanceDependableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_payment_data_generator")
    @SequenceGenerator(name = "pis_payment_data_generator", sequenceName = "pis_payment_data_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "end_to_end_identification")
    @Schema(description = "End to end identification", example = "RI-123456789")
    private String endToEndIdentification;

    @Column(name = "instruction_identification")
    private String instructionIdentification;

    @JoinColumn(name = "debtor_acc_reference_id")
    @ManyToOne(cascade = CascadeType.ALL)
    @Schema(description = "Debtor account", required = true)
    private AccountReferenceEntity debtorAccount;

    @Column(name = "ultimate_debtor")
    @Schema(description = "Name of the ultimate debtor", example = "Mueller")
    private String ultimateDebtor;

    @Column(name = "currency", nullable = false)
    @Schema(description = "Iso currency code", required = true, example = "EUR")
    private Currency currency;

    @Column(name = "amount", nullable = false)
    @Schema(description = "Payment amount", required = true, example = "1000")
    private BigDecimal amount;

    @JoinColumn(name = "creditor_acc_reference_id")
    @ManyToOne(cascade = CascadeType.ALL)
    @Schema(description = "Creditor account", required = true)
    private AccountReferenceEntity creditorAccount;

    @Column(name = "creditor_agent")
    @Schema(description = "Creditor agent", example = "Telekom")
    private String creditorAgent;

    @Column(name = "creditor_name", nullable = false)
    @Schema(description = "Name of the creditor", required = true, example = "Telekom")
    private String creditorName;

    @OneToOne(cascade = CascadeType.ALL)
    @Schema(description = "Creditor Address")
    @JoinColumn(name = "address_id")
    private PisAddress creditorAddress;

    @Column(name = "remittance_info_unstruct")
    @Schema(description = "remittance information unstructured", example = "Ref. Number TELEKOM-1222")
    private String remittanceInformationUnstructured;

    @OneToOne(cascade = CascadeType.ALL)
    @Schema(description = "remittance information structured")
    @JoinColumn(name = "remittance_id")
    private PisRemittance remittanceInformationStructured;

    @Column(name = "requested_execution_date")
    @Schema(description = "Requested execution date", example = "2020-01-01")
    private LocalDate requestedExecutionDate;

    @Column(name = "requested_execution_time")
    @Schema(description = "Requested execution time", example = "2020-01-01T15:30:35.035Z")
    private OffsetDateTime requestedExecutionTime;

    @Column(name = "ultimate_creditor")
    @Schema(description = "Ultimate creditor", example = "Telekom")
    private String ultimateCreditor;

    @Column(name = "purpose_code")
    @Schema(description = "Purpose code", example = "BCENECEQ")
    private String purposeCode;

    @Column(name = "start_date")
    @Schema(name = "startDate", example = "2020-01-01")
    private LocalDate startDate;

    @Column(name = "execution_rule")
    @Schema(name = "Execution rule", example = "following")
    private PisExecutionRule executionRule;

    @Column(name = "end_date")
    @Schema(name = "endDate", example = "2020-03-03")
    private LocalDate endDate;

    @Schema(name = "frequency", example = "ANNUAL")
    private String frequency;

    @Column(name = "day_of_execution")
    @Schema(name = "dayOfExecution", example = "14")
    private PisDayOfExecution dayOfExecution;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "common_payment_id", nullable = false)
    @Schema(description = "Detailed information about payment", required = true)
    private PisCommonPaymentData paymentData;

    @Column(name = "batch_booking_preferred")
    @Schema(name = "Batch booking preferred", example = "true")
    private Boolean batchBookingPreferred;
}
