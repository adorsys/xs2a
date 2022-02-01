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

import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
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
    public PageData<Collection<CmsBasePaymentResponse>> exportPaymentsByTpp(String tppAuthorisationNumber, @Nullable LocalDate createDateFrom,
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
    public PageData<Collection<CmsBasePaymentResponse>> exportPaymentsByPsu(PsuIdData psuIdData, @Nullable LocalDate createDateFrom,
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
    public PageData<Collection<CmsBasePaymentResponse>> exportPaymentsByAccountId(@NotNull String aspspAccountId, @Nullable LocalDate createDateFrom,
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

    private PageData<Collection<CmsBasePaymentResponse>> mapToPageData(Page<PisCommonPaymentData> entities) {
        return new PageData<>(entities
                                  .stream()
                                  .map(cmsPsuPisMapper::mapPaymentDataToCmsPayment)
                                  .collect(Collectors.toList()),
                              entities.getPageable().getPageNumber(),
                              entities.getPageable().getPageSize(),
                              entities.getTotalElements());
    }
}
