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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.AccountAccess;

public interface AisConsentServiceBase {

    /**
     * Saves information about uses of consent
     *
     * @param request needed parameters for logging usage AIS consent
     * @return VoidResponse
     * @throws WrongChecksumException in case of any attempt to change definite consent fields after its status became valid.
     */
    CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest request) throws WrongChecksumException;

    /**
     * Updates AIS consent aspsp account access by id and return consent
     *
     * @param request   needed parameters for updating AIS consent
     * @param consentId id of the consent to be updated
     * @return AisAccountConsent consent
     * @throws WrongChecksumException in case of any attempt to change definite consent fields after its status became valid.
     */
    CmsResponse<CmsConsent> updateAspspAccountAccess(String consentId, AccountAccess request) throws WrongChecksumException;
}
