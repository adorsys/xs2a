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

import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity(name = "pis_common_payment")
public class PisCommonPaymentData extends InstanceDependableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_common_payment_generator")
    @SequenceGenerator(name = "pis_common_payment_generator", sequenceName = "pis_common_payment_id_seq")
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "payment_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "payment_product", nullable = false)
    private String paymentProduct;

    @Column(name = "transaction_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Lob
    @Column(name = "payment")
    private byte[] payment;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "pis_common_payment_psu_data",
        joinColumns = @JoinColumn(name = "pis_common_payment_id"),
        inverseJoinColumns = @JoinColumn(name = "psu_data_id"))
    private List<PsuData> psuDataList = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tpp_info_id", nullable = false)
    private TppInfoEntity tppInfo;

    @OneToMany(mappedBy = "paymentData",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    private List<PisAuthorization> authorizations = new ArrayList<>();

    @OneToMany(mappedBy = "paymentData",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    private List<PisPaymentData> payments = new ArrayList<>();

    @Column(name = "creation_timestamp", nullable = false)
    private OffsetDateTime creationTimestamp = OffsetDateTime.now();

    @Column(name = "multilevel_sca_required", nullable = false)
    private boolean multilevelScaRequired;

    @Column(name = "aspsp_account_id", length = 100)
    private String aspspAccountId;

    @Column(name = "status_change_timestamp")
    private OffsetDateTime statusChangeTimestamp;

    @Transient
    private TransactionStatus previousTransactionStatus;

    @PostLoad
    public void pisCommonPaymentDataPostLoad() {
        previousTransactionStatus = transactionStatus;
    }

    @PreUpdate
    public void pisCommonPaymentDataPreUpdate() {
        if (previousTransactionStatus != transactionStatus) {
            statusChangeTimestamp = OffsetDateTime.now();
        }
    }

    @PrePersist
    public void pisCommonPaymentDataPrePersist() {
        if (Objects.isNull(statusChangeTimestamp)) {
            statusChangeTimestamp = creationTimestamp;
        }
    }

    public boolean isConfirmationExpired(long expirationPeriodMs) {
        if (isNotConfirmed()) {
            return creationTimestamp.plus(expirationPeriodMs, ChronoUnit.MILLIS)
                       .isBefore(OffsetDateTime.now());
        }

        return false;
    }

    public boolean isNotConfirmed() {
        return transactionStatus == TransactionStatus.RCVD;
    }

    public boolean isFinalised() {
        return transactionStatus.isFinalisedStatus();
    }
}

