/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;

public interface AisConsentServiceBase {

    /**
     * Saves information about uses of consent
     *
     * @param request needed parameters for logging usage AIS consent
     */
    CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest request) throws WrongChecksumException;

    /**
     * Updates AIS consent aspsp account access by id and return consent
     *
     * @param request   needed parameters for updating AIS consent
     * @param consentId id of the consent to be updated
     * @return AisAccountConsent consent
     */
    CmsResponse<CmsConsent> updateAspspAccountAccess(String consentId, AisAccountAccessInfo request) throws WrongChecksumException;
}
