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
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAction;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccessHolder;
import de.adorsys.psd2.consent.domain.account.TppAccountAccessHolder;
import de.adorsys.psd2.consent.repository.AisConsentActionRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// TODO temporary solution to switch off Hibernate dirty check. Need to understand why objects are changed here. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
public class AisConsentServiceInternal implements AisConsentService {
    private final AisConsentRepository aisConsentRepository;
    private final AisConsentActionRepository aisConsentActionRepository;
    private final AisConsentMapper consentMapper;
    private final PsuDataMapper psuDataMapper;
    private final AspspProfileService aspspProfileService;
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final TppInfoMapper tppInfoMapper;
    private final CmsPsuService cmsPsuService;
    private final AisConsentUsageService aisConsentUsageService;

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
            log.info("TPP ID: [{}]. Consent cannot be created, because request contains no allowed frequency per day",
                     request.getTppInfo().getAuthorisationNumber());
            return Optional.empty();
        }
        AisConsent consent = createConsentFromRequest(request);
        consent.setExternalId(UUID.randomUUID().toString());
        AisConsent saved = aisConsentRepository.save(consent);

        if (saved.getId() != null) {
            return Optional.of(saved.getExternalId());
        } else {
            log.info("TPP ID: [{}], External Consent ID: [{}]. Ais consent cannot be created, because when saving to DB got null ID",
                     request.getTppInfo().getAuthorisationNumber(), consent.getExternalId());
            return Optional.empty();
        }
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
        Optional<AisConsent> optionalConsentStatus = aisConsentRepository.findByExternalId(consentId);
        if (optionalConsentStatus.isPresent()) {
            return optionalConsentStatus.map(aisConsentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                       .map(this::checkAndUpdateOnExpiration)
                       .map(AisConsent::getConsentStatus);
        } else {
            log.info("Consent ID: [{}]. Get consent failed, because consent is not found", consentId);
            return Optional.empty();
        }
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
                   .orElseGet(() -> {
                       log.info("Consent ID [{}]. Update consent status by id failed, because consent not found",
                                consentId);
                       return false;
                   });
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
    @Transactional
    public Optional<AisAccountConsent> getInitialAisAccountConsentById(String consentId) {
        return aisConsentRepository.findByExternalId(consentId)
                   .map(this::checkAndUpdateOnExpiration)
                   .map(consentMapper::mapToInitialAisAccountConsent);
    }

    @Override
    @Transactional
    public boolean findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        AisConsent newConsent = aisConsentRepository.findByExternalId(newConsentId)
                                    .orElseThrow(() -> {
                                        log.info("Consent ID: [{}]. Cannot find consent by id", newConsentId);
                                        return new IllegalArgumentException("Wrong consent id: " + newConsentId);
                                    });

        if (newConsent.isOneAccessType()) {
            log.info("Consent ID: [{}]. Cannot find old consents, because consent is OneAccessType", newConsentId);
            return false;
        }

        if (newConsent.isWrongConsentData()) {
            log.info("Consent ID: [{}]. Find old consents failed, because consent psu data list is empty or tppInfo is null", newConsentId);
            throw new IllegalArgumentException("Wrong consent data");
        }

        List<PsuData> psuDataList = newConsent.getPsuDataList();
        Set<String> psuIds = psuDataList.stream()
                                 .filter(Objects::nonNull)
                                 .map(PsuData::getPsuId)
                                 .collect(Collectors.toSet());
        TppInfoEntity tppInfo = newConsent.getTppInfo();

        List<AisConsent> oldConsents = aisConsentRepository.findOldConsentsByNewConsentParams(psuIds,
                                                                                              tppInfo.getAuthorisationNumber(),
                                                                                              tppInfo.getAuthorityId(),
                                                                                              newConsent.getInstanceId(),
                                                                                              newConsent.getExternalId(),
                                                                                              EnumSet.of(RECEIVED, PARTIALLY_AUTHORISED, VALID));

        List<AisConsent> oldConsentsWithExactPsuDataLists = oldConsents.stream()
                                                                .filter(c -> cmsPsuService.isPsuDataListEqual(c.getPsuDataList(), psuDataList))
                                                                .collect(Collectors.toList());

        if (oldConsentsWithExactPsuDataLists.isEmpty()) {
            log.info("Consent ID: [{}]. Cannot find old consents, because consent hasn't exact psu data lists as old consents", newConsentId);
            return false;
        }

        oldConsentsWithExactPsuDataLists.forEach(c -> c.setConsentStatus(TERMINATED_BY_TPP));
        aisConsentRepository.save(oldConsentsWithExactPsuDataLists);
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
            // In this method sonar claims that NPE is possible:
            // https://rules.sonarsource.com/java/RSPEC-3655
            // but we have isPresent in the code before.
            updateAisConsentUsage(consent, request.getRequestUri()); //NOSONAR
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

    @Override
    @Transactional
    public Optional<AisAccountConsent> updateAspspAccountAccessWithResponse(String consentId, AisAccountAccessInfo request) {
        return getActualAisConsent(consentId)
                   .map(consent -> {
                       consent.addAspspAccountAccess(new AspspAccountAccessHolder(request)
                                                         .getAccountAccesses());
                       return consentMapper.mapToAisAccountConsent(aisConsentRepository.save(consent));
                   });
    }

    @Override
    public Optional<List<PsuIdData>> getPsuDataByConsentId(String consentId) {
        return getActualAisConsent(consentId)
                   .map(ac -> psuDataMapper.mapToPsuIdDataList(ac.getPsuDataList()));
    }

    @Override
    @Transactional
    public boolean updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) {
        Optional<AisConsent> aisConsentOptional = aisConsentRepository.findByExternalId(consentId);
        if (!aisConsentOptional.isPresent()) {
            log.info("Consent ID: [{}]. Get update multilevel SCA required status failed, because consent authorisation is not found",
                     consentId);
            return false;
        }
        AisConsent consent = aisConsentOptional.get();
        consent.setMultilevelScaRequired(multilevelScaRequired);
        aisConsentRepository.save(consent);

        return true;
    }

    private AisConsent createConsentFromRequest(CreateAisConsentRequest request) {

        AisConsent consent = new AisConsent();
        consent.setConsentStatus(RECEIVED);
        consent.setAllowedFrequencyPerDay(request.getAllowedFrequencyPerDay());
        consent.setTppFrequencyPerDay(request.getRequestedFrequencyPerDay());
        consent.setRequestDateTime(LocalDateTime.now());
        consent.setExpireDate(adjustExpireDate(request.getValidUntil()));
        consent.setPsuDataList(psuDataMapper.mapToPsuDataList(Collections.singletonList(request.getPsuData())));
        consent.setTppInfo(tppInfoMapper.mapToTppInfoEntity(request.getTppInfo()));
        consent.addAccountAccess(new TppAccountAccessHolder(request.getAccess())
                                     .getAccountAccesses());
        consent.setRecurringIndicator(request.isRecurringIndicator());
        consent.setTppRedirectPreferred(request.isTppRedirectPreferred());
        consent.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
        consent.setAisConsentRequestType(getRequestTypeFromAccess(request.getAccess()));
        consent.setAvailableAccounts(request.getAccess().getAvailableAccounts());
        consent.setAllPsd2(request.getAccess().getAllPsd2());
        return consent;
    }

    private LocalDate adjustExpireDate(LocalDate validUntil) {
        int lifetime = aspspProfileService.getAspspSettings().getConsentLifetime();
        if (lifetime <= 0) {
            return validUntil;
        }

        //Expire date is inclusive and TPP can access AIS consent from current date
        LocalDate lifeTimeDate = LocalDate.now().plusDays(lifetime - 1L);
        return lifeTimeDate.isBefore(validUntil) ? lifeTimeDate : validUntil;
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

    private ActionStatus resolveConsentActionStatus(AisConsentActionRequest request, AisConsent consent) {

        if (consent == null) {
            log.info("Consent ID: [{}]. Consent action status resolver received null consent",
                     request.getConsentId());
            return ActionStatus.BAD_PAYLOAD;
        }
        return request.getActionStatus();
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
            log.info("Consent ID: [{}], Consent status [{}]. Update consent status by ID failed, because consent status is finalised",
                     consent.getExternalId(), consent.getConsentStatus());
            return false;
        }
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);
        return Optional.ofNullable(aisConsentRepository.save(consent))
                   .isPresent();
    }

    private void updateAisConsentUsage(AisConsent consent, String requestUri) {
        aisConsentUsageService.incrementUsage(consent, requestUri);
        consent.setLastActionDate(LocalDate.now());
        aisConsentRepository.save(consent);
    }
}
