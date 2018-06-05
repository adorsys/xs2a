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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

@Data
@Entity(name = "pis_consent")
@ApiModel(description = "Pis consent entity", value = "PisConsent")
public class PisConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_consent_generator")
    @SequenceGenerator(name = "pis_consent_generator", sequenceName = "pis_consent_id_seq")
    private Long id;

    @Column(name = "external_id", nullable = false)
    @ApiModelProperty(value = "Id of the created consent for the given accounts and accesses", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @Column(name = "end_to_end_identification")
    @ApiModelProperty(value = "End to end authentication of the psu", example = "RI-123456789")
    private String endToEndIdentification;

    @Column(name = "debtor_iban", nullable = false)
    @ApiModelProperty(value = "Iban of the debtor", required = true, example = "DE2310010010123")
    private String debtorIban;

    @Column(name = "ultimate_debtor", nullable = false)
    @ApiModelProperty(value = "Name of the ultimate debtor", required = true, example = "Mueller")
    private String ultimateDebtor;

    @Column(name = "currency", nullable = false)
    @ApiModelProperty(value = "Currency Type", required = true, example = "EUR")
    private Currency currency;

    @Column(name = "amount", nullable = false)
    @ApiModelProperty(value = "Payment amount", required = true, example = "1000")
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
    @ApiModelProperty(value = "Requested execution date", required = true, example = "2017-01-01")
    private Date requestedExecutionDate;

    @Column(name = "requested_execution_time", nullable = false)
    @ApiModelProperty(value = "Requested execution time", required = true, example = "2017-10-25T15:30:35.035Z")
    private Date requestedExecutionTime;

    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP by more values.", required = true, example = "VALID")
    private SpiConsentStatus consentStatus;

    @Column(name = "consent_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the consent: AIS or PIS.", required = true, example = "AIS")
    private ConsentType consentType = ConsentType.PIS;

    @Column(name = "ultimate_creditor")
    @ApiModelProperty(value = "Ultimate creaditor", example = "Telekom")
    private String ultimateCreditor;

    @Column(name = "purpose_code")
    @ApiModelProperty(value = "Purpose code", example = "BCENECEQ")
    private String purposeCode;
}
