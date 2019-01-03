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

package de.adorsys.psd2.xs2a.core.psu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

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

    public boolean contentEquals(PsuIdData otherPsuIdData) {
        return StringUtils.equals(this.getPsuId(), otherPsuIdData.getPsuId())
                   && StringUtils.equals(this.getPsuCorporateId(), otherPsuIdData.getPsuCorporateId())
                   && StringUtils.equals(this.getPsuCorporateIdType(), otherPsuIdData.getPsuCorporateIdType())
                   && StringUtils.equals(this.getPsuIdType(), otherPsuIdData.getPsuIdType());
    }

    @JsonIgnore
    public boolean isEmpty() {
        return StringUtils.isBlank(this.getPsuId())
                   && StringUtils.isBlank(this.getPsuIdType())
                   && StringUtils.isBlank(this.getPsuCorporateId())
                   && StringUtils.isBlank(this.getPsuCorporateIdType());
    }
}
