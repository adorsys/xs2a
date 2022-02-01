/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.CmsConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.consent.api.CmsError.LOGICAL_ERROR;
import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsentServiceInternal implements ConsentService {
    private final AuthorisationRepository authorisationRepository;
    private final ConsentJpaRepository consentJpaRepository;
    private final AisConsentVerifyingRepository aisConsentRepository;
    private final TppInfoRepository tppInfoRepository;
    private final PsuDataMapper psuDataMapper;
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final CmsPsuService cmsPsuService;
    private final AisConsentUsageService aisConsentUsageService;
    private final CmsConsentMapper cmsConsentMapper;
    private final AisConsentLazyMigrationService aisConsentLazyMigrationService;
    private final AspspProfileService aspspProfileService;

    /**
     * Creates consent.
     *
     * @param cmsConsent needed parameters for creating consent
     * @return create consent response, containing consent and its encrypted ID
     */
    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsCreateConsentResponse> createConsent(CmsConsent cmsConsent) throws WrongChecksumException {

        if (cmsConsent.getFrequencyPerDay() == null) {
            log.info("TPP ID: [{}]. Consent cannot be created, because request contains no allowed frequency per day",
                     cmsConsent.getTppInformation().getTppInfo().getAuthorisationNumber());
            return CmsResponse.<CmsCreateConsentResponse>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }
        ConsentEntity consentEntityFromMapper = cmsConsentMapper.mapToNewConsentEntity(cmsConsent);
        ConsentEntity consent = adjustConsentEntity(consentEntityFromMapper, cmsConsent.getConsentType());
        tppInfoRepository.findByAuthorisationNumber(cmsConsent.getTppInformation().getTppInfo().getAuthorisationNumber())
            .ifPresent(tppInfo -> consent.getTppInformation().setTppInfo(tppInfo));

        ConsentEntity savedConsent = aisConsentRepository.verifyAndSave(consent);

        if (savedConsent.getId() != null) {
            return CmsResponse.<CmsCreateConsentResponse>builder()
                       .payload(new CmsCreateConsentResponse(savedConsent.getExternalId(),
                                                             cmsConsentMapper.mapToCmsConsent(savedConsent, Collections.emptyList(), Collections.emptyMap())))
                       .build();

        } else {
            log.info("TPP ID: [{}], External Consent ID: [{}]. Consent cannot be created, because when saving to DB got null ID",
                     cmsConsent.getTppInformation().getTppInfo().getAuthorisationNumber(), savedConsent.getExternalId());
            return CmsResponse.<CmsCreateConsentResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }
    }

    /**
     * Reads status of consent by ID.
     *
     * @param consentId ID of consent
     * @return ConsentStatus
     */
    @Override
    @Transactional
    public CmsResponse<ConsentStatus> getConsentStatusById(String consentId) {
        Optional<ConsentStatus> consentStatusOptional = consentJpaRepository.findByExternalId(consentId)
                                                            .map(aisConsentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                                                            .map(this::checkAndUpdateOnExpiration)
                                                            .map(ConsentEntity::getConsentStatus);
        if (consentStatusOptional.isPresent()) {
            return CmsResponse.<ConsentStatus>builder()
                       .payload(consentStatusOptional.get())
                       .build();
        } else {
            log.info("Consent ID: [{}]. Get consent status failed, because consent not found", consentId);
            return CmsResponse.<ConsentStatus>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }
    }

    /**
     * Updates consent status by ID.
     *
     * @param consentId ID of consent
     * @param status    new consent status
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<Boolean> updateConsentStatusById(String consentId, ConsentStatus status) throws WrongChecksumException {
        Optional<ConsentEntity> consentOptional = getActualAisConsent(consentId);

        if (consentOptional.isPresent()) {
            ConsentEntity consent = consentOptional.get();
            boolean result = setStatusAndSaveConsent(consent, status);

            return CmsResponse.<Boolean>builder()
                       .payload(result)
                       .build();
        }

        log.info("Consent ID [{}]. Update consent status by ID failed, because consent not found", consentId);
        return CmsResponse.<Boolean>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    /**
     * Reads full information of consent by ID.
     *
     * @param consentId ID of consent
     * @return AisAccountConsent
     */
    @Override
    @Transactional
    public CmsResponse<CmsConsent> getConsentById(String consentId) {
        Optional<ConsentEntity> consentEntityOptional = consentJpaRepository.findByExternalId(consentId)
                                                            .map(aisConsentConfirmationExpirationService::checkAndUpdateOnConfirmationExpiration)
                                                            .map(this::checkAndUpdateOnExpiration);

        if (consentEntityOptional.isEmpty()) {
            log.info("Consent ID [{}]. Get consent by ID failed, couldn't find consent by its ID", consentId);
            return CmsResponse.<CmsConsent>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        ConsentEntity consentEntity = consentEntityOptional.get();
        consentEntity = aisConsentLazyMigrationService.migrateIfNeeded(consentEntity);

        List<AuthorisationEntity> authorisations = authorisationRepository.findAllByParentExternalIdAndType(consentEntity.getExternalId(), AuthorisationType.CONSENT);
        CmsConsent cmsConsent = cmsConsentMapper.mapToCmsConsent(consentEntity, authorisations, aisConsentUsageService.getUsageCounterMap(consentEntity));

        return CmsResponse.<CmsConsent>builder()
                   .payload(cmsConsent)
                   .build();
    }

    /**
     * Searches the old AIS consents and updates their statuses according to authorisation states and PSU data.
     *
     * @param newConsentId ID of new consent that was created
     * @return true if old consents were updated, false otherwise
     */
    @Override
    @Transactional
    public CmsResponse<Boolean> findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        ConsentEntity newConsent = consentJpaRepository.findByExternalId(newConsentId)
                                       .orElseThrow(() -> {
                                           log.info("Consent ID: [{}]. Cannot find consent by ID", newConsentId);
                                           return new IllegalArgumentException("Wrong consent ID: " + newConsentId);
                                       });

        if (newConsent.isOneAccessType()) {
            log.info("Consent ID: [{}]. Cannot find old consents, because consent is OneAccessType", newConsentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        if (newConsent.isWrongConsentData()) {
            log.info("Consent ID: [{}]. Find old consents failed, because consent PSU data list is empty or TPP Info is null", newConsentId);
            throw new IllegalArgumentException("Wrong consent data");
        }

        List<PsuData> psuDataList = newConsent.getPsuDataList();
        Set<String> psuIds = psuDataList.stream()
                                 .filter(Objects::nonNull)
                                 .map(PsuData::getPsuId)
                                 .collect(Collectors.toSet());
        TppInfoEntity tppInfo = newConsent.getTppInformation().getTppInfo();

        List<ConsentEntity> oldConsents = consentJpaRepository.findOldConsentsByNewConsentParams(psuIds,
                                                                                                 tppInfo.getAuthorisationNumber(),
                                                                                                 newConsent.getInstanceId(),
                                                                                                 newConsent.getExternalId(),
                                                                                                 EnumSet.of(RECEIVED, PARTIALLY_AUTHORISED, VALID));

        List<ConsentEntity> oldConsentsWithExactPsuDataLists = oldConsents.stream()
                                                                   .distinct()
                                                                   .filter(c -> cmsPsuService.isPsuDataListEqual(c.getPsuDataList(), psuDataList))
                                                                   .collect(Collectors.toList());

        if (oldConsentsWithExactPsuDataLists.isEmpty()) {
            log.info("Consent ID: [{}]. Cannot find old consents, because consent hasn't exact PSU data lists as old consents", newConsentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        oldConsentsWithExactPsuDataLists.forEach(this::updateStatus);
        consentJpaRepository.saveAll(oldConsentsWithExactPsuDataLists);
        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    /**
     * Searches the old AIS consents and updates their statuses according to authorisation states and PSU data.
     *
     * @param newConsentId ID of new consent that was created
     * @param request      terminate old consent request
     * @return true if old consents were updated, false otherwise
     */
    @Override
    @Transactional
    public CmsResponse<Boolean> findAndTerminateOldConsents(String newConsentId, TerminateOldConsentsRequest request) {
        if (request.isOneAccessType()) {
            log.info("Consent ID: [{}]. Cannot find old consents, because consent is OneAccessType", newConsentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        if (request.isWrongConsentData()) {
            log.info("Consent ID: [{}]. Find old consents failed, because consent PSU data list is empty or TPP Info is null", newConsentId);
            throw new IllegalArgumentException("Wrong consent data");
        }

        List<PsuData> psuDataList = psuDataMapper.mapToPsuDataList(request.getPsuIdDataList(), request.getInstanceId());

        Set<String> psuIds = psuDataList.stream()
                                 .filter(Objects::nonNull)
                                 .map(PsuData::getPsuId)
                                 .collect(Collectors.toSet());

        List<ConsentEntity> oldConsents = consentJpaRepository.findOldConsentsByNewConsentParams(psuIds,
                                                                                                 request.getAuthorisationNumber(),
                                                                                                 request.getInstanceId(),
                                                                                                 newConsentId,
                                                                                                 EnumSet.of(RECEIVED, PARTIALLY_AUTHORISED, VALID));

        List<ConsentEntity> oldConsentsWithExactPsuDataLists = oldConsents.stream()
                                                                   .distinct()
                                                                   .filter(c -> cmsPsuService.isPsuDataListEqual(c.getPsuDataList(), psuDataList))
                                                                   .collect(Collectors.toList());

        if (oldConsentsWithExactPsuDataLists.isEmpty()) {
            log.info("Consent ID: [{}]. Cannot find old consents, because consent hasn't exact PSU data lists as old consents", newConsentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        oldConsentsWithExactPsuDataLists.forEach(this::updateStatus);
        consentJpaRepository.saveAll(oldConsentsWithExactPsuDataLists);
        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    @Override
    public CmsResponse<List<PsuIdData>> getPsuDataByConsentId(String consentId) {
        Optional<List<PsuIdData>> psuIdDataOptional = getActualAisConsent(consentId)
                                                          .map(ac -> psuDataMapper.mapToPsuIdDataList(ac.getPsuDataList()));

        if (psuIdDataOptional.isPresent()) {
            return CmsResponse.<List<PsuIdData>>builder()
                       .payload(psuIdDataOptional.get())
                       .build();
        }

        log.info("Consent ID [{}]. Get psu data by consent id failed, because consent not found",
                 consentId);
        return CmsResponse.<List<PsuIdData>>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<Boolean> updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) throws WrongChecksumException {
        Optional<ConsentEntity> aisConsentOptional = consentJpaRepository.findByExternalId(consentId);
        if (aisConsentOptional.isEmpty()) {
            log.info("Consent ID: [{}]. Get update multilevel SCA required status failed, because consent authorisation is not found",
                     consentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }
        ConsentEntity consent = aisConsentOptional.get();
        consent.setMultilevelScaRequired(multilevelScaRequired);

        aisConsentRepository.verifyAndSave(consent);

        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    private ConsentEntity checkAndUpdateOnExpiration(ConsentEntity consent) {
        if (consent != null && consent.shouldConsentBeExpired()) {
            return aisConsentConfirmationExpirationService.expireConsent(consent);
        }

        return consent;
    }

    private Optional<ConsentEntity> getActualAisConsent(String consentId) {
        return consentJpaRepository.findByExternalId(consentId)
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private boolean setStatusAndSaveConsent(ConsentEntity consent, ConsentStatus status) throws WrongChecksumException {
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);

        ConsentEntity aisConsent = aisConsentRepository.verifyAndSave(consent);

        return Optional.ofNullable(aisConsent)
                   .isPresent();
    }

    private void updateStatus(ConsentEntity aisConsent) {
        aisConsent.setConsentStatus(aisConsent.getConsentStatus() == RECEIVED || aisConsent.getConsentStatus() == PARTIALLY_AUTHORISED
                                        ? REJECTED
                                        : TERMINATED_BY_TPP);
    }

    private ConsentEntity adjustConsentEntity(ConsentEntity consentEntity, ConsentType consentType) {
        if (ConsentType.AIS == consentType) {
            int lifetime = aspspProfileService.getAspspSettings(consentEntity.getInstanceId()).getAis().getConsentTypes().getMaxConsentValidityDays();
            consentEntity.setValidUntil(adjustValidUntilDate(consentEntity.getValidUntil(), lifetime));
        }

        return consentEntity;
    }

    private LocalDate adjustValidUntilDate(LocalDate date, int lifetime) {
        if (lifetime <= 0) {
            return date;
        }

        //Expire date is inclusive and TPP can access AIS consent from current date
        LocalDate lifeTimeDate = LocalDate.now().plusDays(lifetime - 1L);
        return lifeTimeDate.isBefore(date) ? lifeTimeDate : date;
    }
}
