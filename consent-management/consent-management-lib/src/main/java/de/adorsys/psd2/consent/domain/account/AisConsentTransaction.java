/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ais_consent_transaction")
public class AisConsentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_transaction_generator")
    @SequenceGenerator(name = "ais_consent_transaction_generator", sequenceName = "ais_consent_transaction_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consent_id", nullable = false)
    private ConsentEntity consentId;

    @Column(name = "number_of_transactions")
    private int numberOfTransactions;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "total_pages")
    private int totalPages;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus;
}
