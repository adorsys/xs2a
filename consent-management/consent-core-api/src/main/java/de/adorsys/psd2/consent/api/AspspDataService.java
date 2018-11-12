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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface AspspDataService {
    /**
     * Reads an AspspConsentData object by the Consent ID / Payment ID
     *
     * @param id AIS/PIIS Consent ID / Payment ID that identifies the data
     * @return <code>AspspConsentData</code> if consent was found. <code>false</code> otherwise.
     */
    @NotNull
    Optional<AspspConsentData> readAspspConsentData(@NotNull String id);

    /**
     * Writes/Updates an AspspConsentData by the consent ID given in it
     *
     * @param aspspConsentData aspspConsentData to be put
     * @return <code>true</code> if consent was found and data was updated. <code>false</code> otherwise.
     */
    boolean updateAspspConsentData(@NotNull AspspConsentData aspspConsentData);
}
