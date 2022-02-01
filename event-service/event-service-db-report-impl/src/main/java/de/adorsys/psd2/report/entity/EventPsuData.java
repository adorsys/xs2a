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
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "psu_data")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventPsuData implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "psu_data_generator")
    @SequenceGenerator(name = "psu_data_generator", sequenceName = "psu_data_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "psu_id")
    private String psuId;

    @Column(name = "psu_id_type")
    private String psuIdType;

    @Column(name = "psu_corporate_id")
    private String psuCorporateId;

    @Column(name = "psu_corporate_id_type")
    private String psuCorporateIdType;
}
