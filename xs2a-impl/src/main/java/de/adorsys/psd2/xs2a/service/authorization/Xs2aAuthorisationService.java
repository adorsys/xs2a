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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.authorisation.AuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAuthenticationObjectToCmsScaMethodMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aAuthorisationService {

    private final AuthorisationServiceEncrypted authorisationServiceEncrypted;
    private final Xs2aAuthenticationObjectToCmsScaMethodMapper xs2aAuthenticationObjectToCmsScaMethodMapper;

    /**
     * Stores created authorisation
     *
     * @param parentId String representation of identifier of stored parent
     * @return CreateAuthorisationResponse object with authorisation ID and scaStatus
     */
    public Optional<CreateAuthorisationResponse> createAuthorisation(CreateAuthorisationRequest request, String parentId, AuthorisationType authorisationType) {
        CmsResponse<CreateAuthorisationResponse> authorisationResponse = authorisationServiceEncrypted.createAuthorisation(new AuthorisationParentHolder(authorisationType, parentId), request);

        if (authorisationResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(authorisationResponse.getPayload());
    }

    /**
     * Requests CMS to retrieve authorisation by its identifier
     *
     * @param authorisationId String representation of identifier of stored consent authorisation
     * @return Response contains Authorisation
     */

    public Optional<Authorisation> getAuthorisationById(String authorisationId) {
        CmsResponse<Authorisation> authorisationResponse = authorisationServiceEncrypted.getAuthorisationById(authorisationId);

        if (authorisationResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(authorisationResponse.getPayload());
    }

    /**
     * Updates SCA approach in authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaApproach     Chosen SCA approach
     */
    public void updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        authorisationServiceEncrypted.updateScaApproach(authorisationId, scaApproach);
    }

    /**
     * Saves authentication methods in provided authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param methods         List of authentication methods to be saved
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     */
    public boolean saveAuthenticationMethods(String authorisationId, List<AuthenticationObject> methods) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.saveAuthenticationMethods(authorisationId,
                                                                                                xs2aAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(methods));
        return response.isSuccessful() && response.getPayload();
    }

    /**
     * Requests CMS to retrieve authentication method and checks if requested authentication method is decoupled.
     *
     * @param authorisationId        String representation of the authorisation identifier
     * @param authenticationMethodId String representation of the available authentication method identifier
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     */
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
        return response.isSuccessful() && response.getPayload();
    }

    /**
     * Sends a PUT request to CMS to update status in authorisation
     *
     * @param authorisationId String representation of authorisation identifier
     * @param scaStatus       Enum for status of the SCA method applied
     */
    public void updateAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        authorisationServiceEncrypted.updateAuthorisationStatus(authorisationId, scaStatus);
    }

    /**
     * Gets SCA approach from the authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA approach
     */
    public Optional<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        CmsResponse<AuthorisationScaApproachResponse> response = authorisationServiceEncrypted.getAuthorisationScaApproach(authorisationId);

        if (response.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.getPayload());
    }

    /**
     * Requests to retrieve authorisation IDs by parent ID
     *
     * @param parentId String representation of identifier of stored consent
     * @return list of consent authorisation IDs
     */
    public Optional<List<String>> getAuthorisationSubResources(String parentId, AuthorisationType authorisationType) {
        CmsResponse<List<String>> response = authorisationServiceEncrypted.getAuthorisationsByParentId(new AuthorisationParentHolder(authorisationType, parentId));

        if (response.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.getPayload());
    }

    public CmsResponse<Authorisation> updateAuthorisation(UpdateAuthorisationRequest request, String authorisationId) {
        return authorisationServiceEncrypted.updateAuthorisation(authorisationId, request);
    }

    /**
     * Retrieves SCA status of authorisation
     *
     * @param parentId        String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getAuthorisationScaStatus(String authorisationId, String parentId, AuthorisationType authorisationType) {
        CmsResponse<ScaStatus> response = authorisationServiceEncrypted.getAuthorisationScaStatus(authorisationId,
                                                                                                  new AuthorisationParentHolder(authorisationType, parentId));

        if (response.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.getPayload());
    }
}
