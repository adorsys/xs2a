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

package de.adorsys.aspsp.xs2a.spi.domain.consent;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.Objects;

@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class AspspConsentData {
    private final byte[] aspspConsentData;
    private final String consentId;

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
