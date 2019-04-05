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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.ScaMethod;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisAuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.ScaMethodMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.PATC;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RCVD;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.STARTED;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PisCommonPaymentServiceInternal implements PisCommonPaymentService {
    private final PisCommonPaymentMapper pisCommonPaymentMapper;
    private final PsuDataMapper psuDataMapper;
    private final PisAuthorisationRepository pisAuthorisationRepository;
    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final AspspProfileService aspspProfileService;
    private final PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;
    private final ScaMethodMapper scaMethodMapper;
    private final CmsPsuService cmsPsuService;

    /**
     * Creates new pis common payment with full information about payment
     *
     * @param request Consists information about payments.
     * @return Response containing identifier of common payment
     */
    @Override
    @Transactional
    public Optional<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        PisCommonPaymentData commonPaymentData = pisCommonPaymentMapper.mapToPisCommonPaymentData(request);
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(commonPaymentData);

        if (saved.getId() == null) {
            log.info("Payment ID: [{}]. Pis common payment cannot be created, because when saving to DB got null PisCommonPaymentData ID",
                     request.getPaymentId());
            return Optional.empty();
        }

        return Optional.of(new CreatePisCommonPaymentResponse(saved.getPaymentId()));
    }

    /**
     * Retrieves common payment status from pis common payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Information about the status of a common payment
     */
    @Override
    @Transactional
    public Optional<TransactionStatus> getPisCommonPaymentStatusById(String paymentId) {
        return pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                   .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdatePaymentDataOnConfirmationExpiration)
                   .map(PisCommonPaymentData::getTransactionStatus);
    }

    /**
     * Reads full information of pis common payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Response containing full information about pis common payment
     */
    @Override
    @Transactional
    public Optional<PisCommonPaymentResponse> getCommonPaymentById(String paymentId) {
        return pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                   .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdatePaymentDataOnConfirmationExpiration)
                   .flatMap(pisCommonPaymentMapper::mapToPisCommonPaymentResponse);
    }

    /**
     * Updates pis common payment status by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @param status    new common payment status
     * @return Response containing result of status changing
     */
    @Override
    @Transactional
    public Optional<Boolean> updateCommonPaymentStatusById(String paymentId, TransactionStatus status) {
        return pisCommonPaymentDataRepository.findByPaymentId(paymentId)
                   .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdatePaymentDataOnConfirmationExpiration)
                   .filter(pm -> !pm.getTransactionStatus().isFinalisedStatus())
                   .map(pmt -> setStatusAndSaveCommonPaymentData(pmt, status))
                   .map(con -> con.getTransactionStatus() == status);
    }

    /**
     * Create common payment authorization
     *
     * @param paymentId id of the payment
     * @param request   needed parameters for creating PIS authorisation
     * @return response contains authorisation id
     */
    @Override
    @Transactional
    public Optional<CreatePisAuthorisationResponse> createAuthorization(String paymentId, CreatePisAuthorisationRequest request) {
        return readReceivedCommonPaymentDataByPaymentId(paymentId)
                   .map(pmt -> {
                       closePreviousAuthorisationsByPsu(pmt.getAuthorizations(), request.getAuthorizationType(), request.getPsuData());
                       return saveNewAuthorisation(pmt, request);
                   })
                   .map(c -> new CreatePisAuthorisationResponse(c.getExternalId()));
    }

    @Override
    @Transactional
    public Optional<CreatePisAuthorisationResponse> createAuthorizationCancellation(String paymentId, CreatePisAuthorisationRequest request) {
        return readPisCommonPaymentDataByPaymentId(paymentId)
                   .filter(p -> p.getTransactionStatus().isNotFinalisedStatus())
                   .map(pmt -> {
                       closePreviousAuthorisationsByPsu(pmt.getAuthorizations(), request.getAuthorizationType(), request.getPsuData());
                       return saveNewAuthorisation(pmt, request);
                   })
                   .map(c -> new CreatePisAuthorisationResponse(c.getExternalId()));
    }

    /**
     * Update common payment authorisation
     *
     * @param authorisationId id of the authorisation to be updated
     * @param request         contains data for updating authorisation
     * @return response contains updated data
     */
    @Override
    @Transactional
    public Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(String authorisationId, UpdatePisCommonPaymentPsuDataRequest request) {
        Optional<PisAuthorization> pisAuthorisationOptional = pisAuthorisationRepository.findByExternalIdAndAuthorizationType(
            authorisationId, CmsAuthorisationType.CREATED);

        if (!pisAuthorisationOptional.isPresent()) {
            log.info("Authorisation ID: [{}]. Update pis authorisation failed, because pis authorisation with CmsAuthorisationType.CREATED is not found by id",
                     authorisationId);
            return Optional.empty();
        }

        PisAuthorization authorisation = pisAuthorisationOptional.get();
        closePreviousAuthorisationsByPsu(authorisation, request.getPsuData());

        ScaStatus scaStatus = doUpdateConsentAuthorisation(request, authorisation);
        return Optional.of(new UpdatePisCommonPaymentPsuDataResponse(scaStatus));
    }

    /**
     * Update common payment cancellation authorisation
     *
     * @param cancellationId id of the authorisation to be updated
     * @param request        contains data for updating authorisation
     * @return response contains updated data
     */
    @Override
    @Transactional
    public Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorisation(String cancellationId, UpdatePisCommonPaymentPsuDataRequest request) {
        Optional<PisAuthorization> pisAuthorisationOptional = pisAuthorisationRepository.findByExternalIdAndAuthorizationType(
            cancellationId, CmsAuthorisationType.CANCELLED);

        if (!pisAuthorisationOptional.isPresent()) {
            log.info("Cancellation ID: [{}]. Update pis cancellation authorisation failed, because pis authorisation with CmsAuthorisationType.CANCELLED is not found by id",
                     cancellationId);
            return Optional.empty();
        }

        PisAuthorization authorisation = pisAuthorisationOptional.get();
        closePreviousAuthorisationsByPsu(authorisation, request.getPsuData());

        ScaStatus scaStatus = doUpdateConsentAuthorisation(request, authorisation);
        return Optional.of(new UpdatePisCommonPaymentPsuDataResponse(scaStatus));
    }

    /**
     * Update PIS common payment payment data and stores it into database
     *
     * @param request   PIS common payment request for update payment data
     * @param paymentId common payment ID
     */
    // TODO return correct error code in case consent was not found https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/408
    @Override
    @Transactional
    public void updateCommonPayment(PisCommonPaymentRequest request, String paymentId) {
        Optional<PisCommonPaymentData> pisCommonPaymentById = pisCommonPaymentDataRepository.findByPaymentId(paymentId);
        pisCommonPaymentById
            .ifPresent(commonPayment -> savePaymentData(commonPayment, request));
    }

    /**
     * Reads authorisation data by authorisation Id
     *
     * @param authorisationId id of the authorisation
     * @return response contains authorisation data
     */
    @Override
    public Optional<GetPisAuthorisationResponse> getPisAuthorisationById(String authorisationId) {
        return pisAuthorisationRepository.findByExternalIdAndAuthorizationType(authorisationId, CmsAuthorisationType.CREATED)
                   .map(pisCommonPaymentMapper::mapToGetPisAuthorizationResponse);
    }

    /**
     * Reads cancellation authorisation data by cancellation Id
     *
     * @param cancellationId id of the authorisation
     * @return response contains authorisation data
     */
    @Override
    public Optional<GetPisAuthorisationResponse> getPisCancellationAuthorisationById(String cancellationId) {
        return pisAuthorisationRepository.findByExternalIdAndAuthorizationType(cancellationId, CmsAuthorisationType.CANCELLED)
                   .map(pisCommonPaymentMapper::mapToGetPisAuthorizationResponse);
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
        return readReceivedCommonPaymentDataByPaymentId(paymentId)
                   .map(pmt -> readAuthorisationsFromPaymentCommonData(pmt, authorisationType));
    }

    @Override
    @Transactional
    public Optional<ScaStatus> getAuthorisationScaStatus(@NotNull String paymentId, @NotNull String authorisationId, CmsAuthorisationType authorisationType) {
        Optional<PisAuthorization> authorisationOptional = pisAuthorisationRepository.findByExternalIdAndAuthorizationType(authorisationId, authorisationType);

        if (!authorisationOptional.isPresent()) {
            log.info("Authorisation ID: [{}], Authorisation Type: [{}]. Get authorisation SCA status failed, because authorisation is not found",
                     authorisationId, authorisationType);
            return Optional.empty();
        }

        PisCommonPaymentData paymentData = authorisationOptional.get().getPaymentData();
        if (pisCommonPaymentConfirmationExpirationService.isPaymentDataOnConfirmationExpired(paymentData)) {
            pisCommonPaymentConfirmationExpirationService.updatePaymentDataOnConfirmationExpiration(paymentData);
            log.info("Payment ID: [{}]. Get authorisation SCA status failed, because Payment is expired",
                     paymentId);
            return Optional.of(ScaStatus.FAILED);
        }

        return authorisationOptional
                   .filter(auth -> paymentId.equals(auth.getPaymentData().getPaymentId()))
                   .map(PisAuthorization::getScaStatus);
    }

    /**
     * Reads Psu data list by payment Id
     *
     * @param paymentId id of the payment
     * @return response contains data of Psu list
     */
    @Override
    public Optional<List<PsuIdData>> getPsuDataListByPaymentId(String paymentId) {

        return readPisCommonPaymentDataByPaymentId(paymentId)
                   .map(pc -> psuDataMapper.mapToPsuIdDataList(pc.getPsuDataList()));
    }

    @Override
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        Optional<PisAuthorization> authorisationOptional = pisAuthorisationRepository.findByExternalId(authorisationId);

        return authorisationOptional.map(a -> a.getAvailableScaMethods()
                                                  .stream()
                                                  .filter(m -> Objects.equals(m.getAuthenticationMethodId(), authenticationMethodId))
                                                  .anyMatch(ScaMethod::isDecoupled))
                   .orElseGet(() -> {
                       log.info("Authorisation ID: [{}]. Get authorisation method decoupled status failed, because pis authorisation is not found",
                                authorisationId);
                       return false;
                   });
    }

    @Override
    @Transactional
    public boolean saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        Optional<PisAuthorization> authorisationOptional = pisAuthorisationRepository.findByExternalId(authorisationId);

        if (!authorisationOptional.isPresent()) {
            log.info(" Authorisation ID: [{}]. Save authentication methods failed, because authorisation is not found", authorisationId);
            return false;
        }

        PisAuthorization authorisation = authorisationOptional.get();

        authorisation.setAvailableScaMethods(scaMethodMapper.mapToScaMethods(methods));
        pisAuthorisationRepository.save(authorisation);
        return true;
    }

    @Override
    @Transactional
    public boolean updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        Optional<PisAuthorization> authorisationOptional = pisAuthorisationRepository.findByExternalId(authorisationId);

        if (!authorisationOptional.isPresent()) {
            log.info("Authorisation ID: [{}]. Update SCA approach failed, because pis authorisation is not found",
                     authorisationId);
            return false;
        }

        PisAuthorization authorisation = authorisationOptional.get();

        authorisation.setScaApproach(scaApproach);
        pisAuthorisationRepository.save(authorisation);
        return true;
    }

    private PisCommonPaymentData setStatusAndSaveCommonPaymentData(PisCommonPaymentData commonPaymentData, TransactionStatus status) {
        commonPaymentData.setTransactionStatus(status);
        return pisCommonPaymentDataRepository.save(commonPaymentData);
    }

    private Optional<PisCommonPaymentData> readReceivedCommonPaymentDataByPaymentId(String paymentId) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        Optional<PisCommonPaymentData> commonPaymentData = pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatusIn(paymentId, Arrays.asList(RCVD, PATC))
                                                               .filter(CollectionUtils::isNotEmpty)
                                                               .map(list -> list.get(0).getPaymentData())
                                                               .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdatePaymentDataOnConfirmationExpiration)
                                                               .filter(p -> EnumSet.of(RCVD, PATC).contains(p.getTransactionStatus()));

        if (!commonPaymentData.isPresent()) {
            commonPaymentData = pisCommonPaymentDataRepository.findByPaymentIdAndTransactionStatusIn(paymentId, Arrays.asList(RCVD, PATC))
                                    .map(pisCommonPaymentConfirmationExpirationService::checkAndUpdatePaymentDataOnConfirmationExpiration)
                                    .filter(p -> EnumSet.of(RCVD, PATC).contains(p.getTransactionStatus()));
        }

        return commonPaymentData;
    }

    private Optional<PisCommonPaymentData> readPisCommonPaymentDataByPaymentId(String paymentId) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        Optional<PisCommonPaymentData> commonPaymentData = pisPaymentDataRepository.findByPaymentId(paymentId)
                                                               .filter(CollectionUtils::isNotEmpty)
                                                               .map(list -> list.get(0).getPaymentData());
        if (!commonPaymentData.isPresent()) {
            commonPaymentData = pisCommonPaymentDataRepository.findByPaymentId(paymentId);
        }

        return commonPaymentData;
    }

    private void savePaymentData(PisCommonPaymentData pisCommonPayment, PisCommonPaymentRequest request) {
        boolean isCommonPayment = CollectionUtils.isEmpty(request.getPayments()) && request.getPaymentInfo() != null;
        // todo implementation should be changed  https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534

        if (isCommonPayment) {
            pisCommonPaymentDataRepository.save(pisCommonPaymentMapper.mapToPisCommonPaymentData(request.getPaymentInfo()));
        } else {
            pisPaymentDataRepository.save(pisCommonPaymentMapper.mapToPisPaymentDataList(request.getPayments(), pisCommonPayment));
        }
    }

    /**
     * Creates PIS consent authorisation entity and stores it into database
     *
     * @param paymentData PIS payment data, for which authorisation is performed
     * @param request     needed parameters for creating PIS authorisation
     * @return PisAuthorization
     */
    private PisAuthorization saveNewAuthorisation(PisCommonPaymentData paymentData, CreatePisAuthorisationRequest request) {
        PisAuthorization consentAuthorisation = new PisAuthorization();
        Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(psuDataMapper.mapToPsuData(request.getPsuData()), paymentData.getPsuDataList());

        if (psuDataOptional.isPresent()) {
            PsuData psuData = psuDataOptional.get();
            paymentData.setPsuDataList(cmsPsuService.enrichPsuData(psuData, paymentData.getPsuDataList()));
            consentAuthorisation.setPsuData(psuData);
        }

        consentAuthorisation.setExternalId(UUID.randomUUID().toString());
        consentAuthorisation.setScaStatus(STARTED);
        consentAuthorisation.setAuthorizationType(request.getAuthorizationType());
        consentAuthorisation.setRedirectUrlExpirationTimestamp(countRedirectUrlExpirationTimestampForAuthorisationType(request.getAuthorizationType()));
        consentAuthorisation.setScaApproach(request.getScaApproach());
        consentAuthorisation.setPaymentData(paymentData);

        return pisAuthorisationRepository.save(consentAuthorisation);
    }

    private OffsetDateTime countRedirectUrlExpirationTimestampForAuthorisationType(CmsAuthorisationType authorisationType) {
        long redirectUrlExpirationTimeMs;

        if (authorisationType == CmsAuthorisationType.CANCELLED) {
            redirectUrlExpirationTimeMs = aspspProfileService.getAspspSettings().getPaymentCancellationRedirectUrlExpirationTimeMs();
        } else {
            redirectUrlExpirationTimeMs = aspspProfileService.getAspspSettings().getRedirectUrlExpirationTimeMs();
        }

        return OffsetDateTime.now().plus(redirectUrlExpirationTimeMs, ChronoUnit.MILLIS);
    }

    private void closePreviousAuthorisationsByPsu(PisAuthorization authorisation, PsuIdData psuIdData) {
        PisCommonPaymentData paymentData = authorisation.getPaymentData();
        CmsAuthorisationType authorizationType = authorisation.getAuthorizationType();

        closePreviousAuthorisationsByPsu(paymentData.getAuthorizations(), authorizationType, psuIdData);
    }

    private void closePreviousAuthorisationsByPsu(List<PisAuthorization> authorisations, CmsAuthorisationType authorisationType, PsuIdData psuIdData) {
        PsuData psuData = psuDataMapper.mapToPsuData(psuIdData);

        if (Objects.isNull(psuData)
                || psuData.isEmpty()) {
            log.info("Close previous authorisations by psu failed, because psuData is not allowed");
            return;
        }

        List<PisAuthorization> pisAuthorisationList = authorisations
                                                          .stream()
                                                          .filter(auth -> auth.getAuthorizationType() == authorisationType)
                                                          .filter(auth -> Objects.nonNull(auth.getPsuData()) && auth.getPsuData().contentEquals(psuData))
                                                          .map(this::makeAuthorisationFailedAndExpired)
                                                          .collect(Collectors.toList());

        pisAuthorisationRepository.save(pisAuthorisationList);
    }

    private PisAuthorization makeAuthorisationFailedAndExpired(PisAuthorization auth) {
        auth.setScaStatus(ScaStatus.FAILED);
        auth.setRedirectUrlExpirationTimestamp(OffsetDateTime.now());
        return auth;
    }

    private List<String> readAuthorisationsFromPaymentCommonData(PisCommonPaymentData paymentData, CmsAuthorisationType authorisationType) {
        return paymentData.getAuthorizations()
                   .stream()
                   .filter(auth -> auth.getAuthorizationType() == authorisationType)
                   .map(PisAuthorization::getExternalId)
                   .collect(Collectors.toList());
    }

    private ScaStatus doUpdateConsentAuthorisation(UpdatePisCommonPaymentPsuDataRequest request, PisAuthorization pisAuthorisation) {
        if (pisAuthorisation.getScaStatus().isFinalisedStatus()) {
            return pisAuthorisation.getScaStatus();
        }

        PsuData psuDataInAuthorisation = pisAuthorisation.getPsuData();
        PsuData psuDataInRequest = psuDataMapper.mapToPsuData(request.getPsuData());

        if (STARTED == pisAuthorisation.getScaStatus()) {

            if (!cmsPsuService.isPsuDataRequestCorrect(psuDataInRequest, psuDataInAuthorisation)) {
                log.info("Authorisation ID: [{}], SCA status: [{}]. Update consent authorisation failed, because psu data request does not match stored psu data",
                         pisAuthorisation.getExternalId(), pisAuthorisation.getScaStatus().getValue());
                return pisAuthorisation.getScaStatus();
            }

            PisCommonPaymentData paymentData = pisAuthorisation.getPaymentData();
            List<PsuData> psuListInPayment = paymentData.getPsuDataList();
            Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(psuDataInRequest, psuListInPayment);

            if (psuDataOptional.isPresent()) {
                PsuData psuData = psuDataOptional.get();
                paymentData.setPsuDataList(cmsPsuService.enrichPsuData(psuData, psuListInPayment));
                pisAuthorisation.setPsuData(psuData);
            }

        } else {
            boolean isPsuCorrect = Objects.nonNull(psuDataInAuthorisation)
                                       && Objects.nonNull(psuDataInRequest)
                                       && psuDataInAuthorisation.contentEquals(psuDataInRequest);
            if (!isPsuCorrect) {
                log.info("Authorisation ID: [{}], SCA status: [{}]. Update consent authorisation failed, because psu data request does not match stored psu data",
                         pisAuthorisation.getExternalId(), pisAuthorisation.getScaStatus().getValue());
                return pisAuthorisation.getScaStatus();
            }
        }

        if (SCAMETHODSELECTED == request.getScaStatus()) {
            String chosenMethod = request.getAuthenticationMethodId();
            if (StringUtils.isNotBlank(chosenMethod)) {
                pisAuthorisation.setChosenScaMethod(chosenMethod);
            }
        }
        pisAuthorisation.setScaStatus(request.getScaStatus());
        PisAuthorization saved = pisAuthorisationRepository.save(pisAuthorisation);
        return saved.getScaStatus();
    }
}
