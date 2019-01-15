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

package de.adorsys.psd2.xs2a.core.consent;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class is used as a container of some binary data to be used on SPI level.
 * SPI developers may save here necessary information, that will be stored and encrypted in consent.
 * SPI developer shall not use this class without consentId!
 */
@Value
public class AspspConsentData {
    /**
     * Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     * May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     */
    @Nullable
    private final byte[] aspspConsentData;

    @NotNull
    private final String consentId;

    /**
     * To be used in cases where binary data should be updated and new instance should be returned.
     * ConsentId value will be copied from the existing one.
     *
     * @param responseAspspConsentData binary data
     * @return AspspConsentData instance with provided binary data and the copied consentId
     */
    public final AspspConsentData respondWith(byte[] responseAspspConsentData) {
        return new AspspConsentData(responseAspspConsentData, this.consentId);
    }

    /**
     * Returns consent data without information. To be used in case when need to create empty object.
     *
     * @return empty AspspConsentData
     */
    @SuppressWarnings("ConstantConditions") //this is the only one case when we use consentId as null
    public static AspspConsentData emptyConsentData() {
        return new AspspConsentData(null, null);
    }

    /**
     * Checks whether consent data is contains information or not
     *
     * @return <code>true</code> if consent data is empty. <code>false</code> otherwise.
     */
    public boolean isEmptyConsentData() {
        return aspspConsentData == null && StringUtils.isBlank(consentId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AspspConsentData that = (AspspConsentData) o;

        return Arrays.equals(aspspConsentData, that.aspspConsentData) && Objects.equals(consentId, that.getConsentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspspConsentData, consentId);
    }
}

