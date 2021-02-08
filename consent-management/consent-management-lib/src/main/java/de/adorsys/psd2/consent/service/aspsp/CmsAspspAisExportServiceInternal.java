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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmsAspspAisExportServiceInternal implements CmsAspspAisExportService {
    private final AisConsentSpecification aisConsentSpecification;
    private final ConsentJpaRepository consentJpaRepository;
    private final AisConsentMapper aisConsentMapper;
    private final AuthorisationRepository authorisationRepository;
    private final AisConsentLazyMigrationService aisConsentLazyMigrationService;
    private final PageRequestBuilder pageRequestBuilder;

    @Override
    @Transactional
    public PageData<Collection<CmsAisAccountConsent>> exportConsentsByTpp(String tppAuthorisationNumber,
                                                                          @Nullable LocalDate createDateFrom,
                                                                          @Nullable LocalDate createDateTo,
                                                                          @Nullable PsuIdData psuIdData, @NotNull String instanceId,
                                                                          Integer pageIndex, Integer itemsPerPage,
                                                                          @Nullable String additionalTppInfo) {
        if (StringUtils.isBlank(tppAuthorisationNumber) || StringUtils.isBlank(instanceId)) {
            log.info("TPP ID: [{}], InstanceId: [{}]. Export Consents by TPP: Some of these two values are empty", tppAuthorisationNumber, instanceId);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        return mapToPageData(consentJpaRepository.findAll(
            aisConsentSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(tppAuthorisationNumber, createDateFrom, createDateTo, psuIdData, instanceId, additionalTppInfo),
            pageRequestBuilder.getPageable(pageIndex, itemsPerPage)));
    }

    @Override
    @Transactional
    public PageData<Collection<CmsAisAccountConsent>> exportConsentsByPsuAndAdditionalTppInfo(PsuIdData psuIdData, @Nullable LocalDate createDateFrom,
                                                                                              @Nullable LocalDate createDateTo,
                                                                                              @NotNull String instanceId,
                                                                                              Integer pageIndex, Integer itemsPerPage,
                                                                                              @Nullable String additionalTppInfo) {
        if (psuIdData == null || psuIdData.isEmpty() || StringUtils.isBlank(instanceId)) {
            log.info("InstanceId: [{}]. Export consents by Psu failed, psuIdData or instanceId is empty or null.",
                     instanceId);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        return mapToPageData(consentJpaRepository.findAll(
            aisConsentSpecification.byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(psuIdData, createDateFrom, createDateTo, instanceId, additionalTppInfo),
            pageRequestBuilder.getPageable(pageIndex, itemsPerPage)));
    }

    @Override
    @Transactional
    public PageData<Collection<CmsAisAccountConsent>> exportConsentsByAccountIdAndAdditionalTppInfo(@NotNull String aspspAccountId,
                                                                                                    @Nullable LocalDate createDateFrom,
                                                                                                    @Nullable LocalDate createDateTo,
                                                                                                    @NotNull String instanceId,
                                                                                                    Integer pageIndex, Integer itemsPerPage,
                                                                                                    @Nullable String additionalTppInfo) {

        if (StringUtils.isBlank(instanceId)) {
            log.info("InstanceId: [{}], aspspAccountId: [{}]. Export consents by accountId failed, instanceId is empty or null.",
                     instanceId, aspspAccountId);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        ZoneOffset currentOffset = OffsetDateTime.now().getOffset();
        OffsetDateTime startOffsetDateTime = Optional.ofNullable(createDateFrom)
                                                 .map(odt -> OffsetDateTime.of(odt, LocalTime.MIN, currentOffset))
                                                 .orElse(OffsetDateTime.now().minusYears(10));
        OffsetDateTime endOffsetDateTime = Optional.ofNullable(createDateTo)
                                               .map(odt -> OffsetDateTime.of(odt, LocalTime.MAX, currentOffset))
                                               .orElse(OffsetDateTime.now().plusYears(10));

        Pageable pageable = pageRequestBuilder.getPageable(pageIndex, itemsPerPage);
        Page<ConsentEntity> consentEntityPage = additionalTppInfo == null
                                                    ? consentJpaRepository
                                                          .findAllWithPagination(Collections.singleton(ConsentType.AIS.getName()), aspspAccountId, startOffsetDateTime,
                                                                                 endOffsetDateTime, instanceId, pageable)
                                                    : consentJpaRepository
                                                          .findAllWithPaginationAndTppInfo(Collections.singleton(ConsentType.AIS.getName()), aspspAccountId, startOffsetDateTime,
                                                                                           endOffsetDateTime, instanceId, pageable, additionalTppInfo);
        return mapToPageData(consentEntityPage);
    }

    private PageData<Collection<CmsAisAccountConsent>> mapToPageData(Page<ConsentEntity> entities) {
        return new PageData<>(entities
                                  .stream()
                                  .map(aisConsentLazyMigrationService::migrateIfNeeded)
                                  .map(this::mapToCmsAisAccountConsentWithAuthorisations)
                                  .collect(Collectors.toList()),
                              entities.getPageable().getPageNumber(),
                              entities.getPageable().getPageSize(),
                              entities.getTotalElements());
    }

    private CmsAisAccountConsent mapToCmsAisAccountConsentWithAuthorisations(ConsentEntity aisConsentEntity) {
        List<AuthorisationEntity> authorisations =
            authorisationRepository.findAllByParentExternalIdAndType(aisConsentEntity.getExternalId(), AuthorisationType.CONSENT);
        return aisConsentMapper.mapToCmsAisAccountConsent(aisConsentEntity, authorisations);
    }
}
