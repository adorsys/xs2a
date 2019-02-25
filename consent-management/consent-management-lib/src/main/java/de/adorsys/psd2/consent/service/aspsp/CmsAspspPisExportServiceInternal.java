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
import de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisCommonPaymentDataSpecification;
import de.adorsys.psd2.consent.service.mapper.CmsPsuPisMapper;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsAspspPisExportServiceInternal implements CmsAspspPisExportService {
    private final PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final CmsPsuPisMapper cmsPsuPisMapper;


    @Override
    public Collection<CmsPayment> exportPaymentsByTpp(String tppAuthorisationNumber, @Nullable LocalDate createDateFrom, @Nullable LocalDate createDateTo, @Nullable PsuIdData psuIdData, @NotNull String instanceId) {
        if (StringUtils.isBlank(tppAuthorisationNumber) || StringUtils.isBlank(instanceId)) {
            log.info("CmsAspspPisExportServiceInternal.exportPaymentsByTpp failed, tppAuthorisationNumber '{}' or instanceId '{}' is empty or null.", tppAuthorisationNumber,
                     instanceId);
            return Collections.emptyList();
        }

        List<PisCommonPaymentData> commonPayments = pisCommonPaymentDataRepository.findAll(pisCommonPaymentDataSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(tppAuthorisationNumber, createDateFrom, createDateTo, psuIdData, instanceId));
        return cmsPsuPisMapper.mapPaymentDataToCmsPayments(commonPayments);
    }

    @Override
    public Collection<CmsPayment> exportPaymentsByPsu(PsuIdData psuIdData, @Nullable LocalDate createDateFrom, @Nullable LocalDate createDateTo, @NotNull String instanceId) {
        if (psuIdData == null || psuIdData.isEmpty() || StringUtils.isBlank(instanceId)) {
            log.info("CmsAspspPisExportServiceInternal.exportPaymentsByPsu failed, psuIdData or instanceId '{}'is empty or null.",
                     instanceId);
            return Collections.emptyList();
        }

        List<PisCommonPaymentData> commonPayments = pisCommonPaymentDataRepository.findAll(pisCommonPaymentDataSpecification.byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, createDateFrom, createDateTo, instanceId));
        return cmsPsuPisMapper.mapPaymentDataToCmsPayments(commonPayments);
    }

    @Override
    public Collection<CmsPayment> exportPaymentsByAccountId(@NotNull String aspspAccountId, @Nullable LocalDate createDateFrom, @Nullable LocalDate createDateTo, @NotNull String instanceId) {
        if (StringUtils.isBlank(aspspAccountId) || StringUtils.isBlank(instanceId)) {
            log.info("CmsAspspPisExportServiceInternal.exportPaymentsByAccountId failed, aspspAccountId [{}] or instanceId [{}] is empty or null.",
                     aspspAccountId, instanceId);
            return Collections.emptyList();
        }

        List<PisCommonPaymentData> commonPayments = pisCommonPaymentDataRepository.findAll(pisCommonPaymentDataSpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(aspspAccountId, createDateFrom, createDateTo, instanceId));
        return cmsPsuPisMapper.mapPaymentDataToCmsPayments(commonPayments);
    }
}
