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

import de.adorsys.psd2.consent.aspsp.api.psu.CmsAspspPsuAccountService;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.ConsentSpecification;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CmsAspspPsuAccountServiceInternal implements CmsAspspPsuAccountService {
    private final ConsentSpecification consentSpecification;
    private final ConsentJpaRepository consentJpaRepository;

    @Override
    @Transactional
    public boolean revokeAllConsents(@Nullable String aspspAccountId, @NotNull PsuIdData psuIdData, @Nullable String instanceId) {
        List<ConsentEntity> consents = consentJpaRepository
                                           .findAll(consentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(psuIdData, aspspAccountId, instanceId));

        List<ConsentEntity> filteredConsents = consents.stream()
                                                   .filter(cst -> !cst.getConsentStatus().isFinalisedStatus())
                                                   .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(filteredConsents)) {
            return false;
        }

        filteredConsents.forEach(cst -> {
            cst.setLastActionDate(LocalDate.now());
            cst.setConsentStatus(ConsentStatus.REVOKED_BY_PSU);
            consentJpaRepository.save(cst);
        });

        return true;
    }
}
