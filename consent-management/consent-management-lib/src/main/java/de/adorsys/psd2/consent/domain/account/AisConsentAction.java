/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.api.ActionStatus;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity(name = "ais_consent_action")
public class AisConsentAction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_action_generator")
    @SequenceGenerator(name = "ais_consent_action_generator", sequenceName = "ais_consent_action_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "tpp_id", nullable = false)
    private String tppId;

    @Column(name = "action_status", nullable = false, length = 50)
    @Enumerated(value = EnumType.STRING)
    private ActionStatus actionStatus;

    @Column(name = "requested_consent_id", nullable = false)
    private String requestedConsentId;
}
