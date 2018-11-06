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

import java.util.Optional;

public interface PisConsentService {

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
     * Get Pis aspsp consent data by consent id
     *
     * @param consentId id of the consent
     * @return Response containing aspsp consent data
     */
    //TODO move base64 handling to remote service/controller only. Service interface shouldn't perform always base64 encoding/decoding https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/412
    Optional<CmsAspspConsentDataBase64> getAspspConsentDataByConsentId(String consentId);

    /**
     * Get Pis aspsp consent data by payment id
     *
     * @param paymentId id of the payment
     * @return Response containing aspsp consent data
     */
    //TODO move base64 handling to remote service/controller only. Service interface shouldn't perform always base64 encoding/decoding https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/412
    Optional<CmsAspspConsentDataBase64> getAspspConsentDataByPaymentId(String paymentId);

    /**
     * Get original decrypted Id from encrypted string
     *
     * @param encryptedId id to be decrypted
     * @return Response containing original decrypted Id
     */
    Optional<String> getDecryptedId(String encryptedId);

    /**
     * Update PIS consent aspsp consent data by id
     *
     * @param request   Aspsp provided pis consent data
     * @param consentId id of the consent to be updated
     * @return String consent id
     */
    //TODO move base64 handling to remote service/controller only. Service interface shouldn't perform always base64 encoding/decoding https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/412
    Optional<String> updateAspspConsentDataInPisConsent(String consentId, CmsAspspConsentDataBase64 request);

    /**
     * Create consent authorization
     */
    Optional<CreatePisConsentAuthorisationResponse> createAuthorization(String paymentId, CmsAuthorisationType authorizationType, PsuIdData psuData);

    /**
     * Create consent authorization cancellation
     */
    Optional<CreatePisConsentAuthorisationResponse> createAuthorizationCancellation(String paymentId, CmsAuthorisationType authorizationType, PsuIdData psuData);

    Optional<UpdatePisConsentPsuDataResponse> updateConsentAuthorization(String authorizationId, UpdatePisConsentPsuDataRequest request, CmsAuthorisationType authorizationType);

    /**
     * Update PIS consent payment data and stores it into database
     *
     * @param request   PIS consent request for update payment data
     * @param consentId Consent ID
     */
    void updatePaymentConsent(PisConsentRequest request, String consentId);

    Optional<GetPisConsentAuthorisationResponse> getPisConsentAuthorizationById(String authorizationId, CmsAuthorisationType authorizationType);

    Optional<String> getAuthorisationByPaymentId(String paymentId, CmsAuthorisationType authorizationType);

    Optional<PsuIdData> getPsuDataByPaymentId(String paymentId);

    Optional<PsuIdData> getPsuDataByConsentId(String consentId);
}
