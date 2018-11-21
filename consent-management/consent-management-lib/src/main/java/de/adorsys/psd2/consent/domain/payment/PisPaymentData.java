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

package de.adorsys.psd2.consent.domain.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Currency;

@Data
@ToString(exclude = "consent")
@Entity(name = "pis_payment_data")
@ApiModel(description = "pis payment entity", value = "PisPaymentData")
public class PisPaymentData {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_payment_data_generator")
    @SequenceGenerator(name = "pis_payment_data_generator", sequenceName = "pis_payment_data_id_seq")
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "end_to_end_identification")
    @ApiModelProperty(value = "End to end identification", example = "RI-123456789")
    private String endToEndIdentification;

    @JoinColumn(name = "debtor_acc_reference_id")
    @ManyToOne(cascade = CascadeType.ALL)
    @ApiModelProperty(value = "Debtor account", required = true)
    private AccountReferenceEntity debtorAccount;

    @Column(name = "ultimate_debtor")
    @ApiModelProperty(value = "Name of the ultimate debtor", example = "Mueller")
    private String ultimateDebtor;

    @Column(name = "currency", nullable = false)
    @ApiModelProperty(value = "Iso currency code", required = true, example = "EUR")
    private Currency currency;

    @Column(name = "amount", nullable = false)
    @ApiModelProperty(value = "Payment amount", required = true, example = "1000")
    private BigDecimal amount;

    @JoinColumn(name = "creditor_acc_reference_id")
    @ManyToOne(cascade = CascadeType.ALL)
    @ApiModelProperty(value = "Creditor account", required = true)
    private AccountReferenceEntity creditorAccount;

    @Column(name = "creditor_agent")
    @ApiModelProperty(value = "Creditor agent", example = "Telekom")
    private String creditorAgent;

    @Column(name = "creditor_name", nullable = false)
    @ApiModelProperty(value = "Name of the creditor", required = true, example = "Telekom")
    private String creditorName;

    @OneToOne(cascade = CascadeType.ALL)
    @ApiModelProperty(value = "Creditor Address")
    @JoinColumn(name = "address_id")
    private PisAddress creditorAddress;

    @Column(name = "remittance_info_unstruct")
    @ApiModelProperty(value = "remittance information unstructured", example = "Ref. Number TELEKOM-1222")
    private String remittanceInformationUnstructured;

    @OneToOne(cascade = CascadeType.ALL)
    @ApiModelProperty(value = "remittance information structured")
    @JoinColumn(name = "remittance_id")
    private PisRemittance remittanceInformationStructured;

    @Column(name = "requested_execution_date")
    @ApiModelProperty(value = "Requested execution date", example = "2020-01-01")
    private LocalDate requestedExecutionDate;

    @Column(name = "requested_execution_time")
    @ApiModelProperty(value = "Requested execution time", example = "2020-01-01T15:30:35.035Z")
    private OffsetDateTime requestedExecutionTime;

    @Column(name = "ultimate_creditor")
    @ApiModelProperty(value = "Ultimate creditor", example = "Telekom")
    private String ultimateCreditor;

    @Column(name = "purpose_code")
    @ApiModelProperty(value = "Purpose code", example = "BCENECEQ")
    private String purposeCode;

    @Column(name = "start_date")
    @ApiModelProperty(name = "startDate", example = "2020-01-01")
    private LocalDate startDate;

    @Column(name = "execution_rule")
    @ApiModelProperty(name = "Execution rule", example = "latest")
    private String executionRule;

    @Column(name = "end_date")
    @ApiModelProperty(name = "endDate", example = "2020-03-03")
    private LocalDate endDate;

    @ApiModelProperty(name = "frequency", example = "ANNUAL")
    private String frequency;

    @Column(name = "day_of_execution")
    @ApiModelProperty(name = "dayOfExecution", example = "14")
    private int dayOfExecution;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", nullable = false)
    @ApiModelProperty(value = "Detailed information about consent", required = true)
    private PisConsent consent;

    @Column(name = "transaction_status")
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(name = "transactionStatus", example = "ACCP")
    private TransactionStatus transactionStatus;
}
