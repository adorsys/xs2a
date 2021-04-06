/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
