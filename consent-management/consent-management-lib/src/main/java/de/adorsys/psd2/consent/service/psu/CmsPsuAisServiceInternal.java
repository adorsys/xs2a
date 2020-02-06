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

package de.adorsys.psd2.consent.service.psu;


import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.consent.service.AisConsentRequestTypeService;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.CmsPsuAuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
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
    private final AisConsentJpaRepository aisConsentJpaRepository;
    private final AisConsentVerifyingRepository aisConsentRepository;
    private final AisConsentMapper consentMapper;
    private final AuthorisationRepository authorisationRepository;
    private final AuthorisationSpecification authorisationSpecification;
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

        Optional<AuthorisationEntity> optionalAuthorisation = authorisationRepository
                                                                  .findOne(authorisationSpecification.byExternalIdAndInstanceId(redirectId, instanceId));

        if (optionalAuthorisation.isPresent()) {
            AuthorisationEntity authorisation = optionalAuthorisation.get();

            if (!authorisation.isRedirectUrlNotExpired()) {
                log.info("Authorisation ID [{}]. Check redirect URL and get consent failed, because authorisation is expired",
                         redirectId);
                updateAuthorisationOnExpiration(authorisation);

                throw new RedirectUrlIsExpiredException(authorisation.getTppNokRedirectUri());
            }
            return createCmsAisConsentResponseFromAuthorisation(authorisation, redirectId);
        }

        log.info("Authorisation ID [{}]. Check redirect URL and get consent failed, because authorisation not found or has finalised status",
                 redirectId);
        return Optional.empty();
    }

    @Override
    @Transactional
    public @NotNull Optional<CmsAisAccountConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return aisConsentJpaRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId))
                   .map(this::checkAndUpdateOnExpiration)
                   .map(this::mapToCmsAisAccountConsentWithAuthorisations);
    }

    @Override
    public @NotNull Optional<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(@NotNull String authorisationId, @NotNull String instanceId) {
        Optional<AuthorisationEntity> optionalAuthorisation = authorisationRepository
                                                                  .findOne(authorisationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));

        if (optionalAuthorisation.isPresent()) {
            AuthorisationEntity authorisation = optionalAuthorisation.get();
            return Optional.of(cmsPsuPisAuthorisationMapper.mapToCmsPsuAuthorisation(authorisation));
        }

        log.info("Authorisation ID: [{}], Instance ID: [{}]. Get authorisation failed, because authorisation not found",
                 authorisationId, instanceId);

        return Optional.empty();

    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId,
                                             @NotNull String authorisationId, @NotNull ScaStatus status,
                                             @NotNull String instanceId, AuthenticationDataHolder authenticationDataHolder) throws AuthorisationIsExpiredException {
        Optional<AisConsent> actualAisConsent = getActualAisConsent(consentId, instanceId);

        if (!actualAisConsent.isPresent()) {
            log.info("Consent ID: [{}]. Update of authorisation status failed, because consent either has finalised status or not found", consentId);
            return false;
        }

        return getAuthorisationByExternalId(authorisationId, instanceId)
                   .map(authorisation -> updateScaStatusAndAuthenticationData(status, authorisation, authenticationDataHolder))
                   .orElseGet(() -> {
                       log.info("Authorisation ID [{}], Instance ID: [{}]. Update authorisation status failed, because authorisation not found",
                                authorisationId, instanceId);
                       return false;
                   });
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public boolean confirmConsent(@NotNull String consentId, @NotNull String instanceId) throws WrongChecksumException {
        if (changeConsentStatus(consentId, VALID, instanceId)) {
            aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);
            return true;
        }
        log.info("Consent ID [{}]. Confirmation of consent failed because consent has finalised status or not found",
                 consentId);
        return false;
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public boolean rejectConsent(@NotNull String consentId, @NotNull String instanceId) throws WrongChecksumException {
        return changeConsentStatus(consentId, REJECTED, instanceId);
    }

    @Override
    public @NotNull List<CmsAisAccountConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData, @NotNull String instanceId) {
        if (psuIdData.isEmpty()) {
            return Collections.emptyList();
        }

        return aisConsentJpaRepository.findAll(aisConsentSpecification.byPsuDataInListAndInstanceId(psuIdData, instanceId)).stream()
                   .map(this::mapToCmsAisAccountConsentWithAuthorisations)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public boolean revokeConsent(@NotNull String consentId, @NotNull String instanceId) throws WrongChecksumException {
        return changeConsentStatus(consentId, REVOKED_BY_PSU, instanceId);
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public boolean authorisePartiallyConsent(@NotNull String consentId, @NotNull String instanceId) throws WrongChecksumException {
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
        Optional<AisConsent> aisConsentOptional = getActualAisConsent(consentId, instanceId);

        if (!aisConsentOptional.isPresent()) {
            return Optional.empty();
        }

        List<AuthorisationEntity> consentAuthorisations = authorisationRepository.findAllByParentExternalIdAndAuthorisationType(aisConsentOptional.get().getExternalId(),
                                                                                                                                AuthorisationType.AIS);
        return Optional.of(getPsuDataAuthorisations(consentAuthorisations));
    }

    @NotNull
    private List<CmsAisPsuDataAuthorisation> getPsuDataAuthorisations(List<AuthorisationEntity> authorisations) {
        return authorisations.stream()
                   .filter(auth -> Objects.nonNull(auth.getPsuData()))
                   .map(auth -> new CmsAisPsuDataAuthorisation(psuDataMapper.mapToPsuIdData(auth.getPsuData()),
                                                               auth.getExternalId(),
                                                               auth.getScaStatus()))
                   .collect(Collectors.toList());
    }

    private boolean updateAccountAccessInConsent(AisConsent consent, CmsAisConsentAccessRequest request) {
        if (consent.getConsentStatus() == VALID) {
            log.info("Consent ID [{}]. Can't execute updateAccountAccessInConsent, because AIS consent has already VALID status.",
                     consent.getExternalId());
            return false;
        }

        LocalDate validUntil = request.getValidUntil();
        if (validUntil != null && validUntil.isBefore(LocalDate.now())) {
            log.info("Consent property validUntil: [{}] is in the past!", validUntil);
            return false;
        }

        AisAccountAccess accountAccess = request.getAccountAccess();
        if (accountAccess == null) {
            log.info("Consent ID [{}]. Update account access in consent failed, because AIS Account Access is null",
                     consent.getExternalId());
            return false;
        }

        Function<String, AccountAccessType> accountAccessTypeConverter = description -> AccountAccessType.getByDescription(description).orElse(null);
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

        consent.setValidUntil(request.getValidUntil());
        consent.setAllowedFrequencyPerDay(request.getFrequencyPerDay());

        aisConsentUsageService.resetUsage(consent);

        try {
            aisConsentRepository.verifyAndUpdate(consent);
        } catch (WrongChecksumException e) {
            log.info("Consent ID [{}]. Update account access in consent failed, because consent has wrong checksum",
                     consent.getExternalId());
            return false;
        }

        return true;
    }

    private void setAdditionalInformationTypes(AisConsent consent, AdditionalInformationAccess info) {
        AdditionalAccountInformationType ownerNameType = info == null
                                                             ? AdditionalAccountInformationType.NONE
                                                             : AdditionalAccountInformationType.findTypeByList(info.getOwnerName());
        consent.setOwnerNameType(ownerNameType);
    }

    private boolean changeConsentStatus(String consentId, ConsentStatus status, String instanceId) throws WrongChecksumException {

        Optional<AisConsent> aisConsentOptional = aisConsentJpaRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId));

        if (aisConsentOptional.isPresent()) {
            return updateConsentStatus(aisConsentOptional.get(), status);
        }

        log.info("Consent ID [{}], Instance ID: [{}]. Change consent status failed, because AIS consent not found",
                 consentId, instanceId);
        return false;
    }

    private AisConsent checkAndUpdateOnExpiration(AisConsent consent) {
        if (consent != null && consent.shouldConsentBeExpired()) {
            return aisConsentConfirmationExpirationService.expireConsent(consent);
        }

        return consent;
    }

    private Optional<AisConsent> getActualAisConsent(String consentId, String instanceId) {
        return aisConsentJpaRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId))
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private boolean updateConsentStatus(AisConsent consent, ConsentStatus status) throws WrongChecksumException {
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

        return aisConsentRepository.verifyAndSave(consent) != null;
    }

    private boolean updatePsuData(AuthorisationEntity authorisation, PsuIdData psuIdData) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData);

        if (newPsuData == null || StringUtils.isBlank(newPsuData.getPsuId())) {
            log.info("Authorisation ID : [{}]. Update PSU data in consent failed in updatePsuData method, because newPsuData or psuId in newPsuData is empty or null.",
                     authorisation.getExternalId());
            return false;
        }

        Optional<PsuData> optionalPsuData = Optional.ofNullable(authorisation.getPsuData());
        if (optionalPsuData.isPresent()) {
            newPsuData.setId(optionalPsuData.get().getId());
        } else {
            log.info("Authorisation ID [{}]. No PSU data available in the authorisation.", authorisation.getExternalId());

            Optional<AisConsent> consentOptional = aisConsentJpaRepository.findByExternalId(authorisation.getParentExternalId());
            if (!consentOptional.isPresent()) {
                log.info("Authorisation ID [{}]. Update PSU data in consent failed, couldn't find consent by the parent ID in the authorisation.",
                         authorisation.getExternalId());
                return false;
            }

            AisConsent aisConsent = consentOptional.get();
            List<PsuData> psuDataList = aisConsent.getPsuDataList();
            Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(newPsuData, psuDataList);
            if (psuDataOptional.isPresent()) {
                newPsuData = psuDataOptional.get();
                aisConsent.setPsuDataList(cmsPsuService.enrichPsuData(newPsuData, psuDataList));
            }
        }

        authorisation.setPsuData(newPsuData);
        authorisationRepository.save(authorisation);
        return true;
    }

    private boolean updateScaStatusAndAuthenticationData(@NotNull ScaStatus status, AuthorisationEntity authorisation, AuthenticationDataHolder authenticationDataHolder) {
        if (authorisation.getScaStatus().isFinalisedStatus()) {
            log.info("Authorisation ID [{}], SCA status [{}]. Update authorisation status failed in updateScaStatusAndAuthenticationData method because authorisation has finalised status.", authorisation.getExternalId(),
                     authorisation.getScaStatus().getValue());
            return false;
        }
        authorisation.setScaStatus(status);

        if (authenticationDataHolder != null) {
            enrichAuthorisationWithAuthenticationData(authorisation, authenticationDataHolder);
        }

        authorisationRepository.save(authorisation);
        return true;
    }

    private void enrichAuthorisationWithAuthenticationData(AuthorisationEntity authorisation, AuthenticationDataHolder authenticationDataHolder) {
        if (authenticationDataHolder.getAuthenticationData() != null) {
            authorisation.setScaAuthenticationData(authenticationDataHolder.getAuthenticationData());
        }
        if (authenticationDataHolder.getAuthenticationMethodId() != null) {
            authorisation.setAuthenticationMethodId(authenticationDataHolder.getAuthenticationMethodId());
        }
    }

    private void updateAuthorisationOnExpiration(AuthorisationEntity authorisation) {
        authorisation.setScaStatus(ScaStatus.FAILED);
        authorisationRepository.save(authorisation);
    }

    private Optional<CmsAisConsentResponse> createCmsAisConsentResponseFromAuthorisation(AuthorisationEntity authorisation, String redirectId) {
        Optional<AisConsent> aisConsent = aisConsentJpaRepository.findByExternalId(authorisation.getParentExternalId());
        if (!aisConsent.isPresent()) {
            log.info("Authorisation ID [{}]. Check redirect URL and get consent failed in createCmsAisConsentResponseFromAisConsent method, because AIS consent is null",
                     redirectId);
            return Optional.empty();
        }

        CmsAisAccountConsent aisAccountConsent = mapToCmsAisAccountConsentWithAuthorisations(aisConsent.get());
        return Optional.of(new CmsAisConsentResponse(aisAccountConsent, redirectId, authorisation.getTppOkRedirectUri(),
                                                     authorisation.getTppNokRedirectUri()));
    }

    private Optional<AuthorisationEntity> getAuthorisationByExternalId(@NotNull String authorisationId, @NotNull String instanceId) throws AuthorisationIsExpiredException {
        Optional<AuthorisationEntity> authorization = authorisationRepository.findOne(authorisationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));

        if (authorization.isPresent() && !authorization.get().isAuthorisationNotExpired()) {
            log.info("Authorisation ID [{}], Instance ID: [{}]. Authorisation is expired", authorisationId, instanceId);
            throw new AuthorisationIsExpiredException(authorization.get().getTppNokRedirectUri());
        }
        return authorization;
    }

    private CmsAisAccountConsent mapToCmsAisAccountConsentWithAuthorisations(AisConsent aisConsent) {
        List<AuthorisationEntity> authorisations =
            authorisationRepository.findAllByParentExternalIdAndAuthorisationType(aisConsent.getExternalId(), AuthorisationType.AIS);
        return consentMapper.mapToCmsAisAccountConsent(aisConsent, authorisations);
    }
}
