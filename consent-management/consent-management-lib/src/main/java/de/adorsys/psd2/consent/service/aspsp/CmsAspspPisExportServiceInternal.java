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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisCommonPaymentDataSpecification;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
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
public class CmsAspspPisExportServiceInternal implements CmsAspspPisExportService {
    private final PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final CmsPsuPisMapper cmsPsuPisMapper;
    private final PageRequestBuilder pageRequestBuilder;


    @Override
    public PageData<Collection<CmsPayment>> exportPaymentsByTpp(String tppAuthorisationNumber, @Nullable LocalDate createDateFrom,
                                                                @Nullable LocalDate createDateTo, @Nullable PsuIdData psuIdData,
                                                                @NotNull String instanceId, Integer pageIndex, Integer itemsPerPage) {
        if (StringUtils.isBlank(tppAuthorisationNumber) || StringUtils.isBlank(instanceId)) {
            log.info("InstanceId: [{}], TPP ID: [{}]. Export payments by TPP failed, TPP ID or instanceId is empty or null.", instanceId,
                     tppAuthorisationNumber);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        return mapToPageData(pisCommonPaymentDataRepository.findAll(
            pisCommonPaymentDataSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(tppAuthorisationNumber, createDateFrom, createDateTo, psuIdData, instanceId),
            pageRequestBuilder.getPageable(pageIndex, itemsPerPage)));
    }

    @Override
    public PageData<Collection<CmsPayment>> exportPaymentsByPsu(PsuIdData psuIdData, @Nullable LocalDate createDateFrom,
                                                                @Nullable LocalDate createDateTo, @NotNull String instanceId,
                                                                Integer pageIndex, Integer itemsPerPage) {
        if (psuIdData == null || psuIdData.isEmpty() || StringUtils.isBlank(instanceId)) {
            log.info("InstanceId: [{}]. Export payments by psu failed, psuIdData or instanceId is empty or null.",
                     instanceId);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        return mapToPageData(pisCommonPaymentDataRepository.findAll(
            pisCommonPaymentDataSpecification.byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, createDateFrom, createDateTo, instanceId),
            pageRequestBuilder.getPageable(pageIndex, itemsPerPage)));
    }

    @Override
    public PageData<Collection<CmsPayment>> exportPaymentsByAccountId(@NotNull String aspspAccountId, @Nullable LocalDate createDateFrom,
                                                                      @Nullable LocalDate createDateTo, @NotNull String instanceId,
                                                                      Integer pageIndex, Integer itemsPerPage) {
        if (StringUtils.isBlank(aspspAccountId) || StringUtils.isBlank(instanceId)) {
            log.info("InstanceId: [{}], aspspAccountId: [{}]. Export payments by accountId failed, aspspAccountId or instanceId is empty or null.",
                     instanceId, aspspAccountId);
            return new PageData<>(Collections.emptyList(), 0, itemsPerPage, 0);
        }

        return mapToPageData(pisCommonPaymentDataRepository.findAll(
            pisCommonPaymentDataSpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(aspspAccountId, createDateFrom, createDateTo, instanceId),
            pageRequestBuilder.getPageable(pageIndex, itemsPerPage)));
    }

    private PageData<Collection<CmsPayment>> mapToPageData(Page<PisCommonPaymentData> entities) {
        return new PageData<>(entities
                                  .stream()
                                  .map(cmsPsuPisMapper::mapPaymentDataToCmsPayment)
                                  .collect(Collectors.toList()),
                              entities.getPageable().getPageNumber(),
                              entities.getPageable().getPageSize(),
                              entities.getTotalElements());
    }
}
