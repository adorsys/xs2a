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

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;

import java.util.List;
import java.util.Optional;

/**
 * Base version of PisConsentService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see de.adorsys.psd2.consent.api.service.PisConsentService
 * @see de.adorsys.psd2.consent.api.service.PisConsentServiceEncrypted
 */
interface PisConsentServiceBase {

    Optional<CreatePisConsentResponse> createPaymentConsent(PisConsentRequest request);

    /**
     * Retrieves consent status from pis consent by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @return Information about the status of a consent
     */
    Optional<ConsentStatus> getConsentStatusById(String consentId);

    /**
     * Reads full information of pis consent by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @return Response containing full information about pis consent
     */
    Optional<PisConsentResponse> getConsentById(String consentId);

    /**
     * Updates pis consent status by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @param status    new consent status
     * @return Response containing result of status changing
     */
    Optional<Boolean> updateConsentStatusById(String consentId, ConsentStatus status);

    /**
     * Creates consent authorization
     *
     * @param paymentId         String representation of the payment identifier
     * @param authorizationType Type of authorisation
     * @param psuData           Information about PSU
     * @return Response containing authorization id
     */
    Optional<CreatePisConsentAuthorisationResponse> createAuthorization(String paymentId, CmsAuthorisationType authorizationType, PsuIdData psuData);

    /**
     * Creates consent authorization cancellation
     *
     * @param paymentId         String representation of the payment identifier
     * @param authorizationType Type of authorisation
     * @param psuData           Information about PSU
     * @return Response containing authorization id
     */
    Optional<CreatePisConsentAuthorisationResponse> createAuthorizationCancellation(String paymentId, CmsAuthorisationType authorizationType, PsuIdData psuData);

    /**
     * Updates consent authorization
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param request         Incoming request for updating authorization
     * @return Response containing SCA status, available and chosen Sca method
     */
    Optional<UpdatePisConsentPsuDataResponse> updateConsentAuthorisation(String authorisationId, UpdatePisConsentPsuDataRequest request);

    /**
     * Updates consent cancellation authorization
     *
     * @param authorizationId String representation of the authorisation identifier
     * @param request         Incoming request for updating authorization
     * @return Response containing SCA status, available and chosen Sca method
     */
    Optional<UpdatePisConsentPsuDataResponse> updateConsentCancellationAuthorisation(String authorizationId, UpdatePisConsentPsuDataRequest request);

    /**
     * Updates PIS consent payment data and stores it into database
     *
     * @param request   PIS consent request for update payment data
     * @param consentId Consent ID
     */
    void updatePaymentConsent(PisConsentRequest request, String consentId);

    /**
     * Get information about Authorisation by authorisation identifier
     *
     * @param authorisationId String representation of the authorisation identifier
     * @return Response containing information about Authorisation
     */
    Optional<GetPisConsentAuthorisationResponse> getPisConsentAuthorisationById(String authorisationId);

    /**
     * Get information about Authorisation by cancellation identifier
     *
     * @param cancellationId String representation of the cancellation identifier
     * @return Response containing information about Authorisation
     */
    Optional<GetPisConsentAuthorisationResponse> getPisConsentCancellationAuthorisationById(String cancellationId);

    /**
     * Gets list of payment authorisation IDs by payment ID and authorisation type
     *
     * @param paymentId         String representation of the payment identifier
     * @param authorisationType Type of authorisation
     * @return Response containing information about authorisation IDs
     */
    Optional<List<String>> getAuthorisationsByPaymentId(String paymentId, CmsAuthorisationType authorisationType);

    /**
     * Get information about PSU by payment identifier
     *
     * @param paymentId String representation of the payment identifier
     * @return Response containing information about PSU
     */
    Optional<PsuIdData> getPsuDataByPaymentId(String paymentId);

    /**
     * Get information about PSU by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @return Response containing information about PSU
     */
    Optional<PsuIdData> getPsuDataByConsentId(String consentId);
}
