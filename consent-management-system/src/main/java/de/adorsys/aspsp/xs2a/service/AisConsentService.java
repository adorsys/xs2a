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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.account.AccountAccessHolder;
import de.adorsys.aspsp.xs2a.consent.api.*;
import de.adorsys.aspsp.xs2a.consent.api.ais.*;
import de.adorsys.aspsp.xs2a.domain.AccountAccess;
import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.aspsp.xs2a.domain.AisConsentAction;
import de.adorsys.aspsp.xs2a.domain.AisConsentAuthorization;
import de.adorsys.aspsp.xs2a.repository.AisConsentActionRepository;
import de.adorsys.aspsp.xs2a.repository.AisConsentAuthorizationRepository;
import de.adorsys.aspsp.xs2a.repository.AisConsentRepository;
import de.adorsys.aspsp.xs2a.service.mapper.AisConsentMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static de.adorsys.aspsp.xs2a.consent.api.CmsConsentStatus.*;
import static de.adorsys.aspsp.xs2a.consent.api.TypeAccess.*;

@Service
@RequiredArgsConstructor
public class AisConsentService {
    private final AisConsentRepository aisConsentRepository;
    private final AisConsentActionRepository aisConsentActionRepository;
    private final AisConsentAuthorizationRepository aisConsentAuthorizationRepository;
    private final AisConsentMapper consentMapper;
    private final AspspProfileService profileService;

    /**
     * Create AIS consent
     *
     * @param request needed parameters for creating AIS consent
     * @return String consent id
     */
    @Transactional
    public Optional<String> createConsent(CreateAisConsentRequest request) {
        AisConsent consent = createConsentFromRequest(request);
        consent.setExternalId(UUID.randomUUID().toString());
        AisConsent saved = aisConsentRepository.save(consent);
        return saved.getId() != null
                   ? Optional.ofNullable(saved.getExternalId())
                   : Optional.empty();
    }

    /**
     * Read status of consent by id
     *
     * @param consentId
     * @return ConsentStatus
     */
    public Optional<CmsConsentStatus> getConsentStatusById(String consentId) {
        return getAisConsentById(consentId)
                   .map(this::checkAndUpdateOnExpiration)
                   .map(AisConsent::getConsentStatus);
    }

    /**
     * Update consent status by id
     *
     * @param consentId
     * @param status    new consent status
     * @return Boolean
     */
    public Optional<Boolean> updateConsentStatusById(String consentId, CmsConsentStatus status) {
        return getActualAisConsent(consentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    /**
     * Read full information of consent by id
     *
     * @param consentId
     * @return AisAccountConsent
     */
    public Optional<AisAccountConsent> getAisAccountConsentById(String consentId) {
        return getAisConsentById(consentId)
                   .map(this::checkAndUpdateOnExpiration)
                   .map(consentMapper::mapToAisAccountConsent);
    }

    /**
     * Save information about uses of consent
     *
     * @param request needed parameters for logging usage AIS consent
     */
    @Transactional
    public void checkConsentAndSaveActionLog(ConsentActionRequest request) {
        AisConsent consent = getAisConsentById(request.getConsentId())
                                 .orElse(null);
        checkAndUpdateOnExpiration(consent);
        updateAisConsentCounter(consent);
        logConsentAction(request.getConsentId(), resolveConsentActionStatus(request, consent), request.getTppId());
    }

    /**
     * Update AIS consent account access by id
     *
     * @param request   needed parameters for updating AIS consent
     * @param consentId id of the consent to be updated
     * @return String consent id
     */
    @Transactional
    public Optional<String> updateAccountAccess(String consentId, AisAccountAccessInfo request) {
        return getActualAisConsent(consentId)
                   .map(consent -> {
                       consent.addAccountAccess(readAccountAccess(request));
                       return aisConsentRepository.save(consent)
                                  .getExternalId();
                   });
    }

    /**
     * Update AIS consent aspsp blob data by id
     *
     * @param request   needed parameters for updating AIS consent
     * @param consentId id of the consent to be updated
     * @return String consent id
     */
    @Transactional
    public Optional<String> updateAspspData(String consentId, UpdateConsentAspspDataRequest request) {
        return getActualAisConsent(consentId)
                   .map(cons -> updateConsentAspspData(request, cons));
    }

    /**
     * Create consent authorization
     *
     * @param consentId
     * @param request   needed parameters for creating consent authorization
     * @return String authorization id
     */
    @Transactional
    public Optional<String> createAuthorization(String consentId, AisConsentAuthorizationRequest request) {
        return aisConsentRepository.findByExternalIdAndConsentStatusIn(consentId, EnumSet.of(RECEIVED, VALID))
                   .map(aisConsent -> saveNewAuthorization(aisConsent, request));
    }

    /**
     * Get consent authorization
     *
     * @param consentId
     * @param authorizationId
     * @return AisConsentAuthorizationResponse
     */
    public Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return aisConsentRepository.findByExternalIdAndConsentStatusIn(consentId, EnumSet.of(RECEIVED, VALID)).isPresent()
                   ? aisConsentAuthorizationRepository.findByExternalId(authorizationId)
                         .map(consentMapper::mapToAisConsentAuthorizationResponse)
                   : Optional.empty();
    }

    /**
     * Update consent authorization
     *
     * @param authorizationId
     * @param consentId
     * @param request         needed parameters for updating consent authorization
     * @return Boolean
     */
    @Transactional
    public Optional<Boolean> updateConsentAuthorization(String authorizationId, String consentId, AisConsentAuthorizationRequest request) {
        return aisConsentRepository.findByExternalIdAndConsentStatusIn(consentId, EnumSet.of(RECEIVED, VALID)).isPresent()
                   ? updateConsentAuthorization(authorizationId, request)
                   : Optional.of(false);
    }

    private Set<AccountAccess> readAccountAccess(AisAccountAccessInfo access) {
        AccountAccessHolder holder = new AccountAccessHolder();
        holder.fillAccess(access.getAccounts(), ACCOUNT);
        holder.fillAccess(access.getBalances(), BALANCE);
        holder.fillAccess(access.getTransactions(), TRANSACTION);
        return holder.getAccountAccesses();
    }

    private String updateConsentAspspData(UpdateConsentAspspDataRequest request, AisConsent consent) {
        consent.setAspspConsentData(request.getAspspConsentData());
        AisConsent savedConsent = aisConsentRepository.save(consent);
        return savedConsent.getExternalId();
    }

    private AisConsent createConsentFromRequest(CreateAisConsentRequest request) {
        int minFrequencyPerDay = profileService.getMinFrequencyPerDay(request.getFrequencyPerDay());
        AisConsent consent = new AisConsent();
        consent.setConsentStatus(RECEIVED);
        consent.setExpectedFrequencyPerDay(minFrequencyPerDay);
        consent.setTppFrequencyPerDay(request.getFrequencyPerDay());
        consent.setUsageCounter(minFrequencyPerDay);
        consent.setRequestDateTime(LocalDateTime.now());
        consent.setExpireDate(request.getValidUntil());
        consent.setPsuId(request.getPsuId());
        consent.setTppId(request.getTppId());
        consent.addAccountAccess(readAccountAccess(request.getAccess()));
        consent.setRecurringIndicator(request.isRecurringIndicator());
        consent.setTppRedirectPreferred(request.isTppRedirectPreferred());
        consent.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
        consent.setAspspConsentData(request.getAspspConsentData());
        consent.setAisConsentRequestType(getRequestTypeFromAccess(request.getAccess()));

        return consent;
    }

    private AisConsentRequestType getRequestTypeFromAccess(AisAccountAccessInfo accessInfo) {
        if (accessInfo.getAllPsd2() == AccountAccessType.ALL_ACCOUNTS) {
            return AisConsentRequestType.GLOBAL;
        } else if (EnumSet.of(AccountAccessType.ALL_ACCOUNTS, AccountAccessType.ALL_ACCOUNTS_WITH_BALANCES).contains(accessInfo.getAvailableAccounts())) {
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

    private ActionStatus resolveConsentActionStatus(ConsentActionRequest request, AisConsent consent) {
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
        return Optional.ofNullable(consentId)
                   .flatMap(c -> aisConsentRepository.findByExternalIdAndConsentStatusIn(consentId, EnumSet.of(RECEIVED, VALID)));
    }

    private Optional<AisConsent> getAisConsentById(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(aisConsentRepository::findByExternalId);
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

    private AisConsent setStatusAndSaveConsent(AisConsent consent, CmsConsentStatus status) {
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);
        return aisConsentRepository.save(consent);
    }

    private String saveNewAuthorization(AisConsent aisConsent, AisConsentAuthorizationRequest request) {
        AisConsentAuthorization consentAuthorization = new AisConsentAuthorization();
        consentAuthorization.setExternalId(UUID.randomUUID().toString());
        consentAuthorization.setPsuId(consentAuthorization.getPsuId());
        consentAuthorization.setConsent(aisConsent);
        consentAuthorization.setScaStatus(request.getScaStatus());
        return aisConsentAuthorizationRepository.save(consentAuthorization).getExternalId();
    }

    private Optional<Boolean> updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request) {
        return aisConsentAuthorizationRepository.findByExternalId(authorizationId)
                   .map(conAuth -> {
                       conAuth.setScaStatus(request.getScaStatus());
                       conAuth.setPassword(request.getPassword());
                       conAuth.setAuthenticationMethodId(request.getAuthenticationMethodId());
                       conAuth.setScaAuthenticationData(request.getScaAuthenticationData());
                       return aisConsentAuthorizationRepository.save(conAuth).getExternalId() != null;
                   });
    }
}
