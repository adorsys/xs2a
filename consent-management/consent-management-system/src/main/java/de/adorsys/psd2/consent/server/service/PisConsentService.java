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

package de.adorsys.psd2.consent.server.service;

import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.CmsConsentStatus;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.server.domain.payment.PisConsent;
import de.adorsys.psd2.consent.server.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.server.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.server.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.server.repository.PisConsentRepository;
import de.adorsys.psd2.consent.server.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.server.service.mapper.PisConsentMapper;
import de.adorsys.psd2.consent.server.service.security.DecryptedData;
import de.adorsys.psd2.consent.server.service.security.SecurityDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.consent.api.CmsConsentStatus.RECEIVED;
import static de.adorsys.psd2.consent.api.CmsConsentStatus.VALID;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.STARTED;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PisConsentService {
    private final PisConsentRepository pisConsentRepository;
    private final PisConsentMapper pisConsentMapper;
    private final PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final SecurityDataService securityDataService;

    /**
     * Creates new pis consent with full information about payment
     *
     * @param request Consists information about payments.
     * @return Response containing identifier of consent
     */
    @Transactional
    public Optional<CreatePisConsentResponse> createPaymentConsent(PisConsentRequest request) {
        PisConsent consent = pisConsentMapper.mapToPisConsent(request);
        String externalId = UUID.randomUUID().toString();
        consent.setExternalId(externalId);

        PisConsent saved = pisConsentRepository.save(consent);

        return Optional.ofNullable(saved.getId())
                   .flatMap(id -> securityDataService.getEncryptedId(saved.getExternalId()))
                   .map(CreatePisConsentResponse::new);
    }

    /**
     * Retrieves consent status from pis consent by consent identifier
     *
     * @param encryptedConsentId String representation of pis consent identifier
     * @return Information about the status of a consent
     */
    public Optional<CmsConsentStatus> getConsentStatusById(String encryptedConsentId) {
        return getPisConsentById(encryptedConsentId)
                   .map(PisConsent::getConsentStatus);
    }

    /**
     * Reads full information of pis consent by consent identifier
     *
     * @param encryptedConsentId String representation of pis encrypted consent identifier
     * @return Response containing full information about pis consent
     */
    public Optional<PisConsentResponse> getConsentById(String encryptedConsentId) {
        return getPisConsentById(encryptedConsentId)
                   .flatMap(pisConsentMapper::mapToPisConsentResponse);
    }

    /**
     * Updates pis consent status by consent identifier
     *
     * @param encryptedConsentId String representation of pis encrypted consent identifier
     * @param status             new consent status
     * @return Response containing result of status changing
     */
    @Transactional
    public Optional<Boolean> updateConsentStatusById(String encryptedConsentId, CmsConsentStatus status) {
        return getActualPisConsent(encryptedConsentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    /**
     * Get Pis aspsp consent data by consent id
     *
     * @param encryptedConsentId id of the consent
     * @return Response containing aspsp consent data
     */
    public Optional<CmsAspspConsentDataBase64> getAspspConsentDataByConsentId(String encryptedConsentId) {
        return getPisConsentById(encryptedConsentId)
                   .map(dta -> prepareAspspConsentData(dta, encryptedConsentId));
    }

    /**
     * Get Pis aspsp consent data by payment id
     *
     * @param encryptedPaymentId encrypted id of the payment
     * @return Response containing aspsp consent data
     */
    public Optional<CmsAspspConsentDataBase64> getAspspConsentDataByPaymentId(String encryptedPaymentId) {
        Optional<String> paymentId = securityDataService.getDecryptedId(encryptedPaymentId);
        if (!paymentId.isPresent()) {
            log.warn("Payment Id has not encrypted: {}", encryptedPaymentId);
            return Optional.empty();
        }
        return pisPaymentDataRepository.findByPaymentId(paymentId.get())
                   .map(dta -> dta.get(0))
                   .map(PisPaymentData::getConsent)
                   .map(dta -> prepareAspspConsentData(dta, encryptedPaymentId));
    }

    private CmsAspspConsentDataBase64 prepareAspspConsentData(PisConsent consent, String encryptedConsentId) {
        return Optional.ofNullable(consent.getAspspConsentData())
                   .flatMap(dta -> securityDataService.decryptConsentData(encryptedConsentId, dta))
                   .map(DecryptedData::getData)
                   .map(bytes -> Base64.getEncoder().encodeToString(bytes))
                   .map(str64 -> new CmsAspspConsentDataBase64(encryptedConsentId, str64))
                   .orElseGet(() -> new CmsAspspConsentDataBase64(encryptedConsentId, null));
    }

    /**
     * Get original decrypted Id from encrypted string
     *
     * @param encryptedId id to be decrypted
     * @return Response containing original decrypted Id
     */
    public Optional<String> getDecryptedId(String encryptedId) {
        return securityDataService.getDecryptedId(encryptedId);
    }

    /**
     * Update PIS consent aspsp consent data by id
     *
     * @param request            Aspsp provided pis consent data
     * @param encryptedConsentId id of the consent to be updated
     * @return String consent id
     */
    @Transactional
    public Optional<String> updateAspspConsentDataInPisConsent(String encryptedConsentId, CmsAspspConsentDataBase64 request) {
        return getActualPisConsent(encryptedConsentId)
                   .flatMap(cons -> saveAspspConsentDataInPisConsent(request, cons, encryptedConsentId));
    }

    /**
     * Create consent authorization
     *
     * @param encryptedPaymentId encrypted id of the payment
     * @param authorizationType  type of authorization required to create. Can be  CREATED or CANCELLED
     * @return response contains authorization id
     */
    @Transactional
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorization(String encryptedPaymentId, CmsAuthorisationType authorizationType) {
        Optional<String> paymentId = securityDataService.getDecryptedId(encryptedPaymentId);
        if (!paymentId.isPresent()) {
            log.warn("Payment Id has not encrypted: {}", encryptedPaymentId);
            return Optional.empty();
        }

        return pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentId.get(), RECEIVED)
                   .map(list -> saveNewAuthorization(list.get(0).getConsent(), authorizationType))
                   .map(c -> new CreatePisConsentAuthorisationResponse(c.getExternalId()));
    }

    /**
     * Update consent authorization
     *
     * @param authorizationId   id of the authorization to be updated
     * @param request           contains data for updating authorization
     * @param authorizationType type of authorization required to update. Can be  CREATED or CANCELLED
     * @return response contains updated data
     */
    @Transactional
    public Optional<UpdatePisConsentPsuDataResponse> updateConsentAuthorization(String authorizationId, UpdatePisConsentPsuDataRequest request, CmsAuthorisationType authorizationType) {
        Optional<PisConsentAuthorization> pisConsentAuthorisationOptional = pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(
            authorizationId, authorizationType);
        if (pisConsentAuthorisationOptional.isPresent()) {
            PisConsentAuthorization consentAuthorization = pisConsentAuthorisationOptional.get();

            if (SCAMETHODSELECTED == request.getScaStatus()) {
                String chosenMethod = request.getAuthenticationMethodId();
                if (StringUtils.isNotBlank(chosenMethod)) {
                    consentAuthorization.setChosenScaMethod(CmsScaMethod.valueOf(chosenMethod));
                }
            }
            consentAuthorization.setScaStatus(request.getScaStatus());
            pisConsentAuthorizationRepository.save(consentAuthorization);
        }
        return pisConsentAuthorisationOptional.map(pisConsentMapper::mapToUpdatePisConsentPsuDataResponse);
    }

    /**
     * Update PIS consent payment data and stores it into database
     *
     * @param request            PIS consent request for update payment data
     * @param encryptedConsentId encrypted Consent ID
     */
    // TODO return correct error code in case consent was not found https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/408
    @Transactional
    public void updatePaymentConsent(PisConsentRequest request, String encryptedConsentId) {
        Optional<PisConsent> pisConsentById = getPisConsentById(encryptedConsentId);
        pisConsentById
            .ifPresent(pisConsent -> pisPaymentDataRepository.save(pisConsentMapper.mapToPisPaymentDataList(request.getPayments(), pisConsent)));
    }

    /**
     * Reads authorization data by authorization Id
     *
     * @param authorizationId   id of the authorization to be updated
     * @param authorizationType type of authorization. Can be  CREATED or CANCELLED
     * @return response contains authorization data
     */
    public Optional<GetPisConsentAuthorisationResponse> getPisConsentAuthorizationById(String authorizationId, CmsAuthorisationType authorizationType) {
        return pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(authorizationId, authorizationType)
                   .map(pisConsentMapper::mapToGetPisConsentAuthorizationResponse);
    }

    /**
     * Reads authorization id data by encrypted payment Id and type of authorization
     *
     * @param encryptedPaymentId encrypted id of the payment
     * @param authorizationType  type of authorization required to create. Can be  CREATED or CANCELLED
     * @return response contains authorization id
     */
    public Optional<String> getAuthorisationId(String encryptedPaymentId, CmsAuthorisationType authorizationType) {
        Optional<String> paymentId = securityDataService.getDecryptedId(encryptedPaymentId);
        if (!paymentId.isPresent()) {
            log.warn("Payment Id has not encrypted: {}", encryptedPaymentId);
            return Optional.empty();
        }

        return pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentId.get(), RECEIVED)
                   .flatMap(list -> pisConsentAuthorizationRepository.findByConsentIdAndAuthorizationType(list.get(0).getConsent().getId(), authorizationType))
                   .filter(CollectionUtils::isNotEmpty)
                   .map(lst -> lst.get(0).getExternalId());
    }

    private Optional<PisConsent> getActualPisConsent(String encryptedConsentId) {
        Optional<String> consentIdDecrypted = securityDataService.getDecryptedId(encryptedConsentId);
        if (!consentIdDecrypted.isPresent()) {
            log.warn("Consent Id has not encrypted: {}", encryptedConsentId);
        }

        return consentIdDecrypted
                   .flatMap(id -> pisConsentRepository.findByExternalIdAndConsentStatusIn(id, EnumSet.of(RECEIVED, VALID)));
    }

    private Optional<PisConsent> getPisConsentById(String encryptedConsentId) {
        Optional<String> consentIdDecrypted = securityDataService.getDecryptedId(encryptedConsentId);
        if (!consentIdDecrypted.isPresent()) {
            log.warn("Consent Id has not encrypted: {}", encryptedConsentId);
        }

        return consentIdDecrypted
                   .flatMap(pisConsentRepository::findByExternalId);
    }

    private PisConsent setStatusAndSaveConsent(PisConsent consent, CmsConsentStatus status) {
        consent.setConsentStatus(status);
        return pisConsentRepository.save(consent);
    }

    /**
     * Creates PIS consent authorization entity and stores it into database
     *
     * @param pisConsent PIS Consent, for which authorization is performed
     * @return PisConsentAuthorization
     */
    private PisConsentAuthorization saveNewAuthorization(PisConsent pisConsent, CmsAuthorisationType authorizationType) {
        PisConsentAuthorization consentAuthorization = new PisConsentAuthorization();
        consentAuthorization.setExternalId(UUID.randomUUID().toString());
        consentAuthorization.setConsent(pisConsent);
        consentAuthorization.setScaStatus(STARTED);
        consentAuthorization.setAuthorizationType(authorizationType);
        return pisConsentAuthorizationRepository.save(consentAuthorization);
    }

    private Optional<String> saveAspspConsentDataInPisConsent(CmsAspspConsentDataBase64 request, PisConsent consent, String encryptedConsentId) {
        return securityDataService.encryptConsentData(encryptedConsentId, request.getAspspConsentDataBase64())
                   .map(encr -> updateConsentDataAndSaveConsent(consent, encr.getData()))
                   .map(PisConsent::getExternalId);
    }

    private PisConsent updateConsentDataAndSaveConsent(PisConsent consent, byte[] consentData) {
        consent.setAspspConsentData(consentData);
        return pisConsentRepository.save(consent);
    }
}
