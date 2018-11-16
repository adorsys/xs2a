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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.ConsentType;

import java.util.Optional;

public interface CommonConsentService {
    /**
     * Gets aspsp consent data by consent id and consent type
     *
     * @param consentId   String representation of consent identifier
     * @param consentType Type of the consent
     * @return Response containing aspsp consent data
     */
    Optional<CmsAspspConsentDataBase64> getAspspConsentDataByConsentId(String consentId, ConsentType consentType);

    /**
     * Gets Pis aspsp consent data by payment id
     *
     * @param paymentId String representation of payment identifier
     * @return Response containing aspsp consent data
     */
    Optional<CmsAspspConsentDataBase64> getAspspConsentDataByPaymentId(String paymentId);

    /**
     * Updates consent aspsp consent data by consent id and consent type
     *
     * @param consentId   String representation of consent identifier
     * @param request     Aspsp provided consent data
     * @param consentType Type of the consent
     * @return String   consent id
     */
    Optional<String> saveAspspConsentData(String consentId, CmsAspspConsentDataBase64 request, ConsentType consentType);

    /**
     * Deletes aspsp consent data by consent id and consent type
     *
     * @param consentId   String representation of consent identifier
     * @param consentType Type of the consent
     * @return <code>true</code> if consent was found and data was deleted. <code>false</code> otherwise.
     */
    boolean deleteAspspConsentDataByConsentId(String consentId, ConsentType consentType);
}
