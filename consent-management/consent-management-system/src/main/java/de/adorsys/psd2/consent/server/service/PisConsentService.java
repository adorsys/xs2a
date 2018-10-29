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
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.STARTED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PisConsentService {
    private final PisConsentRepository pisConsentRepository;
    private final PisConsentMapper pisConsentMapper;
    private final PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    private final PisPaymentDataRepository pisPaymentDataRepository;

    /**
     * Creates new pis consent with full information about payment
     *
     * @param request Consists information about payments.
     * @return Response containing identifier of consent
     */
    @Transactional
    public Optional<CreatePisConsentResponse> createPaymentConsent(PisConsentRequest request) {
        PisConsent consent = pisConsentMapper.mapToPisConsent(request);
        consent.setExternalId(UUID.randomUUID().toString());
        PisConsent saved = pisConsentRepository.save(consent);
        return Optional.of(new CreatePisConsentResponse(saved.getExternalId()));
    }

    /**
     * Retrieves consent status from pis consent by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @return Information about the status of a consent
     */
    public Optional<ConsentStatus> getConsentStatusById(String consentId) {
        return getPisConsentById(consentId)
                   .map(PisConsent::getConsentStatus);
    }

    /**
     * Reads full information of pis consent by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @return Response containing full information about pis consent
     */
    public Optional<PisConsentResponse> getConsentById(String consentId) {
        return getPisConsentById(consentId)
                   .flatMap(pisConsentMapper::mapToPisConsentResponse);
    }

    /**
     * Updates pis consent status by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @param status    new consent status
     * @return Response containing result of status changing
     */
    @Transactional
    public Optional<Boolean> updateConsentStatusById(String consentId, ConsentStatus status) {
        return getActualPisConsent(consentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    /**
     * Get Pis aspsp consent data by consent id
     *
     * @param consentId id of the consent
     * @return Response containing aspsp consent data
     */
    public Optional<CmsAspspConsentDataBase64> getAspspConsentDataByConsentId(String consentId) {
        return getPisConsentById(consentId)
                   .map(this::prepareAspspConsentData);
    }

    /**
     * Get Pis aspsp consent data by payment id
     *
     * @param paymentId id of the payment
     * @return Response containing aspsp consent data
     */
    public Optional<CmsAspspConsentDataBase64> getAspspConsentDataByPaymentId(String paymentId) {
        return pisPaymentDataRepository.findByPaymentId(paymentId)
                   .map(l -> l.get(0))
                   .map(PisPaymentData::getConsent)
                   .map(this::prepareAspspConsentData);
    }

    private CmsAspspConsentDataBase64 prepareAspspConsentData(PisConsent consent) {
        CmsAspspConsentDataBase64 response = new CmsAspspConsentDataBase64();
        String aspspConsentDataBase64 = Optional.ofNullable(consent.getAspspConsentData())
                                            .map(bytes -> Base64.getEncoder().encodeToString(bytes))
                                            .orElse(null);
        response.setAspspConsentDataBase64(aspspConsentDataBase64);
        response.setConsentId(consent.getExternalId());
        return response;
    }

    /**
     * Update PIS consent aspsp consent data by id
     *
     * @param request   Aspsp provided pis consent data
     * @param consentId id of the consent to be updated
     * @return String consent id
     */
    @Transactional
    public Optional<String> updateAspspConsentDataInPisConsent(String consentId, CmsAspspConsentDataBase64 request) {
        return getActualPisConsent(consentId)
                   .map(cons -> updateAspspConsentDataInPisConsent(request, cons));
    }

    /**
     * Create consent authorization
     */
    @Transactional
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorization(String paymentId, CmsAuthorisationType authorizationType) {
        return pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentId, RECEIVED)
                   .map(list -> saveNewAuthorization(list.get(0).getConsent(), authorizationType))
                   .map(c -> new CreatePisConsentAuthorisationResponse(c.getExternalId()));
    }

    @Transactional
    public Optional<UpdatePisConsentPsuDataResponse> updateConsentAuthorization(String authorizationId, UpdatePisConsentPsuDataRequest request, CmsAuthorisationType authorizationType) {
        Optional<PisConsentAuthorization> pisConsentAuthorisationOptional = pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(
            authorizationId, authorizationType);
        if (pisConsentAuthorisationOptional.isPresent()) {
            PisConsentAuthorization consentAuthorization = pisConsentAuthorisationOptional.get();

            if (SCAMETHODSELECTED == request.getScaStatus()) {
                String chosenMethod = request.getAuthenticationMethodId();
                if (StringUtils.isNotBlank(chosenMethod)) {
                    consentAuthorization.setChosenScaMethod(chosenMethod);
                }
            }
            consentAuthorization.setScaStatus(request.getScaStatus());
            pisConsentAuthorizationRepository.save(consentAuthorization);
        }
        return pisConsentAuthorisationOptional.map(p -> new UpdatePisConsentPsuDataResponse(p.getScaStatus()));
    }

    /**
     * Update PIS consent payment data and stores it into database
     *
     * @param request   PIS consent request for update payment data
     * @param consentId Consent ID
     */
    // TODO return correct error code in case consent was not found https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/408
    @Transactional
    public void updatePaymentConsent(PisConsentRequest request, String consentId) {
        Optional<PisConsent> pisConsentById = getPisConsentById(consentId);
        pisConsentById.ifPresent(pisConsent -> pisPaymentDataRepository.save(pisConsentMapper.mapToPisPaymentDataList(request.getPayments(), pisConsent)));
    }

    public Optional<GetPisConsentAuthorisationResponse> getPisConsentAuthorizationById(String authorizationId, CmsAuthorisationType authorizationType) {
        return pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(authorizationId, authorizationType)
                   .map(pisConsentMapper::mapToGetPisConsentAuthorizationResponse);
    }

    public Optional<String> getAuthorisationByPaymentId(String paymentId, CmsAuthorisationType authorizationType) {
        return pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentId, RECEIVED)
                   .flatMap(list -> pisConsentAuthorizationRepository.findByConsentIdAndAuthorizationType(list.get(0).getConsent().getId(), authorizationType))
                   .filter(CollectionUtils::isNotEmpty)
                   .map(lst -> lst.get(0).getExternalId());
    }

    private Optional<PisConsent> getPisConsentById(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(pisConsentRepository::findByExternalId);
    }

    private PisConsent setStatusAndSaveConsent(PisConsent consent, ConsentStatus status) {
        consent.setConsentStatus(status);
        return pisConsentRepository.save(consent);
    }

    private Optional<PisConsent> getActualPisConsent(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(c -> pisConsentRepository.findByExternalIdAndConsentStatusIn(consentId, EnumSet.of(RECEIVED, VALID)));
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

    private String updateAspspConsentDataInPisConsent(CmsAspspConsentDataBase64 request, PisConsent consent) {
        byte[] aspspConsentData = Optional.ofNullable(request.getAspspConsentDataBase64())
                                      .map(aspspConsentDataBase64 -> Base64.getDecoder().decode(aspspConsentDataBase64))
                                      .orElse(null);
        consent.setAspspConsentData(aspspConsentData);
        PisConsent savedConsent = pisConsentRepository.save(consent);
        return savedConsent.getExternalId();
    }
}
