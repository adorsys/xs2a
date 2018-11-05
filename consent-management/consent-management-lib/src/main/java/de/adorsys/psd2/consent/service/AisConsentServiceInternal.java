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

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.AspspConsentData;
import de.adorsys.psd2.consent.domain.account.*;
import de.adorsys.psd2.consent.repository.AisConsentActionRepository;
import de.adorsys.psd2.consent.repository.AisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.AspspConsentDataRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.security.DecryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static de.adorsys.psd2.consent.api.TypeAccess.*;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // TODO temporary solution to switch off Hibernate dirty check. Need to understand why objects are changed here. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
public class AisConsentServiceInternal implements AisConsentService {
    private final AisConsentRepository aisConsentRepository;
    private final AisConsentActionRepository aisConsentActionRepository;
    private final AisConsentAuthorizationRepository aisConsentAuthorizationRepository;
    private final AisConsentMapper consentMapper;
    private final PsuDataMapper psuDataMapper;
    private final FrequencyPerDateCalculationService frequencyPerDateCalculationService;
    private final SecurityDataService securityDataService;
    private final AspspConsentDataRepository aspspConsentDataRepository;

    /**
     * Create AIS consent
     *
     * @param request needed parameters for creating AIS consent
     * @return String consent id
     */
    @Override
    @Transactional
    public Optional<String> createConsent(CreateAisConsentRequest request) {
        AisConsent consent = createConsentFromRequest(request);
        consent.setExternalId(UUID.randomUUID().toString());
        AisConsent saved = aisConsentRepository.save(consent);
        return saved.getId() != null
                   ? securityDataService.encryptId(saved.getExternalId())
                   : Optional.empty();
    }

    /**
     * Read status of consent by id
     *
     * @param consentId id of consent
     * @return ConsentStatus
     */
    @Override
    public Optional<ConsentStatus> getConsentStatusById(String consentId) {
        return getAisConsentById(consentId)
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
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status)
                   .orElse(false);
    }

    /**
     * Read full information of consent by id
     *
     * @param consentId id of consent
     * @return AisAccountConsent
     */
    @Override
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
    @Override
    @Transactional
    public void checkConsentAndSaveActionLog(AisConsentActionRequest request) {
        Optional<AisConsent> consentOpt = getActualAisConsent(request.getConsentId());
        if (consentOpt.isPresent()) {
            AisConsent consent = consentOpt.get();
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
    public Optional<String> updateAccountAccess(String consentId, AisAccountAccessInfo request) {
        return getActualAisConsent(consentId)
                   .map(consent -> {
                       consent.addAccountAccess(readAccountAccess(request));
                       return aisConsentRepository.save(consent)
                                  .getExternalId();
                   });
    }

    /**
     * Get Ais aspsp consent data by id
     *
     * @param encryptedConsentId id of the consent
     * @return Response containing aspsp consent data
     */
    @Override
    public Optional<CmsAspspConsentDataBase64> getAspspConsentData(String encryptedConsentId) {
        return getActualAisConsent(encryptedConsentId)
                   .map(aisConsent -> getConsentAspspData(aisConsent, encryptedConsentId));
    }

    /**
     * Update AIS consent aspsp consent data by id
     *
     * @param request            Aspsp provided ais consent data
     * @param encryptedConsentId id of the consent to be updated
     * @return String   consent id
     */
    @Override
    @Transactional
    public Optional<String> saveAspspConsentDataInAisConsent(String encryptedConsentId, CmsAspspConsentDataBase64 request) {
        return getActualAisConsent(encryptedConsentId)
                   .flatMap(cons -> saveAspspConsentDataInAisConsent(request, cons, encryptedConsentId));
    }

    /**
     * Create consent authorization
     *
     * @param encryptedConsentId id of consent
     * @param request            needed parameters for creating consent authorization
     * @return String authorization id
     */
    @Override
    @Transactional
    public Optional<String> createAuthorization(String encryptedConsentId, AisConsentAuthorizationRequest request) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(consentId -> aisConsentRepository.findByExternalIdAndConsentStatusIn(consentId, EnumSet.of(RECEIVED, VALID)))
                   .map(aisConsent -> saveNewAuthorization(aisConsent, request));
    }

    /**
     * Get consent authorization
     *
     * @param encryptedConsentId id of consent
     * @param authorizationId    id of authorisation session
     * @return AisConsentAuthorizationResponse
     */
    @Override
    public Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String encryptedConsentId) {
        Optional<String> consentId = securityDataService.decryptId(encryptedConsentId);
        if (!consentId.isPresent()) {
            return Optional.empty();
        }
        return aisConsentRepository.findByExternalIdAndConsentStatusIn(consentId.get(), EnumSet.of(RECEIVED, VALID)).isPresent()
                   ? aisConsentAuthorizationRepository.findByExternalId(authorizationId)
                         .map(consentMapper::mapToAisConsentAuthorizationResponse)
                   : Optional.empty();
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

    private Set<AccountAccess> readAccountAccess(AisAccountAccessInfo access) {
        AccountAccessHolder holder = new AccountAccessHolder();
        holder.fillAccess(access.getAccounts(), ACCOUNT);
        holder.fillAccess(access.getBalances(), BALANCE);
        holder.fillAccess(access.getTransactions(), TRANSACTION);
        return holder.getAccountAccesses();
    }

    private CmsAspspConsentDataBase64 getConsentAspspData(AisConsent consent, String encryptedConsentId) {
        return aspspConsentDataRepository.findByConsentId(consent.getExternalId())
                   .map(AspspConsentData::getData)
                   .flatMap(dta -> securityDataService.decryptConsentData(encryptedConsentId, dta))
                   .map(DecryptedData::getData)
                   .map(bytes -> Base64.getEncoder().encodeToString(bytes))
                   .map(str64 -> new CmsAspspConsentDataBase64(encryptedConsentId, str64))
                   .orElseGet(() -> new CmsAspspConsentDataBase64(encryptedConsentId, null));
    }

    private Optional<String> saveAspspConsentDataInAisConsent(CmsAspspConsentDataBase64 request, AisConsent consent, String encryptedConsentId) {
        return securityDataService.encryptConsentData(encryptedConsentId, request.getAspspConsentDataBase64())
                   .map(encr -> updateAndSaveAspspConsentData(consent, encr.getData()))
                   .map(a -> encryptedConsentId);
    }

    private AspspConsentData updateAndSaveAspspConsentData(AisConsent consent, byte[] consentData) {
        String externalId = consent.getExternalId();
        AspspConsentData aspspConsentData = aspspConsentDataRepository
                                                .findByConsentId(externalId)
                                                .orElseGet(() -> new AspspConsentData(externalId));
        aspspConsentData.setData(consentData);
        return aspspConsentDataRepository.save(aspspConsentData);
    }

    private AisConsent createConsentFromRequest(CreateAisConsentRequest request) {
        int minFrequencyPerDay = frequencyPerDateCalculationService.getMinFrequencyPerDay(request.getFrequencyPerDay());
        AisConsent consent = new AisConsent();
        consent.setConsentStatus(RECEIVED);
        consent.setExpectedFrequencyPerDay(minFrequencyPerDay);
        consent.setTppFrequencyPerDay(request.getFrequencyPerDay());
        consent.setUsageCounter(minFrequencyPerDay);
        consent.setRequestDateTime(LocalDateTime.now());
        consent.setExpireDate(request.getValidUntil());
        consent.setPsuData(psuDataMapper.mapToPsuData(request.getPsuData()));
        consent.setTppId(request.getTppId());
        consent.addAccountAccess(readAccountAccess(request.getAccess()));
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

    private Optional<AisConsent> getActualAisConsent(String encryptedConsentId) {
        Optional<String> consentIdDecrypted = securityDataService.decryptId(encryptedConsentId);
        return consentIdDecrypted
                   .flatMap(c -> aisConsentRepository.findByExternalIdAndConsentStatusIn(c, EnumSet.of(RECEIVED, VALID)));
    }

    private Optional<AisConsent> getAisConsentById(String encryptedConsentId) {
        Optional<String> consentIdDecrypted = securityDataService.decryptId(encryptedConsentId);
        return consentIdDecrypted
                   .flatMap(aisConsentRepository::findByExternalId);
    }

    private AisConsent checkAndUpdateOnExpiration(AisConsent consent) {
        if (consent != null && consent.isExpiredByDate() && consent.isStatusNotExpired()) {
            consent.setConsentStatus(ConsentStatus.EXPIRED);
            consent.setExpireDate(LocalDate.now());
            consent.setLastActionDate(LocalDate.now());
            aisConsentRepository.save(consent);
        }
        return consent;
    }

    private AisConsent setStatusAndSaveConsent(AisConsent consent, ConsentStatus status) {
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);
        return aisConsentRepository.save(consent);
    }

    private String saveNewAuthorization(AisConsent aisConsent, AisConsentAuthorizationRequest request) {
        AisConsentAuthorization consentAuthorization = new AisConsentAuthorization();
        consentAuthorization.setExternalId(UUID.randomUUID().toString());
        consentAuthorization.setPsuData(psuDataMapper.mapToPsuData(request.getPsuData()));
        consentAuthorization.setConsent(aisConsent);
        consentAuthorization.setScaStatus(request.getScaStatus());
        return aisConsentAuthorizationRepository.save(consentAuthorization).getExternalId();
    }
}
