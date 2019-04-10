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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

import java.util.List;
import java.util.Optional;

/**
 * Base version of AisConsentAuthorisationService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see de.adorsys.psd2.consent.api.service.AisConsentAuthorisationService
 * @see de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted
 */
interface AisConsentAuthorisationServiceBase {
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
}
