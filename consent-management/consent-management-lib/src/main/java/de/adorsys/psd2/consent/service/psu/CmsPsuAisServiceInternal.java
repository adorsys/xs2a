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


import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentAuthorizationSpecification;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
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
    @Override
    @Transactional
    public boolean updatePsuDataInConsent(@NotNull PsuIdData psuIdData, @NotNull String authorisationId, @NotNull String instanceId) {
        AisConsentAuthorization authorisation = aisConsentAuthorisationRepository.findOne(
            aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));
        return Optional.ofNullable(authorisation)
                   .map(auth -> updatePsuData(auth, psuIdData))
                   .orElseGet(() -> {
                       log.info("Authorisation ID [{}], Instance ID: [{}]. Update PSU  in consent failed, because authorisation not found",
                                instanceId, authorisationId);
                       return false;
                   });
    }

    @Override
    @Transactional
    public @NotNull Optional<AisAccountConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return Optional.ofNullable(aisConsentRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId)))
                   .map(this::checkAndUpdateOnExpiration)
                   .map(consentMapper::mapToAisAccountConsent);
    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String authorisationId, @NotNull ScaStatus status, @NotNull String instanceId) {
        Optional<AisConsent> actualAisConsent = getActualAisConsent(consentId, instanceId);

        if (!actualAisConsent.isPresent()) {
            log.info("Consent ID: [{}]. Update of authorisation status failed, because consent either has finalised status or not found", consentId);
            return false;
        }

        return Optional.ofNullable(aisConsentAuthorisationRepository.findOne(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId)))
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
    public @NotNull Optional<CmsAisConsentResponse> checkRedirectAndGetConsent(@NotNull String redirectId, @NotNull String instanceId) {
        Optional<AisConsentAuthorization> optionalAuthorisation = Optional.ofNullable(aisConsentAuthorisationRepository.findOne(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(redirectId, instanceId)))
                                                                      .filter(a -> a.getScaStatus().isNotFinalisedStatus());

        if (optionalAuthorisation.isPresent()) {
            AisConsentAuthorization authorisation = optionalAuthorisation.get();

            if (authorisation.isNotExpired()) {
                return createCmsAisConsentResponseFromAisConsent(authorisation.getConsent(), redirectId);
            } else {
                log.info("Authorisation ID [{}]. Check redirect and get consent failed, because authorisation is expired",
                         redirectId);
            }

            updateAuthorisationOnExpiration(authorisation);
            String tppNokRedirectUri = authorisation.getConsent().getTppInfo().getNokRedirectUri();

            return Optional.of(new CmsAisConsentResponse(tppNokRedirectUri));
        }

        log.info("Authorisation ID [{}]. Check redirect and get consent failed, because authorisation not found or has finalised status",
                 redirectId);
        return Optional.empty();
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
                                                               CmsAuthorisationType.CREATED))
                   .collect(Collectors.toList());
    }

    private boolean updateAccountAccessInConsent(AisConsent consent, CmsAisConsentAccessRequest request) {
        Set<AspspAccountAccess> aspspAccountAccesses = consentMapper.mapAspspAccountAccesses(request.getAccountAccess());
        consent.addAspspAccountAccess(aspspAccountAccesses);
        consent.setExpireDate(request.getValidUntil());
        consent.setAllowedFrequencyPerDay(request.getFrequencyPerDay());
        aisConsentUsageService.resetUsage(consent);
        aisConsentRepository.save(consent);
        return true;
    }

    private boolean changeConsentStatus(String consentId, ConsentStatus status, String instanceId) {
        return Optional.ofNullable(aisConsentRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId)))
                   .map(con -> updateConsentStatus(con, status))
                   .orElseGet(() -> {
                       log.info("Consent ID [{}], Instance ID: [{}]. Change consent status failed, because AIS consent not found",
                                consentId, instanceId);
                       return false;
                   });
    }

    private AisConsent checkAndUpdateOnExpiration(AisConsent consent) {
        if (consent != null && consent.isExpiredByDate() && consent.isStatusNotExpired()) {
            consent.setConsentStatus(EXPIRED);
            consent.setExpireDate(LocalDate.now());
            consent.setLastActionDate(LocalDate.now());
            aisConsentRepository.save(consent);
        } else {
            log.info("Get consent failed in checkAndUpdateOnExpiration method, because consent is null or expired.");
        }
        return consent;
    }

    private Optional<AisConsent> getActualAisConsent(String consentId, String instanceId) {
        return Optional.ofNullable(aisConsentRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId)))
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private boolean updateConsentStatus(AisConsent consent, ConsentStatus status) {
        if (consent.getConsentStatus().isFinalisedStatus()) {
            log.info("Consent ID: [{}], Consent status: status [{}]. Confirmation of consent failed in updateConsentStatus method, because consent has finalised status",
                     consent.getExternalId(), consent.getConsentStatus().getValue());
            return false;
        }
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);
        return aisConsentRepository.save(consent) != null;
    }

    private boolean updatePsuData(AisConsentAuthorization authorisation, PsuIdData psuIdData) {
        PsuData newPsuData = psuDataMapper.mapToPsuData(psuIdData);

        if (newPsuData == null || StringUtils.isBlank(newPsuData.getPsuId())) {
            log.info("Authorisation ID : [{}]. Update PSU data in consent failed in updatePsuData method, because newPsuData or psuId in newPsuData is empty or null. ",
                     authorisation.getId());
            return false;
        }

        Optional<PsuData> optionalPsuData = Optional.ofNullable(authorisation.getPsuData());
        if (optionalPsuData.isPresent()) {
            newPsuData.setId(optionalPsuData.get().getId());
        } else {
            log.info("Authorisation ID [{}]. no psu data available in the authorization.", authorisation.getId());
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

    private Optional<CmsAisConsentResponse> createCmsAisConsentResponseFromAisConsent(AisConsent aisConsent, String
                                                                                                                 redirectId) {
        if (aisConsent == null) {
            log.info("Authorisation ID [{}]. Check redirect and get consent failed in createCmsAisConsentResponseFromAisConsent method, because AIS consent is null");
            return Optional.empty();
        }

        AisAccountConsent aisAccountConsent = consentMapper.mapToAisAccountConsent(aisConsent);

        Optional<TppInfo> tppInfoOptional = Optional.ofNullable(aisAccountConsent)
                                                .map(AisAccountConsent::getTppInfo);

        if (!tppInfoOptional.isPresent()) {
            log.info("Authorisation ID [{}]. Check redirect and get consent failed in createCmsAisConsentResponseFromAisConsent method, because TPP info is not present");
            return Optional.empty();
        }

        Optional<TppRedirectUri> tppRedirectUriOptional = tppInfoOptional.map(TppInfo::getTppRedirectUri);

        String tppOkRedirectUri = null;
        String tppNokRedirectUri = null;

        if (tppRedirectUriOptional.isPresent()) {
            TppRedirectUri tppRedirectUri = tppRedirectUriOptional.get();
            tppOkRedirectUri = tppRedirectUri.getUri();
            tppNokRedirectUri = tppRedirectUri.getNokUri();
        }

        return Optional.of(new CmsAisConsentResponse(aisAccountConsent, redirectId, tppOkRedirectUri, tppNokRedirectUri));
    }
}
