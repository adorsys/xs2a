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
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

import java.util.List;
import java.util.Optional;

/**
 * Base version of PisAuthorisationService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see PisAuthorisationService
 * @see PisAuthorisationServiceEncrypted
 */
public interface PisAuthorisationServiceBase {
    /**
     * Creates payment authorization
     *
     * @param paymentId String representation of the payment identifier
     * @param request   PIS authorisation request
     * @return Response containing authorization id
     */
    Optional<CreatePisAuthorisationResponse> createAuthorization(String paymentId, CreatePisAuthorisationRequest request);

    /**
     * Creates payment authorization cancellation
     *
     * @param paymentId               String representation of the payment identifier
     * @param pisAuthorisationRequest PIS authorisation request
     * @return Response containing authorization id
     */
    Optional<CreatePisAuthorisationResponse> createAuthorizationCancellation(String paymentId, CreatePisAuthorisationRequest pisAuthorisationRequest);

    /**
     * Updates payment authorization
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param request         Incoming request for updating authorization
     * @return Response containing SCA status, available and chosen Sca method
     */
    Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(String authorisationId, UpdatePisCommonPaymentPsuDataRequest request);

    /**
     * Updates a specific payment authorisation's status
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaStatus       The to be updated status
     * @return
     */
    boolean updatePisAuthorisationStatus(String authorisationId, ScaStatus scaStatus);

    /**
     * Updates payment cancellation authorization
     *
     * @param authorizationId String representation of the authorisation identifier
     * @param request         Incoming request for updating authorization
     * @return Response containing SCA status, available and chosen Sca method
     */
    Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorisation(String authorizationId, UpdatePisCommonPaymentPsuDataRequest request);

    /**
     * Get information about Authorisation by authorisation identifier
     *
     * @param authorisationId String representation of the authorisation identifier
     * @return Response containing information about Authorisation
     */
    Optional<GetPisAuthorisationResponse> getPisAuthorisationById(String authorisationId);

    /**
     * Get information about Authorisation by cancellation identifier
     *
     * @param cancellationId String representation of the cancellation identifier
     * @return Response containing information about Authorisation
     */
    Optional<GetPisAuthorisationResponse> getPisCancellationAuthorisationById(String cancellationId);

    /**
     * Gets list of payment authorisation IDs by payment ID and authorisation type
     *
     * @param paymentId         String representation of the payment identifier
     * @param authorisationType Type of authorisation
     * @return Response containing information about authorisation IDs
     */
    Optional<List<String>> getAuthorisationsByPaymentId(String paymentId, PaymentAuthorisationType authorisationType);

    /**
     * Gets SCA status of the authorisation by payment ID, authorisation ID and authorisation type
     *
     * @param paymentId         String representation of the payment identifier
     * @param authorisationId   String representation of the authorisation identifier
     * @param authorisationType Type of authorisation
     * @return SCA status of the authorisation
     */
    Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId, PaymentAuthorisationType authorisationType);

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
     * Updates pis sca approach
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaApproach     chosen sca approach
     * @return <code>true</code> if authorisation was found and sca approach updated, <code>false</code> otherwise
     */
    boolean updateScaApproach(String authorisationId, ScaApproach scaApproach);

    /**
     * Gets SCA approach from the authorisation by authorisation ID and authorisation type
     *
     * @param authorisationId   String representation of the authorisation identifier
     * @param authorisationType Type of authorisation
     * @return SCA approach of the authorisation
     */
    Optional<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId, PaymentAuthorisationType authorisationType);
}
