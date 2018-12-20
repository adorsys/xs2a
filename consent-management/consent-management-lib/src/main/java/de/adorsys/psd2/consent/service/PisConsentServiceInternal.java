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

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisConsent;
import de.adorsys.psd2.consent.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.PisConsentRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.mapper.PisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final AspspProfileService aspspProfileService;

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
        if (saved.getId() == null) {
            return Optional.empty();
        }

        return Optional.of(new CreatePisConsentResponse(saved.getExternalId()));
    }

    /**
     * Retrieves consent status from pis consent by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @return Information about the status of a consent
     */
    @Override
    public Optional<ConsentStatus> getConsentStatusById(String consentId) {
        return pisConsentRepository.findByExternalId(consentId)
                   .map(PisConsent::getConsentStatus);
    }

    /**
     * Reads full information of pis consent by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @return Response containing full information about pis consent
     */
    @Override
    public Optional<PisConsentResponse> getConsentById(String consentId) {
        return pisConsentRepository.findByExternalId(consentId)
                   .flatMap(pisConsentMapper::mapToPisConsentResponse);
    }

    /**
     * Updates pis consent status by consent identifier
     *
     * @param consentId String representation of pis consent identifier
     * @param status    new consent status
     * @return Response containing result of status changing
     */
    @Override
    @Transactional
    public Optional<Boolean> updateConsentStatusById(String consentId, ConsentStatus status) {
        return getActualPisConsent(consentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    /**
     * Create consent authorization
     *
     * @param paymentId         id of the payment
     * @param authorizationType type of authorization required to create. Can be  CREATED or CANCELLED
     * @return response contains authorization id
     */
    @Override
    @Transactional
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorization(String paymentId, CmsAuthorisationType authorizationType,
                                                                               PsuIdData psuData) {
        return readReceivedConsentByPaymentId(paymentId)
                   .map(pisConsent -> saveNewAuthorisation(pisConsent, authorizationType))
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
     * @param request   PIS consent request for update payment data
     * @param consentId Consent ID
     */
    // TODO return correct error code in case consent was not found https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/408
    @Override
    @Transactional
    public void updatePaymentConsent(PisConsentRequest request, String consentId) {
        Optional<PisConsent> pisConsentById = pisConsentRepository.findByExternalId(consentId);
        pisConsentById
            .ifPresent(pisConsent -> savePaymentData(pisConsent, request));
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
     * Reads authorisation IDs data by payment Id and type of authorization
     *
     * @param paymentId         id of the payment
     * @param authorisationType type of authorization required to create. Can be  CREATED or CANCELLED
     * @return response contains authorisation IDs
     */
    @Override
    public Optional<List<String>> getAuthorisationsByPaymentId(String paymentId, CmsAuthorisationType authorisationType) {
        return readReceivedConsentByPaymentId(paymentId)
                   .map(cnst -> readAuthorisationsFromConsent(cnst, authorisationType));
    }

    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId, CmsAuthorisationType authorisationType) {
        Optional<PisConsent> consent = readPisConsentByPaymentId(paymentId);
        if (!consent.isPresent()) {
            return Optional.empty();
        }

        Optional<PisConsentAuthorization> authorisation = findAuthorisationInConsent(authorisationId,
                                                                                     consent.get(),
                                                                                     authorisationType);
        return authorisation.map(PisConsentAuthorization::getScaStatus);
    }

    /**
     * Reads Psu data by payment Id
     *
     * @param paymentId Payment ID
     * @return response contains data of Psu
     */
    @Override
    public Optional<PsuIdData> getPsuDataByPaymentId(String paymentId) {
        return readPisConsentByPaymentId(paymentId)
                   .map(pc -> psuDataMapper.mapToPsuIdData(pc.getPsuData()));
    }

    /**
     * Reads Psu data by consent Id
     *
     * @param consentId Consent ID
     * @return response contains data of Psu
     */
    @Override
    public Optional<PsuIdData> getPsuDataByConsentId(String consentId) {
        return pisConsentRepository.findByExternalId(consentId)
                   .map(pc -> psuDataMapper.mapToPsuIdData(pc.getPsuData()));
    }

    private Optional<PisConsent> getActualPisConsent(String consentId) {
        return pisConsentRepository.findByExternalId(consentId)
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private PisConsent setStatusAndSaveConsent(PisConsent consent, ConsentStatus status) {
        consent.setConsentStatus(status);
        return pisConsentRepository.save(consent);
    }

    private Optional<PisConsent> readReceivedConsentByPaymentId(String paymentId) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        Optional<PisConsent> consentOpt = pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentId, RECEIVED)
                                              .filter(CollectionUtils::isNotEmpty)
                                              .map(list -> list.get(0).getConsent());

        if (!consentOpt.isPresent()) {
            consentOpt = pisCommonPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentId, RECEIVED)
                             .map(PisCommonPaymentData::getConsent);
        }

        return consentOpt;
    }

    private Optional<PisConsent> readPisConsentByPaymentId(String paymentId) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        Optional<PisConsent> consentOpt = pisPaymentDataRepository.findByPaymentId(paymentId)
                                              .filter(CollectionUtils::isNotEmpty)
                                              .map(list -> list.get(0).getConsent());

        if (!consentOpt.isPresent()) {
            consentOpt = pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                             .map(PisCommonPaymentData::getConsent);
        }

        return consentOpt;
    }

    private void savePaymentData(PisConsent pisConsent, PisConsentRequest request) {
        boolean isCommonPayment = CollectionUtils.isEmpty(request.getPayments()) && request.getPaymentInfo() != null;
        // todo implementation should be changed  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534

        if (isCommonPayment) {
            pisCommonPaymentDataRepository.save(pisConsentMapper.mapToPisCommonPaymentData(request.getPaymentInfo(), pisConsent));
        } else {
            pisPaymentDataRepository.save(pisConsentMapper.mapToPisPaymentDataList(request.getPayments(), pisConsent));
        }
    }

    /**
     * Creates PIS consent authorisation entity and stores it into database
     *
     * @param pisConsent PIS Consent, for which authorisation is performed
     * @return PisConsentAuthorization
     */
    private PisConsentAuthorization saveNewAuthorisation(PisConsent pisConsent, CmsAuthorisationType authorisationType) {
        PisConsentAuthorization consentAuthorization = new PisConsentAuthorization();
        consentAuthorization.setExternalId(UUID.randomUUID().toString());
        consentAuthorization.setConsent(pisConsent);
        consentAuthorization.setScaStatus(STARTED);
        consentAuthorization.setAuthorizationType(authorisationType);
        consentAuthorization.setPsuData(pisConsent.getPsuData());
        consentAuthorization.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().plus(aspspProfileService.getAspspSettings().getRedirectUrlExpirationTimeMs(), ChronoUnit.MILLIS));
        return pisConsentAuthorizationRepository.save(consentAuthorization);
    }

    private List<String> readAuthorisationsFromConsent(PisConsent pisConsent, CmsAuthorisationType authorisationType) {
        return pisConsent.getAuthorizations()
                   .stream()
                   .filter(auth -> auth.getAuthorizationType() == authorisationType)
                   .map(PisConsentAuthorization::getExternalId)
                   .collect(Collectors.toList());
    }

    private Optional<PisConsentAuthorization> findAuthorisationInConsent(String authorisationId, PisConsent pisConsent, CmsAuthorisationType authorisationType) {
        return pisConsent.getAuthorizations()
                   .stream()
                   .filter(auth -> auth.getAuthorizationType() == authorisationType)
                   .filter(auth -> auth.getExternalId().equals(authorisationId))
                   .findFirst();
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
