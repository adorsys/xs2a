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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisFundsExportService;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.migration.PiisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsAspspPiisFundsExportServiceInternal implements CmsAspspPiisFundsExportService {
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    private final ConsentJpaRepository consentJpaRepository;
    private final PiisConsentEntitySpecification piisConsentEntitySpecification;
    private final PiisConsentMapper piisConsentMapper;
    private final PiisConsentLazyMigrationService piisConsentLazyMigrationService;
    private final PageRequestBuilder pageRequestBuilder;

    @Override
    @Transactional
    public PageData<Collection<CmsPiisConsent>> exportConsentsByTpp(String tppAuthorisationNumber,
                                                                    @Nullable LocalDate createDateFrom,
                                                                    @Nullable LocalDate createDateTo, @Nullable PsuIdData psuIdData,
                                                                    @Nullable String instanceId,
                                                                    Integer pageIndex, Integer itemsPerPage) {
        if (StringUtils.isBlank(tppAuthorisationNumber)) {
            log.info("TPP ID: [{}], instanceId: [{}]. Export consents by TPP failed, TPP ID is empty or null.",
                     tppAuthorisationNumber, instanceId);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        String actualInstanceId = StringUtils.defaultIfEmpty(instanceId, DEFAULT_SERVICE_INSTANCE_ID);
        return mapToPageData(consentJpaRepository.findAll(
            piisConsentEntitySpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(tppAuthorisationNumber, createDateFrom, createDateTo, psuIdData, actualInstanceId, null),
            pageRequestBuilder.getPageable(pageIndex, itemsPerPage)));
    }

    @Override
    @Transactional
    public PageData<Collection<CmsPiisConsent>> exportConsentsByPsu(PsuIdData psuIdData, @Nullable LocalDate createDateFrom,
                                                                    @Nullable LocalDate createDateTo, @Nullable String instanceId,
                                                                    Integer pageIndex, Integer itemsPerPage) {
        if (psuIdData == null || psuIdData.isEmpty()) {
            log.info("InstanceId: [{}]. Export consents by psu failed, psuIdData is empty or null.", instanceId);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        String actualInstanceId = StringUtils.defaultIfEmpty(instanceId, DEFAULT_SERVICE_INSTANCE_ID);
        return mapToPageData(consentJpaRepository.findAll(
            piisConsentEntitySpecification.byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(psuIdData, createDateFrom, createDateTo, actualInstanceId, null),
            pageRequestBuilder.getPageable(pageIndex, itemsPerPage)));
    }

    @Override
    @Transactional
    public PageData<Collection<CmsPiisConsent>> exportConsentsByAccountId(@NotNull String aspspAccountId,
                                                                          @Nullable LocalDate createDateFrom,
                                                                          @Nullable LocalDate createDateTo,
                                                                          @Nullable String instanceId,
                                                                          Integer pageIndex, Integer itemsPerPage) {
        if (StringUtils.isBlank(aspspAccountId)) {
            log.info("InstanceId: [{}]. Export consents by accountId failed, aspspAccountId is empty or null.", instanceId);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        String actualInstanceId = StringUtils.defaultIfEmpty(instanceId, DEFAULT_SERVICE_INSTANCE_ID);
        return mapToPageData(consentJpaRepository.findAll(
            piisConsentEntitySpecification.byAspspAccountIdAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(aspspAccountId, createDateFrom, createDateTo, actualInstanceId, null),
            pageRequestBuilder.getPageable(pageIndex, itemsPerPage)));
    }

    private PageData<Collection<CmsPiisConsent>> mapToPageData(Page<ConsentEntity> entities) {
        return new PageData<>(entities
                                  .stream()
                                  .map(piisConsentLazyMigrationService::migrateIfNeeded)
                                  .map(piisConsentMapper::mapToCmsPiisConsent)
                                  .collect(Collectors.toList()),
                              entities.getPageable().getPageNumber(),
                              entities.getPageable().getPageSize(),
                              entities.getTotalElements());
    }
}
