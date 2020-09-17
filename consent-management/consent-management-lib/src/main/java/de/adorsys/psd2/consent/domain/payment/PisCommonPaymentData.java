/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.domain.*;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode
@Entity(name = "pis_common_payment")
public class PisCommonPaymentData extends InstanceDependableEntity implements Authorisable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_common_payment_generator")
    @SequenceGenerator(name = "pis_common_payment_generator", sequenceName = "pis_common_payment_id_seq", allocationSize = 1)
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "authorisation_template_id", nullable = false)
    private AuthorisationTemplateEntity authorisationTemplate;

    @Column(name = "tpp_ntfc_uri")
    private String tppNotificationUri;

    @ElementCollection
    @CollectionTable(name = "payment_tpp_ntfc", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "notification_mode", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private List<NotificationSupportedMode> tppNotificationContentPreferred;

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

    @Column(name = "int_req_id")
    private String internalRequestId;

    @Column(name = "canc_int_req_id")
    private String cancellationInternalRequestId;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "tpp_brand_log_info")
    private String tppBrandLoggingInformation;

    @Column(name = "signing_basket_blocked", nullable = false)
    private boolean signingBasketBlocked;

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

    private boolean isNotConfirmed() {
        return EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC).contains(transactionStatus);
    }

    public boolean isFinalised() {
        return transactionStatus.isFinalisedStatus();
    }

    @Override
    public String getExternalId() {
        return paymentId;
    }

    @Override
    public String getInternalRequestId(AuthorisationType authorisationType) {
        if (authorisationType == AuthorisationType.PIS_CREATION) {
            return internalRequestId;
        } else if (authorisationType == AuthorisationType.PIS_CANCELLATION) {
            return cancellationInternalRequestId;
        }

        throw new IllegalArgumentException("Invalid authorisation type: " + authorisationType);
    }
}

