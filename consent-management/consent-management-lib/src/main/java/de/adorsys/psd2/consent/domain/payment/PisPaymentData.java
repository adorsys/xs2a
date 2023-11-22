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
public class PisPaymentData extends InstanceDependableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_payment_data_generator")
    @SequenceGenerator(name = "pis_payment_data_generator", sequenceName = "pis_payment_data_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "end_to_end_identification")
    private String endToEndIdentification;

    @Column(name = "instruction_identification")
    private String instructionIdentification;

    @JoinColumn(name = "debtor_acc_reference_id")
    @ManyToOne(cascade = CascadeType.ALL)
    private AccountReferenceEntity debtorAccount;

    @Column(name = "ultimate_debtor")
    private String ultimateDebtor;

    @Column(name = "currency", nullable = false)
    private Currency currency;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @JoinColumn(name = "creditor_acc_reference_id")
    @ManyToOne(cascade = CascadeType.ALL)
    private AccountReferenceEntity creditorAccount;

    @Column(name = "creditor_agent")
    private String creditorAgent;

    @Column(name = "creditor_name", nullable = false)
    private String creditorName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private PisAddress creditorAddress;

    @Column(name = "remittance_info_unstruct")
    private String remittanceInformationUnstructured;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "remittance_id")
    private PisRemittance remittanceInformationStructured;

    @Column(name = "requested_execution_date")
    private LocalDate requestedExecutionDate;

    @Column(name = "requested_execution_time")
    private OffsetDateTime requestedExecutionTime;

    @Column(name = "ultimate_creditor")
    private String ultimateCreditor;

    @Column(name = "purpose_code")
    private String purposeCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "execution_rule")
    private PisExecutionRule executionRule;

    @Column(name = "end_date")
    private LocalDate endDate;

    private String frequency;

    @Column(name = "day_of_execution")
    private PisDayOfExecution dayOfExecution;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "common_payment_id", nullable = false)
    private PisCommonPaymentData paymentData;

    @Column(name = "batch_booking_preferred")
    private Boolean batchBookingPreferred;
}
