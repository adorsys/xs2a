/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.domain.ais;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentTypeBankSetting {

    /**
     * Indicates whether an ASPSP supports bank offered consent
     */
    private boolean bankOfferedConsentSupported;

    /**
     * Indicates whether an ASPSP supports global consent
     */
    private boolean globalConsentSupported;

    /**
     * Indicates whether an ASPSP supports consents for providing a list of available accounts of a PSU
     */
    private boolean availableAccountsConsentSupported;

    /**
     * Defines the maximum allowed by bank accesses for consent's usage per unique resource for each endpoint
     */
    private int accountAccessFrequencyPerDay;

    /**
     * The limit of an expiration time of not confirmed consent url set in milliseconds
     */
    private long notConfirmedConsentExpirationTimeMs;

    /**
     * The limit of a maximum lifetime of consent set in days. When this value equals to 0 or empty, then the maximum lifetime of consent is infinity
     */
    private int maxConsentValidityDays;

    /**
     * Indicates whether an ASPSP supports account owner information in consent
     */
    private boolean accountOwnerInformationSupported;
}
