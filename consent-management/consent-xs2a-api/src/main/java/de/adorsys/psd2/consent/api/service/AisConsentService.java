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
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;

import java.util.Optional;

public interface AisConsentService {

    /**
     * Create AIS consent
     *
     * @param request needed parameters for creating AIS consent
     * @return String consent id
     */
    Optional<String> createConsent(CreateAisConsentRequest request);

    /**
     * Read status of consent by id
     *
     * @param consentId id of consent
     * @return ConsentStatus
     */
    Optional<ConsentStatus> getConsentStatusById(String consentId);

    /**
     * Update consent status by id
     *
     * @param consentId id of consent
     * @param status    new consent status
     * @return Boolean
     */
    boolean updateConsentStatusById(String consentId, ConsentStatus status);

    /**
     * Read full information of consent by id
     *
     * @param consentId id of consent
     * @return AisAccountConsent
     */
    Optional<AisAccountConsent> getAisAccountConsentById(String consentId);

    /**
     * Save information about uses of consent
     *
     * @param request needed parameters for logging usage AIS consent
     */
    void checkConsentAndSaveActionLog(AisConsentActionRequest request);

    /**
     * Update AIS consent account access by id
     *
     * @param request   needed parameters for updating AIS consent
     * @param consentId id of the consent to be updated
     * @return String   consent id
     */
    Optional<String> updateAccountAccess(String consentId, AisAccountAccessInfo request);

    /**
     * Get Ais aspsp consent data by id
     *
     * @param consentId id of the consent
     * @return Response containing aspsp consent data
     */
    Optional<CmsAspspConsentDataBase64> getAspspConsentData(String consentId);

    /**
     * Update AIS consent aspsp consent data by id
     *
     * @param request   Aspsp provided ais consent data
     * @param consentId id of the consent to be updated
     * @return String   consent id
     */
    Optional<String> saveAspspConsentDataInAisConsent(String consentId, CmsAspspConsentDataBase64 request);

    /**
     * Create consent authorization
     *
     * @param consentId id of consent
     * @param request   needed parameters for creating consent authorization
     * @return String authorization id
     */
    Optional<String> createAuthorization(String consentId, AisConsentAuthorizationRequest request);

    /**
     * Get consent authorization
     *
     * @param consentId       id of consent
     * @param authorizationId id of authorisation session
     * @return AisConsentAuthorizationResponse
     */
    Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId);

    /**
     * Update consent authorization
     *
     * @param authorizationId id of authorisation session
     * @param request         needed parameters for updating consent authorization
     * @return boolean
     */
    boolean updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request);

    Optional<PsuIdData> getPsuDataByConsentId(String consentId);
}
