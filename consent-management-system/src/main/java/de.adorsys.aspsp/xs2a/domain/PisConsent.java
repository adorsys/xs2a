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

import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

@Data
@Entity(name = "pis_consent")
public class PisConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_consent_generator")
    @SequenceGenerator(name = "pis_consent_generator", sequenceName = "pis_consent_id_seq")
    private Long id;
    @Column(name = "external_id", nullable = false)
    private String externalId;
    @Column(name = "end_to_end_identification")
    private String endToEndIdentification;
    @Column(name = "debtor_iban", nullable = false)
    private String debtorIban;
    @Column(name = "ultimate_debtor", nullable = false)
    private String ultimateDebtor;
    @Column(name = "currency", nullable = false)
    private Currency currency;
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "creditor_iban", nullable = false)
    private String creditorIban;
    @Column(name = "creditor_agent", nullable = false)
    private String creditorAgent;
    @Column(name = "creditor_name", nullable = false)
    private String creditorName;
    @Column(name = "requested_execution_date", nullable = false)
    private Date requestedExecutionDate;
    @Column(name = "requested_execution_time", nullable = false)
    private Date requestedExecutionTime;
    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private SpiConsentStatus consentStatus;
    @Column(name = "consent_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ConsentType consentType = ConsentType.PIS;
    @Column(name = "ultimate_creditor")
    private String ultimateCreditor;
    @Column(name = "purpose_code")
    private String purposeCode;
    @Column(name = "remittance_information_unstructured")
    private String remittanceInformationUnstructured;
}
