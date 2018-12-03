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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.consent.domain.payment.PisConsent;
import de.adorsys.psd2.consent.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.PisConsentRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.mapper.PisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.STARTED;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PisConsentServiceInternal implements PisConsentService {
    private final PisConsentRepository pisConsentRepository;
    private final PisConsentMapper pisConsentMapper;
    private final PsuDataMapper psuDataMapper;
    private final PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final SecurityDataService securityDataService;

    /**
     * Creates new pis consent with full information about payment
     *
     * @param request Consists information about payments.
     * @return Response containing identifier of consent
     */
    @Override
    @Transactional
    public Optional<CreatePisConsentResponse> createPaymentConsent(PisConsentRequest request) {
        PisConsent consent = pisConsentMapper.mapToPisConsent(request);
        String externalId = UUID.randomUUID().toString();
        consent.setExternalId(externalId);

        PisConsent saved = pisConsentRepository.save(consent);

        return Optional.ofNullable(saved.getId())
                   .flatMap(id -> securityDataService.encryptId(saved.getExternalId()))
                   .map(CreatePisConsentResponse::new);
    }

    /**
     * Retrieves consent status from pis consent by consent identifier
     *
     * @param encryptedConsentId String representation of pis consent identifier
     * @return Information about the status of a consent
     */
    @Override
    public Optional<ConsentStatus> getConsentStatusById(String encryptedConsentId) {
        return getPisConsentById(encryptedConsentId)
                   .map(PisConsent::getConsentStatus);
    }

    /**
     * Reads full information of pis consent by consent identifier
     *
     * @param encryptedConsentId String representation of pis encrypted consent identifier
     * @return Response containing full information about pis consent
     */
    @Override
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
    @Override
    @Transactional
    public Optional<Boolean> updateConsentStatusById(String encryptedConsentId, ConsentStatus status) {
        return getActualPisConsent(encryptedConsentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    /**
     * Get original decrypted Id from encrypted string
     *
     * @param encryptedId id to be decrypted
     * @return Response containing original decrypted Id
     */
    @Override
    public Optional<String> getDecryptedId(String encryptedId) {
        return securityDataService.decryptId(encryptedId);
    }

    /**
     * Create consent authorization
     *
     * @param encryptedPaymentId encrypted id of the payment
     * @param authorizationType  type of authorization required to create. Can be  CREATED or CANCELLED
     * @return response contains authorization id
     */
    @Override
    @Transactional
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorization(String encryptedPaymentId, CmsAuthorisationType authorizationType, PsuIdData psuData) {
        Optional<String> paymentId = securityDataService.decryptId(encryptedPaymentId);
        if (!paymentId.isPresent()) {
            log.warn("Payment Id has not encrypted: {}", encryptedPaymentId);
            return Optional.empty();
        }

        return pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentId.get(), RECEIVED)
                   .map(list -> saveNewAuthorization(list.get(0).getConsent(), authorizationType, psuData))
                   .map(c -> new CreatePisConsentAuthorisationResponse(c.getExternalId()));
    }

    @Override
    @Transactional
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorizationCancellation(String paymentId, CmsAuthorisationType authorizationType, PsuIdData psuData) {
        return createAuthorization(paymentId, authorizationType, psuData);
    }

    /**
     * Update consent authorisation
     *
     * @param authorizationId id of the authorisation to be updated
     * @param request         contains data for updating authorisation
     * @return response contains updated data
     */
    @Override
    @Transactional
    public Optional<UpdatePisConsentPsuDataResponse> updateConsentAuthorisation(String authorizationId, UpdatePisConsentPsuDataRequest request) {
        Optional<PisConsentAuthorization> pisConsentAuthorisationOptional = pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(
            authorizationId, CmsAuthorisationType.CREATED);

        if (pisConsentAuthorisationOptional.isPresent()) {
            ScaStatus scaStatus = doUpdateConsentAuthorisation(request, pisConsentAuthorisationOptional.get());
            return Optional.of(new UpdatePisConsentPsuDataResponse(scaStatus));
        }

        return Optional.empty();
    }

    /**
     * Update consent cancellation authorisation
     *
     * @param cancellationId id of the authorisation to be updated
     * @param request        contains data for updating authorisation
     * @return response contains updated data
     */
    @Override
    @Transactional
    public Optional<UpdatePisConsentPsuDataResponse> updateConsentCancellationAuthorisation(String cancellationId, UpdatePisConsentPsuDataRequest request) {
        Optional<PisConsentAuthorization> pisConsentAuthorisationOptional = pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(
            cancellationId, CmsAuthorisationType.CANCELLED);

        if (pisConsentAuthorisationOptional.isPresent()) {
            ScaStatus scaStatus = doUpdateConsentAuthorisation(request, pisConsentAuthorisationOptional.get());
            return Optional.of(new UpdatePisConsentPsuDataResponse(scaStatus));
        }

        return Optional.empty();
    }

    /**
     * Update PIS consent payment data and stores it into database
     *
     * @param request            PIS consent request for update payment data
     * @param encryptedConsentId encrypted Consent ID
     */
    // TODO return correct error code in case consent was not found https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/408
    @Override
    @Transactional
    public void updatePaymentConsent(PisConsentRequest request, String encryptedConsentId) {
        Optional<PisConsent> pisConsentById = getPisConsentById(encryptedConsentId);
        pisConsentById
            .ifPresent(pisConsent -> pisPaymentDataRepository.save(pisConsentMapper.mapToPisPaymentDataList(request.getPayments(), pisConsent)));
    }

    /**
     * Reads authorisation data by authorisation Id
     *
     * @param authorisationId id of the authorisation
     * @return response contains authorisation data
     */
    @Override
    public Optional<GetPisConsentAuthorisationResponse> getPisConsentAuthorisationById(String authorisationId) {
        return pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(authorisationId, CmsAuthorisationType.CREATED)
                   .map(pisConsentMapper::mapToGetPisConsentAuthorizationResponse);
    }

    /**
     * Reads cancellation authorisation data by cancellation Id
     *
     * @param cancellationId id of the authorisation
     * @return response contains authorisation data
     */
    @Override
    public Optional<GetPisConsentAuthorisationResponse> getPisConsentCancellationAuthorisationById(String cancellationId) {
        return pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(cancellationId, CmsAuthorisationType.CANCELLED)
                   .map(pisConsentMapper::mapToGetPisConsentAuthorizationResponse);
    }

    /**
     * Reads authorisation IDs data by encrypted payment Id and type of authorization
     *
     * @param encryptedPaymentId encrypted id of the payment
     * @param authorisationType  type of authorization required to create. Can be  CREATED or CANCELLED
     * @return response contains authorisation IDs
     */
    @Override
    public Optional<List<String>> getAuthorisationsByPaymentId(String encryptedPaymentId, CmsAuthorisationType authorisationType) {
        Optional<String> paymentId = securityDataService.decryptId(encryptedPaymentId);
        if (!paymentId.isPresent()) {
            log.warn("Payment Id has not encrypted: {}", encryptedPaymentId);
            return Optional.empty();
        }

        Optional<List<PisPaymentData>> paymentDataListOptional = pisPaymentDataRepository.findByPaymentId(paymentId.get());
        if (!paymentDataListOptional.isPresent()) {
            return Optional.empty();
        }

        List<PisPaymentData> paymentDataList = paymentDataListOptional.get();
        if (paymentDataList.isEmpty()) {
            return Optional.of(Collections.emptyList());
        }

        List<String> authorisationIds = paymentDataList.get(0).getConsent()
                                            .getAuthorizations()
                                            .stream()
                                            .map(PisConsentAuthorization::getExternalId)
                                            .collect(Collectors.toList());

        return Optional.of(authorisationIds);
    }

    /**
     * Reads Psu data by encrypted payment Id
     *
     * @param encryptedPaymentId encrypted id of the payment
     * @return response contains data of Psu
     */
    @Override
    public Optional<PsuIdData> getPsuDataByPaymentId(String encryptedPaymentId) {
        Optional<String> paymentId = securityDataService.decryptId(encryptedPaymentId);
        if (!paymentId.isPresent()) {
            log.warn("Payment Id has not encrypted: {}", encryptedPaymentId);
            return Optional.empty();
        }

        return pisPaymentDataRepository.findByPaymentId(paymentId.get())
                   .map(l -> l.get(0))
                   .map(PisPaymentData::getConsent)
                   .map(pc -> psuDataMapper.mapToPsuIdData(pc.getPsuData()));
    }

    /**
     * Reads Psu data by encrypted consent Id
     *
     * @param encryptedConsentId encrypted Consent ID
     * @return response contains data of Psu
     */
    @Override
    public Optional<PsuIdData> getPsuDataByConsentId(String encryptedConsentId) {
        return getPisConsentById(encryptedConsentId)
                   .map(pc -> psuDataMapper.mapToPsuIdData(pc.getPsuData()));
    }

    private Optional<PisConsent> getActualPisConsent(String encryptedConsentId) {
        Optional<String> consentIdDecrypted = securityDataService.decryptId(encryptedConsentId);
        if (!consentIdDecrypted.isPresent()) {
            log.warn("Consent Id has not encrypted: {}", encryptedConsentId);
        }

        return consentIdDecrypted
                   .flatMap(pisConsentRepository::findByExternalId)
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private Optional<PisConsent> getPisConsentById(String encryptedConsentId) {
        Optional<String> consentIdDecrypted = securityDataService.decryptId(encryptedConsentId);
        if (!consentIdDecrypted.isPresent()) {
            log.warn("Consent Id has not encrypted: {}", encryptedConsentId);
        }

        return consentIdDecrypted
                   .flatMap(pisConsentRepository::findByExternalId);
    }

    private PisConsent setStatusAndSaveConsent(PisConsent consent, ConsentStatus status) {
        consent.setConsentStatus(status);
        return pisConsentRepository.save(consent);
    }

    /**
     * Creates PIS consent authorization entity and stores it into database
     *
     * @param pisConsent PIS Consent, for which authorization is performed
     * @return PisConsentAuthorization
     */
    private PisConsentAuthorization saveNewAuthorization(PisConsent pisConsent, CmsAuthorisationType authorizationType, PsuIdData psuData) {
        PisConsentAuthorization consentAuthorization = new PisConsentAuthorization();
        consentAuthorization.setExternalId(UUID.randomUUID().toString());
        consentAuthorization.setConsent(pisConsent);
        consentAuthorization.setScaStatus(STARTED);
        consentAuthorization.setAuthorizationType(authorizationType);
        consentAuthorization.setPsuData(psuDataMapper.mapToPsuData(psuData));
        return pisConsentAuthorizationRepository.save(consentAuthorization);
    }

    private ScaStatus doUpdateConsentAuthorisation(UpdatePisConsentPsuDataRequest request, PisConsentAuthorization pisConsentAuthorisation) {
        if (pisConsentAuthorisation.getScaStatus().isFinalisedStatus()) {
            return pisConsentAuthorisation.getScaStatus();
        }

        if (SCAMETHODSELECTED == request.getScaStatus()) {
            String chosenMethod = request.getAuthenticationMethodId();
            if (StringUtils.isNotBlank(chosenMethod)) {
                pisConsentAuthorisation.setChosenScaMethod(chosenMethod);
            }
        }
        pisConsentAuthorisation.setScaStatus(request.getScaStatus());
        PisConsentAuthorization saved = pisConsentAuthorizationRepository.save(pisConsentAuthorisation);
        return saved.getScaStatus();
    }
}
