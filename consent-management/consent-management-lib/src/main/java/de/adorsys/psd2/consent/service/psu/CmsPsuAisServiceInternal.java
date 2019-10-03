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

package de.adorsys.psd2.consent.service.psu;


import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentAuthorizationSpecification;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.consent.service.AisConsentRequestTypeService;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.CmsPsuAuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
//TODO Discuss instanceId security workflow https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/577
public class CmsPsuAisServiceInternal implements CmsPsuAisService {
    private final AisConsentRepository aisConsentRepository;
    private final AisConsentMapper consentMapper;
    private final AisConsentAuthorisationRepository aisConsentAuthorisationRepository;
    private final AisConsentAuthorizationSpecification aisConsentAuthorizationSpecification;
    private final AisConsentSpecification aisConsentSpecification;
    private final AisConsentService aisConsentService;
    private final PsuDataMapper psuDataMapper;
    private final AisConsentUsageService aisConsentUsageService;
    private final CmsPsuService cmsPsuService;
    private final AisConsentRequestTypeService aisConsentRequestTypeService;
    private final CmsPsuAuthorisationMapper cmsPsuPisAuthorisationMapper;
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;

    @Override
    @Transactional
    public boolean updatePsuDataInConsent(@NotNull PsuIdData psuIdData, @NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException {
        return getAuthorisationByExternalId(authorisationId, instanceId)
                   .map(auth -> updatePsuData(auth, psuIdData))
                   .orElseGet(() -> {
                       log.info("Authorisation ID [{}], Instance ID: [{}]. Update PSU  in consent failed, because authorisation not found",
                                authorisationId, instanceId);
                       return false;
                   });
    }

    @Override
    @Transactional
    public @NotNull Optional<CmsAisConsentResponse> checkRedirectAndGetConsent(@NotNull String redirectId,
                                                                               @NotNull String instanceId) throws RedirectUrlIsExpiredException {

        Optional<AisConsentAuthorization> optionalAuthorisation = aisConsentAuthorisationRepository
                                                                      .findOne(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(redirectId, instanceId));

        if (optionalAuthorisation.isPresent()) {
            AisConsentAuthorization authorisation = optionalAuthorisation.get();

            if (!authorisation.isRedirectUrlNotExpired()) {
                log.info("Authorisation ID [{}]. Check redirect URL and get consent failed, because authorisation is expired",
                         redirectId);
                updateAuthorisationOnExpiration(authorisation);

                throw new RedirectUrlIsExpiredException(authorisation.getTppNokRedirectUri());
            }
            return createCmsAisConsentResponseFromAisConsent(authorisation, redirectId);
        }

        log.info("Authorisation ID [{}]. Check redirect URL and get consent failed, because authorisation not found or has finalised status",
                 redirectId);
        return Optional.empty();
    }

    @Override
    @Transactional
    public @NotNull Optional<AisAccountConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return aisConsentRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId))
                   .map(this::checkAndUpdateOnExpiration)
                   .map(consentMapper::mapToAisAccountConsent);
    }

    @Override
    public @NotNull Optional<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(@NotNull String authorisationId, @NotNull String instanceId) {
        Optional<AisConsentAuthorization> optionalAuthorisation = aisConsentAuthorisationRepository
                                                                      .findOne(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));

        if (optionalAuthorisation.isPresent()) {
            AisConsentAuthorization authorisation = optionalAuthorisation.get();
            return Optional.of(cmsPsuPisAuthorisationMapper.mapToCmsPsuAuthorisationAis(authorisation));
        }

        log.info("Authorisation ID: [{}], Instance ID: [{}]. Get authorisation failed, because authorisation not found",
                 authorisationId, instanceId);

        return Optional.empty();

    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId,
                                             @NotNull String authorisationId, @NotNull ScaStatus status,
                                             @NotNull String instanceId) throws AuthorisationIsExpiredException {
        Optional<AisConsent> actualAisConsent = getActualAisConsent(consentId, instanceId);

        if (!actualAisConsent.isPresent()) {
            log.info("Consent ID: [{}]. Update of authorisation status failed, because consent either has finalised status or not found", consentId);
            return false;
        }

        return getAuthorisationByExternalId(authorisationId, instanceId)
                   .map(auth -> updateScaStatus(status, auth))
                   .orElseGet(() -> {
                       log.info("Authorisation ID [{}], Instance ID: [{}]. Update authorisation status failed, because authorisation not found",
                                authorisationId, instanceId);
                       return false;
                   });
    }

    @Override
    @Transactional
    public boolean confirmConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        if (changeConsentStatus(consentId, VALID, instanceId)) {
            aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);
            return true;
        }
        log.info("Consent ID [{}]. Confirmation of consent failed because consent has finalised status or not found",
                 consentId);
        return false;
    }

    @Override
    @Transactional
    public boolean rejectConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return changeConsentStatus(consentId, REJECTED, instanceId);
    }

    @Override
    public @NotNull List<AisAccountConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData, @NotNull String instanceId) {
        if (psuIdData.isEmpty()) {
            return Collections.emptyList();
        }

        return aisConsentRepository.findAll(aisConsentSpecification.byPsuDataInListAndInstanceId(psuIdData, instanceId)).stream()
                   .map(consentMapper::mapToAisAccountConsent)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean revokeConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return changeConsentStatus(consentId, REVOKED_BY_PSU, instanceId);
    }

    @Override
    @Transactional
    public boolean authorisePartiallyConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return changeConsentStatus(consentId, PARTIALLY_AUTHORISED, instanceId);
    }

    @Override
    @Transactional
    public boolean updateAccountAccessInConsent(@NotNull String consentId, @NotNull CmsAisConsentAccessRequest accountAccessRequest, @NotNull String instanceId) {
        Optional<AisConsent> aisConsentOptional = getActualAisConsent(consentId, instanceId);
        if (aisConsentOptional.isPresent()) {
            return updateAccountAccessInConsent(aisConsentOptional.get(), accountAccessRequest);
        }
        log.info("Consent ID [{}]. Update account access in consent failed, because consent not found or has finalised status",
                 consentId);
        return false;
    }

    @Override
    public Optional<List<CmsAisPsuDataAuthorisation>> getPsuDataAuthorisations(@NotNull String consentId, @NotNull String instanceId) {
        return getActualAisConsent(consentId, instanceId)
                   .map(AisConsent::getAuthorizations)
                   .map(this::getPsuDataAuthorisations);
    }

    @NotNull
    private List<CmsAisPsuDataAuthorisation> getPsuDataAuthorisations(List<AisConsentAuthorization> authorisations) {
        return authorisations.stream()
                   .filter(auth -> Objects.nonNull(auth.getPsuData()))
                   .map(auth -> new CmsAisPsuDataAuthorisation(psuDataMapper.mapToPsuIdData(auth.getPsuData()),
                                                               auth.getExternalId(),
                                                               auth.getScaStatus(),
                                                               // Here we use hardcoded value of enum, because AIS consent can not be in any other status than 'CREATED'.
                                                               PaymentAuthorisationType.CREATED))
                   .collect(Collectors.toList());
    }

    private boolean updateAccountAccessInConsent(AisConsent consent, CmsAisConsentAccessRequest request) {
        AisAccountAccess accountAccess = request.getAccountAccess();
        if (accountAccess == null) {
            log.info("Consent ID [{}]. Update account access in consent failed, because AIS Account Access is null",
                     consent.getExternalId());
            return false;
        }

        Function<String, AccountAccessType> accountAccessTypeConverter = (description) -> AccountAccessType.getByDescription(description).orElse(null);
        consent.setAllPsd2(accountAccessTypeConverter.apply(accountAccess.getAllPsd2()));
        consent.setAvailableAccounts(accountAccessTypeConverter.apply(accountAccess.getAvailableAccounts()));
        consent.setAvailableAccountsWithBalance(accountAccessTypeConverter.apply(accountAccess.getAvailableAccountsWithBalance()));

        Optional.ofNullable(request.getRecurringIndicator())
            .ifPresent(consent::setRecurringIndicator);
        Optional.ofNullable(request.getCombinedServiceIndicator())
            .ifPresent(consent::setCombinedServiceIndicator);

        Set<AspspAccountAccess> aspspAccountAccesses = consentMapper.mapAspspAccountAccesses(accountAccess);
        setAdditionalInformationTypes(consent, accountAccess.getAccountAdditionalInformationAccess());

        consent.addAspspAccountAccess(aspspAccountAccesses);
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromConsent(consent);
        consent.setAisConsentRequestType(aisConsentRequestType);

        consent.setExpireDate(request.getValidUntil());
        consent.setAllowedFrequencyPerDay(request.getFrequencyPerDay());
        aisConsentUsageService.resetUsage(consent);
        aisConsentRepository.save(consent);
        return true;
    }

    private void setAdditionalInformationTypes(AisConsent consent, AdditionalInformationAccess info) {
        if (info == null) {
            consent.setOwnerNameType(AdditionalAccountInformationType.NONE);
            consent.setOwnerAddressType(AdditionalAccountInformationType.NONE);
        } else {
            consent.setOwnerNameType(AdditionalAccountInformationType.findTypeByList(info.getOwnerName()));
            consent.setOwnerAddressType(AdditionalAccountInformationType.findTypeByList(info.getOwnerAddress()));
        }
    }

    private boolean changeConsentStatus(String consentId, ConsentStatus status, String instanceId) {
        return aisConsentRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId))
                   .map(con -> updateConsentStatus(con, status))
                   .orElseGet(() -> {
                       log.info("Consent ID [{}], Instance ID: [{}]. Change consent status failed, because AIS consent not found",
                                consentId, instanceId);
                       return false;
                   });
    }

    private AisConsent checkAndUpdateOnExpiration(AisConsent consent) {
        if (aisConsentConfirmationExpirationService.isConsentExpiredOrFinalised(consent)) {
            aisConsentConfirmationExpirationService.expireConsent(consent);
        } else {
            log.info("Get consent failed in checkAndUpdateOnExpiration method, because consent is null or expired or has finalised status.");
        }
        return consent;
    }

    private Optional<AisConsent> getActualAisConsent(String consentId, String instanceId) {
        return aisConsentRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId))
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private boolean updateConsentStatus(AisConsent consent, ConsentStatus status) {
        if (consent.getConsentStatus().isFinalisedStatus()) {
            log.info("Consent ID: [{}], Consent status: [{}]. Confirmation of consent failed in updateConsentStatus method, because consent has finalised status",
                     consent.getExternalId(), consent.getConsentStatus().getValue());
            return false;
        }
        if (status == PARTIALLY_AUTHORISED) {
            consent.setMultilevelScaRequired(true);
        }
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);
        return aisConsentRepository.save(consent) != null;
    }

    private boolean updatePsuData(AisConsentAuthorization authorisation, PsuIdData psuIdData) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData);

        if (newPsuData == null || StringUtils.isBlank(newPsuData.getPsuId())) {
            log.info("Authorisation ID : [{}]. Update PSU data in consent failed in updatePsuData method, because newPsuData or psuId in newPsuData is empty or null.",
                     authorisation.getId());
            return false;
        }

        Optional<PsuData> optionalPsuData = Optional.ofNullable(authorisation.getPsuData());
        if (optionalPsuData.isPresent()) {
            newPsuData.setId(optionalPsuData.get().getId());
        } else {
            log.info("Authorisation ID [{}]. No PSU data available in the authorization.", authorisation.getId());
            List<PsuData> psuDataList = authorisation.getConsent().getPsuDataList();
            Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(newPsuData, psuDataList);
            if (psuDataOptional.isPresent()) {
                newPsuData = psuDataOptional.get();
                authorisation.getConsent().setPsuDataList(cmsPsuService.enrichPsuData(newPsuData, psuDataList));
            }
        }

        authorisation.setPsuData(newPsuData);
        aisConsentAuthorisationRepository.save(authorisation);
        return true;
    }

    private boolean updateScaStatus(@NotNull ScaStatus status, AisConsentAuthorization authorisation) {
        if (authorisation.getScaStatus().isFinalisedStatus()) {
            log.info("Authorisation ID [{}], SCA status [{}]. Update authorisation status failed in updateScaStatus method because authorisation has finalised status.", authorisation.getId(),
                     authorisation.getScaStatus().getValue());
            return false;
        }
        authorisation.setScaStatus(status);
        return aisConsentAuthorisationRepository.save(authorisation) != null;
    }

    private void updateAuthorisationOnExpiration(AisConsentAuthorization authorisation) {
        authorisation.setScaStatus(ScaStatus.FAILED);
        aisConsentAuthorisationRepository.save(authorisation);
    }

    private Optional<CmsAisConsentResponse> createCmsAisConsentResponseFromAisConsent(AisConsentAuthorization authorisation, String redirectId) {
        AisConsent aisConsent = authorisation.getConsent();
        if (aisConsent == null) {
            log.info("Authorisation ID [{}]. Check redirect URL and get consent failed in createCmsAisConsentResponseFromAisConsent method, because AIS consent is null",
                     redirectId);
            return Optional.empty();
        }

        AisAccountConsent aisAccountConsent = consentMapper.mapToAisAccountConsent(aisConsent);
        return Optional.of(new CmsAisConsentResponse(aisAccountConsent, redirectId, authorisation.getTppOkRedirectUri(),
                                                     authorisation.getTppNokRedirectUri()));
    }

    private Optional<AisConsentAuthorization> getAuthorisationByExternalId(@NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException {
        Optional<AisConsentAuthorization> authorization = aisConsentAuthorisationRepository.findOne(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));

        if (authorization.isPresent() && !authorization.get().isAuthorisationNotExpired()) {
            log.info("Authorisation ID [{}], Instance ID: [{}]. Authorisation is expired", authorisationId, instanceId);
            throw new AuthorisationIsExpiredException(authorization.get().getTppNokRedirectUri());
        }
        return authorization;
    }
}
