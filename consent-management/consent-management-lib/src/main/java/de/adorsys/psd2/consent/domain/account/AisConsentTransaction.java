/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
