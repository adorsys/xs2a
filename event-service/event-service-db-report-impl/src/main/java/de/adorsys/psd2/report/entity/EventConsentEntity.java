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

package de.adorsys.psd2.report.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "consent")
public class EventConsentEntity implements EventPsuDataList, Serializable {
    @Id
    @Column(name = "consent_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consent_generator")
    @SequenceGenerator(name = "consent_generator", sequenceName = "consent_id_seq",
        allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "consent_psu_data",
        joinColumns = @JoinColumn(name = "consent_id"),
        inverseJoinColumns = @JoinColumn(name = "psu_data_id"))
    private List<EventPsuData> psuDataList = new ArrayList<>();
}
