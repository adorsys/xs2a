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

package de.adorsys.psd2.xs2a.core.psu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Contains authorisation data about PSU.
 * Normally it comes with the TPP request header.
 */
@Value
public class PsuIdData {
    @Nullable
    private String psuId;

    @Nullable
    private String psuIdType;

    @Nullable
    private String psuCorporateId;

    @Nullable
    private String psuCorporateIdType;

    @Nullable
    private String psuIpAddress;

    @Nullable
    private AdditionalPsuIdData additionalPsuIdData;

    public PsuIdData() {
        this(null, null, null, null, null);
    }

    public PsuIdData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String psuIpAddress) {
        this(psuId, psuIdType, psuCorporateId, psuCorporateIdType, psuIpAddress, null);
    }

    public PsuIdData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String psuIpAddress, AdditionalPsuIdData additionalPsuIdData) {
        this.psuId = psuId;
        this.psuIdType = psuIdType;
        this.psuCorporateId = psuCorporateId;
        this.psuCorporateIdType = psuCorporateIdType;
        this.psuIpAddress = psuIpAddress;
        this.additionalPsuIdData = additionalPsuIdData;
    }

    public boolean contentEquals(PsuIdData otherPsuIdData) {
        if (Objects.isNull(otherPsuIdData)) {
            return false;
        }

        return StringUtils.equals(this.getPsuId(), otherPsuIdData.getPsuId())
                   && StringUtils.equals(this.getPsuCorporateId(), otherPsuIdData.getPsuCorporateId())
                   && StringUtils.equals(this.getPsuCorporateIdType(), otherPsuIdData.getPsuCorporateIdType())
                   && StringUtils.equals(this.getPsuIdType(), otherPsuIdData.getPsuIdType());
    }

    @JsonIgnore
    public boolean isEmpty() {
        return StringUtils.isBlank(this.getPsuId());
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
