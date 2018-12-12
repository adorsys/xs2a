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
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
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
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    public boolean updatePsuDataInConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId) {
        return getAisConsentById(consentId)
                   .map(con -> updatePsuData(con, psuIdData))
                   .orElse(false);
    }

    @Override
    @Transactional
    public @NotNull Optional<AisAccountConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId) {
        return getAisConsentById(consentId)
                   .map(this::checkAndUpdateOnExpiration)
                   .map(consentMapper::mapToAisAccountConsent);
    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String authorisationId, @NotNull ScaStatus status) {
        Optional<AisConsent> actualAisConsent = getActualAisConsent(consentId);

        if (!actualAisConsent.isPresent()) {
            return false;
        }

        return aisConsentAuthorizationRepository.findByExternalId(authorisationId)
                   .map(auth -> updateScaStatus(status, auth))
                   .orElse(false);
    }

    @Override
    @Transactional
    public boolean confirmConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId) {
        return changeConsentStatus(consentId, VALID);
    }

    @Override
    @Transactional
    public boolean rejectConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId) {
        return changeConsentStatus(consentId, REJECTED);
    }

    @Override
    public @NotNull List<AisAccountConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData) {
        return aisConsentRepository.findByPsuDataPsuId(psuIdData.getPsuId()).stream()
                   .map(consentMapper::mapToAisAccountConsent)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean revokeConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId) {
        return changeConsentStatus(consentId, REVOKED_BY_PSU);
    }

    @Override
    @Transactional
    public @NotNull Optional<CmsAisConsentResponse> checkRedirectAndGetConsent(@NotNull PsuIdData psuIdData, @NotNull String redirectId) {
        Optional<AisConsentAuthorization> authorisationOptional = aisConsentAuthorizationRepository.findByExternalId(redirectId);

        if (!authorisationOptional.isPresent()) {
            return Optional.empty();
        }

        AisConsentAuthorization authorisation = authorisationOptional.get();

        if (authorisation.isExpired()) {
            updateAuthorisationOnExpiration(authorisation);
            return Optional.empty();
        }

        return createCmsAisConsentResponseFromAisConsent(authorisation.getConsent(), redirectId);
    }

    private boolean changeConsentStatus(String consentId, ConsentStatus status) {
        return getAisConsentById(consentId)
                   .map(con -> updateConsentStatus(con, status))
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

    private Optional<AisConsent> getActualAisConsent(String encryptedConsentId) {
        Optional<String> consentIdDecrypted = securityDataService.decryptId(encryptedConsentId);
        return consentIdDecrypted
                   .flatMap(aisConsentRepository::findByExternalId)
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private Optional<AisConsent> getAisConsentById(String encryptedConsentId) {
        Optional<String> consentIdDecrypted = securityDataService.decryptId(encryptedConsentId);
        return consentIdDecrypted
                   .flatMap(aisConsentRepository::findByExternalId);
    }

    private boolean updateConsentStatus(AisConsent consent, ConsentStatus status) {
        if (consent.getConsentStatus().isFinalisedStatus()) {
            return false;
        }
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

    private boolean updateScaStatus(@NotNull ScaStatus status, AisConsentAuthorization authorization) {
        if (authorization.getScaStatus().isFinalisedStatus()) {
            return false;
        }
        authorization.setScaStatus(status);
        return aisConsentAuthorizationRepository.save(authorization) != null;
    }

    private void updateAuthorisationOnExpiration(AisConsentAuthorization authorisation) {
        authorisation.setScaStatus(ScaStatus.FAILED);
        aisConsentAuthorizationRepository.save(authorisation);
    }

    private Optional<CmsAisConsentResponse> createCmsAisConsentResponseFromAisConsent(AisConsent aisConsent, String redirectId) {
        if (aisConsent == null) {
            return Optional.empty();
        }

        AisAccountConsent aisAccountConsent = consentMapper.mapToAisAccountConsent(aisConsent);

        Optional<TppInfo> tppInfoOptional = Optional.ofNullable(aisAccountConsent)
                                                .map(AisAccountConsent::getTppInfo);

        if (!tppInfoOptional.isPresent()) {
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
