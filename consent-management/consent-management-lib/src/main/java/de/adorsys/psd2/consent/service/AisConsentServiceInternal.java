/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.account.AisConsentAction;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentActionRepository;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.service.account.AccountAccessUpdater;
import de.adorsys.psd2.consent.service.mapper.AccessMapper;
import de.adorsys.psd2.consent.service.mapper.CmsConsentMapper;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.psd2.consent.api.CmsError.LOGICAL_ERROR;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.EXPIRED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AisConsentServiceInternal implements AisConsentService {
    private final AisConsentVerifyingRepository aisConsentRepository;
    private final AisConsentActionRepository aisConsentActionRepository;
    private final AuthorisationRepository authorisationRepository;
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final AisConsentUsageService aisConsentUsageService;
    private final OneOffConsentExpirationService oneOffConsentExpirationService;
    private final CmsConsentMapper cmsConsentMapper;
    private final AccessMapper accessMapper;
    private final AccountAccessUpdater accountAccessUpdater;

    /**
     * Saves information about consent usage and consent's sub-resources usage.
     *
     * @param request {@link AisConsentActionRequest} needed parameters for logging usage AIS consent
     */
    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest request) throws WrongChecksumException {
        Optional<ConsentEntity> consentOpt = aisConsentRepository.getActualAisConsent(request.getConsentId());
        if (consentOpt.isPresent()) {
            ConsentEntity consent = consentOpt.get();
            aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent);
            checkAndUpdateOnExpiration(consent);
            updateAisConsentUsage(consent, request);
            logConsentAction(consent.getExternalId(), request.getActionStatus(), request.getTppId());
        }

        return CmsResponse.<CmsResponse.VoidResponse>builder()
                   .payload(CmsResponse.voidResponse())
                   .build();
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsConsent> updateAspspAccountAccess(String consentId, AccountAccess request) throws WrongChecksumException {
        Optional<ConsentEntity> consentOptional = aisConsentRepository.getActualAisConsent(consentId);

        if (consentOptional.isEmpty()) {
            log.info("Consent ID [{}]. Update aspsp account access with response failed, because consent not found",
                     consentId);
            return CmsResponse.<CmsConsent>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        ConsentEntity consentEntity = consentOptional.get();
        AccountAccess requestedAccessWithFilledAccounts = fillAccountsWithAllAccountReferences(request);
        ConsentEntity updatedConsent = updateConsentAccess(consentEntity, requestedAccessWithFilledAccounts);
        ConsentEntity savedConsent = aisConsentRepository.verifyAndUpdate(updatedConsent);
        CmsConsent cmsConsent = mapToCmsConsent(savedConsent);

        return CmsResponse.<CmsConsent>builder()
                   .payload(cmsConsent)
                   .build();
    }

    private AccountAccess fillAccountsWithAllAccountReferences(AccountAccess accountAccess) {
        List<AccountReference> allReferences = Stream.of(accountAccess.getAccounts(),
                                                         accountAccess.getBalances(),
                                                         accountAccess.getTransactions())
                                                   .flatMap(Collection::stream)
                                                   .distinct()
                                                   .collect(Collectors.toList());
        return new AccountAccess(allReferences, accountAccess.getBalances(), accountAccess.getTransactions(),
                                 accountAccess.getAdditionalInformationAccess());
    }

    private ConsentEntity updateConsentAccess(ConsentEntity consentEntity, AccountAccess request) {
        List<AspspAccountAccess> aspspAccountAccesses = consentEntity.getAspspAccountAccesses();
        AccountAccess existingAccess = accessMapper.mapAspspAccessesToAccountAccess(aspspAccountAccesses,
                                                                                    consentEntity.getOwnerNameType(),
                                                                                    consentEntity.getTrustedBeneficiariesType());
        AccountAccess updatedAccesses = accountAccessUpdater.updateAccountReferencesInAccess(existingAccess, request);
        List<AspspAccountAccess> updatedAspspAccountAccesses = accessMapper.mapToAspspAccountAccess(consentEntity, updatedAccesses);
        consentEntity.setAspspAccountAccesses(updatedAspspAccountAccesses);
        return consentEntity;
    }

    private ConsentEntity checkAndUpdateOnExpiration(ConsentEntity consent) {
        if (consent.shouldConsentBeExpired()) {
            return aisConsentConfirmationExpirationService.expireConsent(consent);
        }

        return consent;
    }

    private void updateAisConsentUsage(ConsentEntity consent, AisConsentActionRequest request) throws WrongChecksumException {
        if (!request.isUpdateUsage()) {
            return;
        }
        aisConsentUsageService.incrementUsage(consent, request);

        CmsConsent cmsConsent = mapToCmsConsent(consent);

        if (!consent.isRecurringIndicator() && consent.getFrequencyPerDay() == 1
                && oneOffConsentExpirationService.isConsentExpired(cmsConsent, consent.getId())) {
            consent.setConsentStatus(EXPIRED);
        }

        consent.setLastActionDate(LocalDate.now());

        aisConsentRepository.verifyAndSave(consent);
    }

    private CmsConsent mapToCmsConsent(ConsentEntity consent) {
        List<AuthorisationEntity> authorisations = authorisationRepository.findAllByParentExternalIdAndType(consent.getExternalId(), AuthorisationType.CONSENT);
        Map<String, Integer> usageCounterMap = aisConsentUsageService.getUsageCounterMap(consent);
        return cmsConsentMapper.mapToCmsConsent(consent, authorisations, usageCounterMap);
    }

    private void logConsentAction(String requestedConsentId, ActionStatus actionStatus, String tppId) {
        AisConsentAction action = new AisConsentAction();
        action.setActionStatus(actionStatus);
        action.setRequestedConsentId(requestedConsentId);
        action.setTppId(tppId);
        action.setRequestDate(LocalDate.now());
        aisConsentActionRepository.save(action);
    }
}
