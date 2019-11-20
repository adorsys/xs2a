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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

import java.util.List;

/**
 * Base version of PisAuthorisationService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see PisAuthorisationService
 * @see PisAuthorisationServiceEncrypted
 */
public interface PisAuthorisationServiceBase {
    /**
     * Creates payment authorisation
     *
     * @param paymentId String representation of the payment identifier
     * @param request   PIS authorisation request
     * @return Response containing authorisation ID
     */
    CmsResponse<CreatePisAuthorisationResponse> createAuthorization(String paymentId, CreatePisAuthorisationRequest request);

    /**
     * Creates payment authorisation cancellation
     *
     * @param paymentId               String representation of the payment identifier
     * @param pisAuthorisationRequest PIS authorisation request
     * @return Response containing authorisation ID
     */
    CmsResponse<CreatePisAuthorisationResponse> createAuthorizationCancellation(String paymentId, CreatePisAuthorisationRequest pisAuthorisationRequest);

    /**
     * Updates payment authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param request         Incoming request for updating authorisation
     * @return Response containing SCA status, available and chosen SCA method
     */
    CmsResponse<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(String authorisationId, UpdatePisCommonPaymentPsuDataRequest request);

    /**
     * Updates a specific payment authorisation's status
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaStatus       The status to be updated
     * @return <code>true</code> if status was updated, <code>false</code> otherwise
     */
    CmsResponse<Boolean> updatePisAuthorisationStatus(String authorisationId, ScaStatus scaStatus);

    /**
     * Updates payment cancellation authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param request         Incoming request for updating authorisation
     * @return Response containing SCA status, available and chosen SCA method
     */
    CmsResponse<UpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorisation(String authorisationId, UpdatePisCommonPaymentPsuDataRequest request);

    /**
     * Get information about Authorisation by authorisation identifier
     *
     * @param authorisationId String representation of the authorisation identifier
     * @return Response containing information about Authorisation
     */
    CmsResponse<GetPisAuthorisationResponse> getPisAuthorisationById(String authorisationId);

    /**
     * Get information about Authorisation by cancellation identifier
     *
     * @param cancellationId String representation of the cancellation identifier
     * @return Response containing information about Authorisation
     */
    CmsResponse<GetPisAuthorisationResponse> getPisCancellationAuthorisationById(String cancellationId);

    /**
     * Gets list of payment authorisation IDs by payment ID and authorisation type
     *
     * @param paymentId         String representation of the payment identifier
     * @param authorisationType Type of authorisation
     * @return Response containing information about authorisation IDs
     */
    CmsResponse<List<String>> getAuthorisationsByPaymentId(String paymentId, PaymentAuthorisationType authorisationType);

    /**
     * Gets SCA status of the authorisation by payment ID, authorisation ID and authorisation type
     *
     * @param paymentId         String representation of the payment identifier
     * @param authorisationId   String representation of the authorisation identifier
     * @param authorisationType Type of authorisation
     * @return SCA status of the authorisation
     */
    CmsResponse<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId, PaymentAuthorisationType authorisationType);

    /**
     * Checks if requested authentication method is decoupled.
     *
     * @param authorisationId        String representation of the authorisation identifier
     * @param authenticationMethodId String representation of the available authentication method identifier
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     */
    CmsResponse<Boolean>  isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId);

    /**
     * Saves authentication methods in provided authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param methods         List of authentication methods to be saved
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     */
    CmsResponse<Boolean>  saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods);

    /**
     * Updates pis sca approach
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaApproach     chosen sca approach
     * @return <code>true</code> if authorisation was found and sca approach updated, <code>false</code> otherwise
     */
    CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach);

    /**
     * Gets SCA approach from the authorisation by authorisation ID and authorisation type
     *
     * @param authorisationId   String representation of the authorisation identifier
     * @param authorisationType Type of authorisation
     * @return SCA approach of the authorisation
     */
    CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId, PaymentAuthorisationType authorisationType);
}
