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

import de.adorsys.psd2.consent.api.ConsentType;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "pis_consent")
@ApiModel(description = "Pis consent entity", value = "PisConsent")
@NoArgsConstructor
public class PisConsent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_consent_generator")
    @SequenceGenerator(name = "pis_consent_generator", sequenceName = "pis_consent_id_seq")
    private Long id;

    @Column(name = "external_id", nullable = false)
    @ApiModelProperty(value = "An external exposed identification of the created payment consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @OneToMany(mappedBy = "consent",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    @ApiModelProperty(value = "List of single payments ", required = true)
    private List<PisPaymentData> payments = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "psu_id")
    private PsuData psuData;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tpp_info_id")
    @ApiModelProperty(value = "Information about TPP", required = true)
    private TppInfoEntity tppInfo;

    @Column(name = "payment_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Payment type: BULK, SINGLE or PERIODIC.", required = true, example = "SINGLE")
    private PaymentType paymentType;

    @Column(name = "payment_product", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Payment product", required = true, example = "sepa-credit-transfers")
    private PaymentProduct pisPaymentProduct;

    @Column(name = "consent_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Type of the consent: AIS or PIS.", required = true, example = "AIS")
    private ConsentType consentType = ConsentType.PIS;

    @Column(name = "consent_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP.", required = true, example = "VALID")
    private ConsentStatus consentStatus;
}
