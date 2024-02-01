/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.core.profile;

public enum PiisConsentSupported {
    /**
     * - NOT_SUPPORTED (by default) means that there is no need to create PIIS consent in CMS for availability of funds check,
     * Confirmation of Funds request is being passed to SPI level as is and ASPSP is responsible for proper handling of
     * such a request(no consent validation is performed by XS2A);
     * <p>
     * - TPP_CONSENT_SUPPORTED Establish PIIS Consent through XS2A interface;
     * <p>
     * - ASPSP_CONSENT_SUPPORTED means that ASPSP creates PIIS consent in CMS and Confirmation of Funds request is validated according to this consent
     */
    NOT_SUPPORTED,
    TPP_CONSENT_SUPPORTED,
    ASPSP_CONSENT_SUPPORTED
}
