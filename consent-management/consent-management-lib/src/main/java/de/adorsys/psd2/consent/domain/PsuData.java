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

package de.adorsys.psd2.consent.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity(name = "psu_data")
public class PsuData extends BaseEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "psu_data_generator")
    @SequenceGenerator(name = "psu_data_generator", sequenceName = "psu_data_id_seq")
    private Long id;

    @Column(name = "psu_id")
    private String psuId;

    @Column(name = "psu_id_type")
    private String psuIdType;

    @Column(name = "psu_corporate_id")
    private String psuCorporateId;

    @Column(name = "psu_corporate_id_type")
    private String psuCorporateIdType;

    public PsuData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        this.psuId = psuId;
        this.psuIdType = psuIdType;
        this.psuCorporateId = psuCorporateId;
        this.psuCorporateIdType = psuCorporateIdType;
    }
}
