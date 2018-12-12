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
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString(exclude = "consent")
@Entity(name = "pis_common_payment")
@ApiModel(description = "pis common payment entity", value = "PisCommonPaymentData")
public class PisCommonPaymentData {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_common_payment_generator")
    @SequenceGenerator(name = "pis_common_payment_generator", sequenceName = "pis_common_payment_id_seq")
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "payment_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "Payment type: BULK, SINGLE or PERIODIC.", required = true, example = "SINGLE")
    private PaymentType paymentType;

    @Column(name = "payment_product", nullable = false)
    @ApiModelProperty(value = "Payment product", required = true, example = "sepa-credit-transfers")
    private String paymentProduct;

    @Column(name = "transaction_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(name = "transactionStatus", example = "ACCP", required = true)
    private TransactionStatus transactionStatus;

    @Lob
    @Column(name = "payment", nullable = false)
    @ApiModelProperty(value = "All data about the payment", required = true)
    private byte[] payment;

    @OneToMany(mappedBy = "psuId", cascade = CascadeType.ALL)
    @ApiModelProperty(value = "List of PSU", required = true)
    private List<PsuData> psuData = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tpp_info_id", nullable = false)
    @ApiModelProperty(value = "Information about TPP", required = true)
    private TppInfoEntity tppInfo;

    @OneToMany(mappedBy = "paymentData",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    @ApiModelProperty(value = "List of authorizations related to the consent", required = true)
    private List<PisConsentAuthorization> authorizations = new ArrayList<>();

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consent_id")
    @ApiModelProperty(value = "Detailed information about consent")
    private PisConsent consent;
}
