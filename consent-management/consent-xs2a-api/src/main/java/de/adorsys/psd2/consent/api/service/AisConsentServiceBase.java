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

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

import java.util.List;
import java.util.Optional;

/**
 * Base version of AisConsentService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see de.adorsys.psd2.consent.api.service.AisConsentService
 * @see de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted
 */
interface AisConsentServiceBase {

    /**
     * Create AIS consent
     *
     * @param request needed parameters for creating AIS consent
     * @return String consent id
     */
    Optional<String> createConsent(CreateAisConsentRequest request);

    /**
     * Reads status of consent by id
     *
     * @param consentId id of consent
     * @return ConsentStatus
     */
    Optional<ConsentStatus> getConsentStatusById(String consentId);

    /**
     * Updates consent status by id
     *
     * @param consentId id of consent
     * @param status    new consent status
     * @return Boolean
     */
    boolean updateConsentStatusById(String consentId, ConsentStatus status);

    /**
     * Reads full information of consent by id
     *
     * @param consentId id of consent
     * @return AisAccountConsent
     */
    Optional<AisAccountConsent> getAisAccountConsentById(String consentId);

    /**
     * Reads full initial information of consent by id
     *
     * @param consentId id of consent
     * @return AisAccountConsent
     */
    Optional<AisAccountConsent> getInitialAisAccountConsentById(String consentId);

    /**
     * Finds old consents for current TPP and PSU and terminates them.
     * This method should be invoked, when a new consent is authorised.
     *
     * @param newConsentId id of new consent
     * @return true if any consents have been terminated, false - if none
     */
    boolean findAndTerminateOldConsentsByNewConsentId(String newConsentId);

    /**
     * Saves information about uses of consent
     *
     * @param request needed parameters for logging usage AIS consent
     */
    void checkConsentAndSaveActionLog(AisConsentActionRequest request);

    /**
     * Updates AIS consent aspsp account access by id
     *
     * @param request   needed parameters for updating AIS consent
     * @param consentId id of the consent to be updated
     * @return String   consent id
     */
    Optional<String> updateAspspAccountAccess(String consentId, AisAccountAccessInfo request);

    /**
     * Creates consent authorization
     *
     * @param consentId id of consent
     * @param request   needed parameters for creating consent authorization
     * @return String authorization id
     */
    Optional<String> createAuthorization(String consentId, AisConsentAuthorizationRequest request);

    /**
     * Gets consent authorization
     *
     * @param consentId       id of consent
     * @param authorizationId id of authorisation session
     * @return AisConsentAuthorizationResponse
     */
    Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId);

    /**
     * Updates consent authorization
     *
     * @param authorizationId id of authorisation session
     * @param request         needed parameters for updating consent authorization
     * @return boolean
     */
    boolean updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request);

    Optional<List<PsuIdData>> getPsuDataByConsentId(String consentId);

    /**
     * Gets list of consent authorisation IDs by consent ID
     *
     * @param consentId id of consent
     * @return list of consent authorisation IDs
     */
    Optional<List<String>> getAuthorisationsByConsentId(String consentId);

    /**
     * Gets SCA status of the authorisation by consent ID and authorisation ID
     *
     * @param consentId       String representation of the consent identifier
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA status of the authorisation
     */
    Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId);

    /**
     * Checks if requested authentication method is decoupled.
     *
     * @param authorisationId        String representation of the authorisation identifier
     * @param authenticationMethodId String representation of the available authentication method identifier
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     */
    boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId);

    /**
     * Saves authentication methods in provided authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param methods         List of authentication methods to be saved
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     */
    boolean saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods);

    /**
     * Updates AIS SCA approach in authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaApproach     Chosen SCA approach
     * @return <code>true</code> if authorisation was found and SCA approach updated, <code>false</code> otherwise
     */
    boolean updateScaApproach(String authorisationId, ScaApproach scaApproach);

    /**
     * Updates multilevel SCA required field
     *
     * @param consentId             String representation of the consent identifier
     * @param multilevelScaRequired multilevel SCA required indicator
     * @return <code>true</code> if authorisation was found and SCA required field updated, <code>false</code> otherwise
     */
    boolean updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired);
}
