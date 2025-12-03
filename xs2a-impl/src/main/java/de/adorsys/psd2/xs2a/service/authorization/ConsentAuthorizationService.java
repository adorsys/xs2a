/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;

import java.util.Optional;

public interface ConsentAuthorizationService extends ScaApproachServiceTypeProvider {

    /**
     * Creates a new consent authorization.
     *
     * @param createAuthorisationRequest the authorization creation request
     * @return an Optional containing the authorization response if successful, empty otherwise
     */
    Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(Xs2aCreateAuthorisationRequest createAuthorisationRequest);

    /**
     * Updates consent PSU data during the authorization process.
     *
     * @param request the authorization parameters
     * @param response the authorization processor response
     * @return the updated authorization processor response
     */
    AuthorisationProcessorResponse updateConsentPsuData(CommonAuthorisationParameters request, AuthorisationProcessorResponse response);

    /**
     * Retrieves a consent authorization by its ID.
     *
     * @param authorizationId the authorization ID
     * @return an Optional containing the authorization if found, empty otherwise
     */
    Optional<Authorisation> getConsentAuthorizationById(String authorizationId);

    /**
     * Retrieves the SCA status of an authorization.
     *
     * @param consentId the consent ID
     * @param authorisationId the authorization ID
     * @return an Optional containing the SCA status if found, empty otherwise
     */
    Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId);
}
