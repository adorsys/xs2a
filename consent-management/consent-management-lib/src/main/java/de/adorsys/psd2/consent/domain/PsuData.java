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

package de.adorsys.psd2.consent.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity(name = "psu_data")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PsuData extends InstanceDependableEntity {
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

    @Column(name = "psu_ip_address")
    private String psuIpAddress;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "additional_psu_data_id")
    private AdditionalPsuData additionalPsuData;

    public PsuData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String psuIpAddress) {
        this.psuId = psuId;
        this.psuIdType = psuIdType;
        this.psuCorporateId = psuCorporateId;
        this.psuCorporateIdType = psuCorporateIdType;
        this.psuIpAddress = psuIpAddress;
    }

    public PsuData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String psuIpAddress, AdditionalPsuData additionalPsuData) {
        this(psuId, psuIdType, psuCorporateId, psuCorporateIdType, psuIpAddress);
        this.additionalPsuData = additionalPsuData;
    }

    public boolean contentEquals(@NotNull PsuData otherPsuData) {
        return StringUtils.equals(this.getPsuId(), otherPsuData.getPsuId())
                   && StringUtils.equals(this.getPsuCorporateId(), otherPsuData.getPsuCorporateId())
                   && StringUtils.equals(this.getPsuCorporateIdType(), otherPsuData.getPsuCorporateIdType())
                   && StringUtils.equals(this.getPsuIdType(), otherPsuData.getPsuIdType());
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(this.getPsuId());
    }
}
