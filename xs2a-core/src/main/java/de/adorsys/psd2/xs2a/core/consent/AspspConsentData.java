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
    private final byte[] aspspConsentDataBytes;

    @NotNull
    private final String consentId;

    /**
     * Checks whether consent data is contains information or not
     *
     * @return <code>true</code> if consent data is empty. <code>false</code> otherwise.
     */
    public boolean isEmptyConsentData() {
        return aspspConsentDataBytes == null && StringUtils.isBlank(consentId);
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

        return Arrays.equals(aspspConsentDataBytes, that.aspspConsentDataBytes) && Objects.equals(consentId, that.getConsentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspspConsentDataBytes, consentId);
    }
}

