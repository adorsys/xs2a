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
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.authorisation.AuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

import java.util.List;

/**
 * Base version of AuthorisationService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see de.adorsys.psd2.consent.api.service.AuthorisationService
 * @see de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted
 */
public interface AuthorisationServiceBase {

    /**
     * Creates authorisation and return response object
     *
     * @param parentHolder holder contains authorisation type and parent ID
     * @param request      needed parameters for creating authorisation
     * @return CreateAuthorisationResponse object with authorisation ID and scaStatus
     */
    CmsResponse<CreateAuthorisationResponse> createAuthorisation(AuthorisationParentHolder parentHolder, CreateAuthorisationRequest request);

    /**
     * Gets authorisation
     *
     * @param authorisationId ID of authorisation session
     * @return AuthorisationResponse
     */
    CmsResponse<Authorisation> getAuthorisationById(String authorisationId);

    /**
     * Updates authorisation
     *
     * @param authorisationId ID of authorisation session
     * @param request         needed parameters for updating authorisation
     * @return AuthorisationResponse
     */
    CmsResponse<Authorisation> updateAuthorisation(String authorisationId, UpdateAuthorisationRequest request);

    /**
     * Updates authorisation status
     *
     * @param authorisationId ID of authorisation session
     * @param scaStatus       to be updated status
     * @return boolean
     */
    CmsResponse<Boolean> updateAuthorisationStatus(String authorisationId, ScaStatus scaStatus);

    /**
     * Gets list of authorisation IDs by parent ID
     *
     * @param parentHolder holder contains authorisation type and parent ID
     * @return list of parent authorisation IDs
     */
    CmsResponse<List<String>> getAuthorisationsByParentId(AuthorisationParentHolder parentHolder);

    /**
     * Gets SCA status of the authorisation by parent ID and authorisation ID
     *
     * @param parentHolder    holder contains authorisation type and parent ID
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA status of the authorisation
     */
    CmsResponse<ScaStatus> getAuthorisationScaStatus(String authorisationId, AuthorisationParentHolder parentHolder);

    /**
     * Checks if requested authentication method is decoupled.
     *
     * @param authorisationId        String representation of the authorisation identifier
     * @param authenticationMethodId String representation of the available authentication method identifier
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     */
    CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId);

    /**
     * Saves authentication methods in provided authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param methods         List of authentication methods to be saved
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     */
    CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods);

    /**
     * Updates AIS SCA approach in authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaApproach     Chosen SCA approach
     * @return <code>true</code> if authorisation was found and SCA approach updated, <code>false</code> otherwise
     */
    CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach);

    /**
     * Gets SCA approach from the authorisation by authorisation ID
     *
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA approach of the authorisation
     */
    CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId);
}
