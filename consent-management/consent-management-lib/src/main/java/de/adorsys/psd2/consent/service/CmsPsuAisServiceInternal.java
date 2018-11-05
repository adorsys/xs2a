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

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.repository.AisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// TODO temporary solution to switch off Hibernate dirty check. Need to understand why objects are changed here. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
public class CmsPsuAisServiceInternal implements CmsPsuAisService {
    private final AisConsentRepository aisConsentRepository;
    private final AisConsentMapper consentMapper;
    private final PsuDataRepository psuDataRepository;
    private final AisConsentAuthorizationRepository aisConsentAuthorizationRepository;
    private final SecurityDataService securityDataService;

    @Override
    @Transactional
    public boolean updatePsuDataInConsent(@NotNull PsuIdData psuIdData, @NotNull String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(this::getAisConsentById)
                   .map(con -> updatePsuData(con, psuIdData))
                   .orElse(false);
    }

    @Override
    public @NotNull Optional<AisAccountConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(this::getAisConsentById)
                   .map(this::checkAndUpdateOnExpiration)
                   .map(consentMapper::mapToAisAccountConsent);
    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String encryptedConsentId, @NotNull String authorisationId, @NotNull ScaStatus status) {
        Optional<AisConsent> actualAisConsent = securityDataService.decryptId(encryptedConsentId)
                                                    .flatMap(this::getActualAisConsent);

        if (!actualAisConsent.isPresent()) {
            return false;
        }

        return aisConsentAuthorizationRepository.findByExternalId(authorisationId)
                   .map(auth -> updateScaStatus(status, auth))
                   .orElse(false);
    }

    @Override
    @Transactional
    public boolean confirmConsent(@NotNull PsuIdData psuIdData, @NotNull String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .map(consentId -> changeConsentStatus(consentId, VALID))
                   .orElse(false);
    }

    @Override
    @Transactional
    public boolean rejectConsent(@NotNull PsuIdData psuIdData, @NotNull String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .map(consentId -> changeConsentStatus(consentId, REJECTED))
                   .orElse(false);
    }

    @Override
    public @NotNull List<AisAccountConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData) {
        return aisConsentRepository.findByPsuDataPsuId(psuIdData.getPsuId()).stream()
                   .map(consentMapper::mapToAisAccountConsent)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean revokeConsent(@NotNull PsuIdData psuIdData, @NotNull String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
            .map(consentId -> changeConsentStatus(consentId, REVOKED_BY_PSU))
            .orElse(false);
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

    private Optional<AisConsent> getActualAisConsent(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(c -> aisConsentRepository.findByExternalIdAndConsentStatusIn(c, EnumSet.of(RECEIVED, VALID)));
    }

    private boolean changeConsentStatus(String consentId, ConsentStatus status) {
        return getAisConsentById(consentId)
                   .map(con -> updateConsentStatus(con, status))
                   .orElse(false);
    }

    private Optional<AisConsent> getAisConsentById(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(aisConsentRepository::findByExternalId);
    }

    private boolean updateConsentStatus(AisConsent consent, ConsentStatus status) {
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);
        return aisConsentRepository.save(consent) != null;
    }

    private boolean updatePsuData(AisConsent consent, PsuIdData psuIdData) {
        PsuData psuData = consent.getPsuData();
        psuData.setPsuId(psuIdData.getPsuId());
        psuData.setPsuIdType(psuIdData.getPsuIdType());
        psuData.setPsuCorporateId(psuIdData.getPsuCorporateId());
        psuData.setPsuCorporateIdType(psuIdData.getPsuCorporateIdType());

        return psuDataRepository.save(psuData) != null;
    }

    private boolean updateScaStatus(@NotNull ScaStatus status, AisConsentAuthorization auth) {
        auth.setScaStatus(status);
        return aisConsentAuthorizationRepository.save(auth) != null;
    }
}
