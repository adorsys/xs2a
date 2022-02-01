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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.psu.api.CmsPsuPiisService;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.PiisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsPsuPiisServiceInternal implements CmsPsuPiisService {
    private final ConsentJpaRepository consentJpaRepository;
    private final PiisConsentMapper piisConsentMapper;
    private final PsuDataMapper psuDataMapper;
    private final PiisConsentEntitySpecification piisConsentEntitySpecification;
    private final PiisConsentLazyMigrationService piisConsentLazyMigrationService;
    private final PageRequestBuilder pageRequestBuilder;

    @Override
    public @NotNull Optional<CmsPiisConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return consentJpaRepository.findOne(piisConsentEntitySpecification.byConsentIdAndInstanceId(consentId, instanceId))
                   .filter(con -> isPsuIdDataContentEquals(con, psuIdData))
                   .map(piisConsentLazyMigrationService::migrateIfNeeded)
                   .map(piisConsentMapper::mapToCmsPiisConsent);
    }

    @Override
    public @NotNull List<CmsPiisConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData, @NotNull String instanceId, Integer pageIndex, Integer itemsPerPage) {
        if (pageIndex == null && itemsPerPage == null) {
            return consentJpaRepository.findAll(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, instanceId)).stream()
                       .filter(con -> isPsuIdDataContentEquals(con, psuIdData))
                       .map(piisConsentLazyMigrationService::migrateIfNeeded)
                       .map(piisConsentMapper::mapToCmsPiisConsent)
                       .collect(Collectors.toList());
        }
        Pageable pageRequest = pageRequestBuilder.getPageable(pageIndex, itemsPerPage);
        return consentJpaRepository.findAll(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, instanceId), pageRequest).stream()
                   .filter(con -> isPsuIdDataContentEquals(con, psuIdData))
                   .map(piisConsentLazyMigrationService::migrateIfNeeded)
                   .map(piisConsentMapper::mapToCmsPiisConsent)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean revokeConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        Optional<ConsentEntity> piisConsentEntity = consentJpaRepository.findOne(piisConsentEntitySpecification.byConsentIdAndInstanceId(consentId, instanceId))
                                                        .filter(con -> isPsuIdDataContentEquals(con, psuIdData) && !con.getConsentStatus().isFinalisedStatus());
        if (piisConsentEntity.isPresent()) {
            revokeConsent(piisConsentEntity.get());
            return true;
        }

        log.info("Consent ID [{}], Instance ID: [{}]. Revoke consent failed, because given psuData is not equals stored psuData, or consent status is finalised, or consent not found",
                 consentId, instanceId);
        return false;
    }

    private boolean isPsuIdDataContentEquals(ConsentEntity piisConsentEntity, PsuIdData psuIdData) {
        List<PsuIdData> psuIdDataInConsent = piisConsentEntity.getPsuDataList().stream()
                                                 .map(psuDataMapper::mapToPsuIdData)
                                                 .filter(Objects::nonNull)
                                                 .collect(Collectors.toList());

        if (psuIdDataInConsent.isEmpty()) {
            log.info("PIIS Consent ID [{}]. Consent doesn't contain any PSU Data", piisConsentEntity.getExternalId());
            return false;
        }

        return psuIdDataInConsent.stream()
                   .anyMatch(consentPsu -> consentPsu.contentEquals(psuIdData));
    }

    private void revokeConsent(ConsentEntity consent) {
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(ConsentStatus.REVOKED_BY_PSU);
    }
}
