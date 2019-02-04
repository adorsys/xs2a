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

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CmsAspspAisExportServiceInternal implements CmsAspspAisExportService {
    private final AisConsentSpecification aisConsentSpecification;
    private final AisConsentRepository aisConsentRepository;
    private final AisConsentMapper aisConsentMapper;

    @Override
    public Collection<AisAccountConsent> exportConsentsByTpp(String tppAuthorisationNumber,
                                                             @Nullable LocalDate createDateFrom,
                                                             @Nullable LocalDate createDateTo,
                                                             @Nullable PsuIdData psuIdData, @NotNull String instanceId) {
        if (StringUtils.isBlank(tppAuthorisationNumber) || StringUtils.isBlank(instanceId)) {
            return Collections.emptyList();
        }

        return aisConsentRepository.findAll(aisConsentSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(
            tppAuthorisationNumber,
            createDateFrom,
            createDateTo,
            psuIdData,
            instanceId
        ))
                   .stream()
                   .map(aisConsentMapper::mapToAisAccountConsent)
                   .collect(Collectors.toList());
    }

    @Override
    public Collection<AisAccountConsent> exportConsentsByPsu(PsuIdData psuIdData, @Nullable LocalDate createDateFrom,
                                                             @Nullable LocalDate createDateTo,
                                                             @NotNull String instanceId) {
        if (psuIdData == null || psuIdData.isEmpty() || StringUtils.isBlank(instanceId)) {
            return Collections.emptyList();
        }

        return aisConsentRepository.findAll(aisConsentSpecification.byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData,
                                                                                                              createDateFrom,
                                                                                                              createDateTo,
                                                                                                              instanceId
        ))
                   .stream()
                   .map(aisConsentMapper::mapToAisAccountConsent)
                   .collect(Collectors.toList());
    }

    @Override
    public Collection<AisAccountConsent> exportConsentsByAccountId(@NotNull String aspspAccountId,
                                                                   @Nullable LocalDate createDateFrom,
                                                                   @Nullable LocalDate createDateTo,
                                                                   @NotNull String instanceId) {

        if (StringUtils.isBlank(instanceId)) {
            return Collections.emptyList();
        }

        return aisConsentRepository.findAll(aisConsentSpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(aspspAccountId,
                                                                                                              createDateFrom,
                                                                                                              createDateTo,
                                                                                                              instanceId
        ))
                   .stream()
                   .map(aisConsentMapper::mapToAisAccountConsent)
                   .collect(Collectors.toList());
    }
}
