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
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.*;
import de.adorsys.psd2.consent.repository.AisConsentActionRepository;
import de.adorsys.psd2.consent.repository.AisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// TODO temporary solution to switch off Hibernate dirty check. Need to understand why objects are changed here. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
public class AisConsentServiceInternal implements AisConsentService {
    private final AisConsentRepository aisConsentRepository;
    private final AisConsentActionRepository aisConsentActionRepository;
    private final AisConsentAuthorizationRepository aisConsentAuthorizationRepository;
    private final AisConsentMapper consentMapper;
    private final PsuDataMapper psuDataMapper;
    private final AspspProfileService aspspProfileService;
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final TppInfoMapper tppInfoMapper;

    /**
     * Create AIS consent
     *
     * @param request needed parameters for creating AIS consent
     * @return String consent id
     */
    @Override
    @Transactional
    public Optional<String> createConsent(CreateAisConsentRequest request) {
        if (request.getAllowedFrequencyPerDay() == null) {
            return Optional.empty();
        }
        AisConsent consent = createConsentFromRequest(request);
        consent.setExternalId(UUID.randomUUID().toString());
        AisConsent saved = aisConsentRepository.save(consent);
        return saved.getId() != null
                   ? Optional.of(saved.getExternalId())
                   : Optional.empty();
    }

    /**
     * Read status of consent by id
     *
     * @param consentId id of consent
     * @return ConsentStatus
     */
    @Override
    @Transactional
    public Optional<ConsentStatus> getConsentStatusById(String consentId) {
        return aisConsentRepository.findByExternalId(consentId)
                   .map(aisConsentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                   .map(this::checkAndUpdateOnExpiration)
                   .map(AisConsent::getConsentStatus);
    }

    /**
     * Update consent status by id
     *
     * @param consentId id of consent
     * @param status    new consent status
     * @return Boolean
     */
    @Override
    @Transactional
    public boolean updateConsentStatusById(String consentId, ConsentStatus status) {
        return getActualAisConsent(consentId)
                   .map(c -> setStatusAndSaveConsent(c, status))
                   .orElse(false);
    }

    /**
     * Read full information of consent by id
     *
     * @param consentId id of consent
     * @return AisAccountConsent
     */
    @Override
    @Transactional
    public Optional<AisAccountConsent> getAisAccountConsentById(String consentId) {
        return aisConsentRepository.findByExternalId(consentId)
                   .map(aisConsentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                   .map(this::checkAndUpdateOnExpiration)
                   .map(consentMapper::mapToAisAccountConsent);
    }

    /**
     * Read full initial information of consent by id
     *
     * @param consentId id of consent
     * @return AisAccountConsent
     */
    @Override
    public Optional<AisAccountConsent> getInitialAisAccountConsentById(String consentId) {
        return aisConsentRepository.findByExternalId(consentId)
                   .map(this::checkAndUpdateOnExpiration)
                   .map(consentMapper::mapToInitialAisAccountConsent);
    }

    @Override
    @Transactional
    public boolean findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        AisConsent newConsent = aisConsentRepository.findByExternalId(newConsentId)
                                    .orElseThrow(() -> new IllegalArgumentException("Wrong consent id: " + newConsentId));

        if (newConsent.isOneAccessType()) {
            return false;
        }

        PsuData psuData = newConsent.getPsuData();
        TppInfoEntity tppInfo = newConsent.getTppInfo();

        if (psuData == null || tppInfo == null) {
            throw new IllegalArgumentException("Wrong consent data");
        }

        List<AisConsent> oldConsents = aisConsentRepository.findOldConsentsByNewConsentParams(psuData.getPsuId(), tppInfo.getAuthorisationNumber(), tppInfo.getAuthorityId(),
                                                                                              newConsent.getInstanceId(), newConsent.getExternalId(), EnumSet.of(RECEIVED, VALID));

        if (oldConsents.isEmpty()) {
            return false;
        }

        oldConsents.forEach(c -> c.setConsentStatus(TERMINATED_BY_TPP));
        aisConsentRepository.save(oldConsents);
        return true;
    }

    /**
     * Save information about uses of consent
     *
     * @param request needed parameters for logging usage AIS consent
     */
    @Override
    @Transactional
    public void checkConsentAndSaveActionLog(AisConsentActionRequest request) {
        Optional<AisConsent> consentOpt = getActualAisConsent(request.getConsentId());
        if (consentOpt.isPresent()) {
            AisConsent consent = consentOpt.get();
            aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent);
            checkAndUpdateOnExpiration(consent);
            updateAisConsentCounter(consent);
            logConsentAction(consent.getExternalId(), resolveConsentActionStatus(request, consent), request.getTppId());
        }
    }

    /**
     * Update AIS consent account access by id
     *
     * @param request   needed parameters for updating AIS consent
     * @param consentId id of the consent to be updated
     * @return String   consent id
     */
    @Override
    @Transactional
    public Optional<String> updateAspspAccountAccess(String consentId, AisAccountAccessInfo request) {
        return getActualAisConsent(consentId)
                   .map(consent -> {
                       consent.addAspspAccountAccess(new AspspAccountAccessHolder(request)
                                                         .getAccountAccesses());
                       return aisConsentRepository.save(consent)
                                  .getExternalId();
                   });
    }

    /**
     * Create consent authorization
     *
     * @param consentId id of consent
     * @param request   needed parameters for creating consent authorization
     * @return String authorization id
     */
    @Override
    @Transactional
    public Optional<String> createAuthorization(String consentId, AisConsentAuthorizationRequest request) {
        return aisConsentRepository.findByExternalId(consentId)
                   .filter(con -> !con.getConsentStatus().isFinalisedStatus())
                   .map(aisConsent -> {
                       closePreviousAuthorisationsByPsu(aisConsent.getAuthorizations(), request.getPsuData());
                       return saveNewAuthorization(aisConsent, request);
                   });
    }

    /**
     * Get consent authorization
     *
     * @param consentId       id of consent
     * @param authorizationId id of authorisation session
     * @return AisConsentAuthorizationResponse
     */
    @Override
    public Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        boolean consentPresent = aisConsentRepository.findByExternalId(consentId)
                                     .filter(c -> !c.getConsentStatus().isFinalisedStatus())
                                     .isPresent();

        return consentPresent
                   ? aisConsentAuthorizationRepository.findByExternalId(authorizationId)
                         .map(consentMapper::mapToAisConsentAuthorizationResponse)
                   : Optional.empty();
    }

    /**
     * Gets list of consent authorisation IDs by consent ID
     *
     * @param consentId id of consent
     * @return Gets list of consent authorisation IDs
     */
    @Override
    public Optional<List<String>> getAuthorisationsByConsentId(String consentId) {
        return aisConsentRepository.findByExternalId(consentId)
                   .map(cst -> cst.getAuthorizations().stream()
                                   .map(AisConsentAuthorization::getExternalId)
                                   .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        Optional<AisConsent> consentOptional = aisConsentRepository.findByExternalId(consentId);
        if (!consentOptional.isPresent()) {
            return Optional.empty();
        }

        AisConsent consent = consentOptional.get();
        if (aisConsentConfirmationExpirationService.isConsentConfirmationExpired(consent)) {
            aisConsentConfirmationExpirationService.updateConsentOnConfirmationExpiration(consent);
            return Optional.of(ScaStatus.FAILED);
        }

        Optional<AisConsentAuthorization> authorisation = findAuthorisationInConsent(authorisationId, consent);
        return authorisation.map(AisConsentAuthorization::getScaStatus);
    }

    /**
     * Update consent authorization
     *
     * @param authorizationId id of authorisation session
     * @param request         needed parameters for updating consent authorization
     * @return boolean
     */
    @Override
    @Transactional
    public boolean updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request) {
        Optional<AisConsentAuthorization> aisConsentAuthorizationOptional = aisConsentAuthorizationRepository.findByExternalId(authorizationId);

        if (!aisConsentAuthorizationOptional.isPresent()) {
            return false;
        }

        AisConsentAuthorization aisConsentAuthorization = aisConsentAuthorizationOptional.get();

        if (aisConsentAuthorization.getScaStatus().isFinalisedStatus()) {
            return false;
        }

        if (ScaStatus.STARTED == aisConsentAuthorization.getScaStatus()) {
            aisConsentAuthorization.setPsuData(psuDataMapper.mapToPsuData(request.getPsuData()));
        }

        if (ScaStatus.SCAMETHODSELECTED == request.getScaStatus()) {
            // TODO refactor logic and don't save tan and password data in plain text https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/390
            aisConsentAuthorization.setAuthenticationMethodId(request.getAuthenticationMethodId());
        }

        aisConsentAuthorization.setScaStatus(request.getScaStatus());
        aisConsentAuthorization = aisConsentAuthorizationRepository.save(aisConsentAuthorization);

        return aisConsentAuthorization.getExternalId() != null;
    }

    @Override
    public Optional<PsuIdData> getPsuDataByConsentId(String consentId) {
        return getActualAisConsent(consentId)
                   .map(ac -> psuDataMapper.mapToPsuIdData(ac.getPsuData()));
    }

    private AisConsent createConsentFromRequest(CreateAisConsentRequest request) {

        AisConsent consent = new AisConsent();
        consent.setConsentStatus(RECEIVED);
        consent.setAllowedFrequencyPerDay(request.getAllowedFrequencyPerDay());
        consent.setTppFrequencyPerDay(request.getRequestedFrequencyPerDay());
        consent.setUsageCounter(request.getAllowedFrequencyPerDay()); // Initially we set maximum and then decrement it by usage
        consent.setRequestDateTime(LocalDateTime.now());
        consent.setExpireDate(request.getValidUntil());
        consent.setPsuData(psuDataMapper.mapToPsuData(request.getPsuData()));
        consent.setTppInfo(tppInfoMapper.mapToTppInfoEntity(request.getTppInfo()));
        consent.addAccountAccess(new TppAccountAccessHolder(request.getAccess())
                                     .getAccountAccesses());
        consent.setRecurringIndicator(request.isRecurringIndicator());
        consent.setTppRedirectPreferred(request.isTppRedirectPreferred());
        consent.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
        consent.setAisConsentRequestType(getRequestTypeFromAccess(request.getAccess()));

        return consent;
    }

    private AisConsentRequestType getRequestTypeFromAccess(AisAccountAccessInfo accessInfo) {
        if (accessInfo.getAllPsd2() == AisAccountAccessType.ALL_ACCOUNTS) {
            return AisConsentRequestType.GLOBAL;
        } else if (EnumSet.of(AisAccountAccessType.ALL_ACCOUNTS, AisAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES).contains(accessInfo.getAvailableAccounts())) {
            return AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        } else if (isEmptyAccess(accessInfo)) {
            return AisConsentRequestType.BANK_OFFERED;
        }
        return AisConsentRequestType.DEDICATED_ACCOUNTS;
    }

    private boolean isEmptyAccess(AisAccountAccessInfo accessInfo) {
        return CollectionUtils.isEmpty(accessInfo.getAccounts())
                   && CollectionUtils.isEmpty(accessInfo.getBalances())
                   && CollectionUtils.isEmpty(accessInfo.getTransactions());
    }

    private ActionStatus resolveConsentActionStatus(AisConsentActionRequest request, AisConsent consent) {
        return consent == null
                   ? ActionStatus.BAD_PAYLOAD
                   : request.getActionStatus();
    }

    private void updateAisConsentCounter(AisConsent consent) {
        if (consent != null && consent.hasUsagesAvailable()) {
            int usageCounter = consent.getUsageCounter();
            int newUsageCounter = --usageCounter;
            consent.setUsageCounter(newUsageCounter);
            consent.setLastActionDate(LocalDate.now());
            aisConsentRepository.save(consent);
        }
    }

    private void logConsentAction(String requestedConsentId, ActionStatus actionStatus, String tppId) {
        AisConsentAction action = new AisConsentAction();
        action.setActionStatus(actionStatus);
        action.setRequestedConsentId(requestedConsentId);
        action.setTppId(tppId);
        action.setRequestDate(LocalDate.now());
        aisConsentActionRepository.save(action);
    }

    private Optional<AisConsent> getActualAisConsent(String consentId) {
        return aisConsentRepository.findByExternalId(consentId)
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private AisConsent checkAndUpdateOnExpiration(AisConsent consent) {
        if (consent != null && consent.isExpiredByDate() && consent.isStatusNotExpired()) {
            consent.setConsentStatus(EXPIRED);
            consent.setExpireDate(LocalDate.now());
            consent.setLastActionDate(LocalDate.now());
            aisConsentRepository.save(consent);
        }
        return consent;
    }

    private boolean setStatusAndSaveConsent(AisConsent consent, ConsentStatus status) {
        if (consent.getConsentStatus().isFinalisedStatus()) {
            return false;
        }
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);
        return Optional.ofNullable(aisConsentRepository.save(consent))
                   .isPresent();
    }

    private String saveNewAuthorization(AisConsent aisConsent, AisConsentAuthorizationRequest request) {
        AisConsentAuthorization consentAuthorization = new AisConsentAuthorization();
        consentAuthorization.setExternalId(UUID.randomUUID().toString());
        consentAuthorization.setPsuData(psuDataMapper.mapToPsuData(request.getPsuData()));
        consentAuthorization.setConsent(aisConsent);
        consentAuthorization.setScaStatus(request.getScaStatus());
        consentAuthorization.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().plus(aspspProfileService.getAspspSettings().getRedirectUrlExpirationTimeMs(), ChronoUnit.MILLIS));
        return aisConsentAuthorizationRepository.save(consentAuthorization).getExternalId();
    }

    private Optional<AisConsentAuthorization> findAuthorisationInConsent(String authorisationId, AisConsent consent) {
        return consent.getAuthorizations()
                   .stream()
                   .filter(auth -> auth.getExternalId().equals(authorisationId))
                   .findFirst();
    }

    private void closePreviousAuthorisationsByPsu(List<AisConsentAuthorization> authorisations, PsuIdData psuIdData) {
        PsuData psuData = psuDataMapper.mapToPsuData(psuIdData);

        if (!isPsuDataCorrect(psuData)) {
            return;
        }

        List<AisConsentAuthorization> aisConsentAuthorisations = authorisations
                                                                     .stream()
                                                                     .filter(auth -> Objects.nonNull(auth.getPsuData()) && auth.getPsuData().contentEquals(psuData))
                                                                     .map(this::makeAuthorisationFailedAndExpired)
                                                                     .collect(Collectors.toList());

        aisConsentAuthorizationRepository.save(aisConsentAuthorisations);
    }

    private boolean isPsuDataCorrect(PsuData psuData) {
        return Objects.nonNull(psuData)
                   && StringUtils.isNotBlank(psuData.getPsuId());
    }

    private AisConsentAuthorization makeAuthorisationFailedAndExpired(AisConsentAuthorization auth) {
        auth.setScaStatus(ScaStatus.FAILED);
        auth.setRedirectUrlExpirationTimestamp(OffsetDateTime.now());
        return auth;
    }
}
