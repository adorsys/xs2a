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

    /**
     * Indicates whether an ASPSP supports trusted beneficiaries in consent
     */
    private boolean trustedBeneficiariesSupported;
}
