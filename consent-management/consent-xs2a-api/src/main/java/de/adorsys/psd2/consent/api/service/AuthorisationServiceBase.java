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
