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
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity(name = "consent_usage")
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"consent_id", "request_uri", "usage_date"})
})
@NoArgsConstructor
public class AisConsentUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consent_usage_generator")
    @SequenceGenerator(name = "consent_usage_generator", sequenceName = "consent_usage_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consent_id", nullable = false)
    private ConsentEntity consent;

    @Column(name = "request_uri", nullable = false)
    private String requestUri;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "usage_amount", nullable = false)
    private int usage;

    @Version
    @Column(name = "version")
    private long version;

    public AisConsentUsage(ConsentEntity consent, String requestUri) {
        this.usageDate = LocalDate.now();
        this.consent = consent;
        this.requestUri = requestUri;
    }

    @Override
    public String toString() {
        return "AisConsentUsage{" +
                   "id=" + id +
                   ", consentId=" + consent.getId() +
                   ", requestUri='" + requestUri + '\'' +
                   ", resourceId='" + resourceId + '\'' +
                   ", transactionId='" + transactionId + '\'' +
                   ", usageDate=" + usageDate +
                   ", usage=" + usage +
                   ", version=" + version +
                   '}';
    }
}
