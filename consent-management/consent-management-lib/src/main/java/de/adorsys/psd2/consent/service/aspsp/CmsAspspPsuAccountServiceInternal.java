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

import de.adorsys.psd2.consent.aspsp.api.psu.CmsAspspPsuAccountService;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
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
    private final AisConsentSpecification aisConsentSpecification;
    private final AisConsentJpaRepository aisConsentJpaRepository;
    private final PiisConsentRepository piisConsentRepository;
    private final PiisConsentEntitySpecification piisConsentEntitySpecification;

    @Override
    @Transactional
    public boolean revokeAllConsents(@Nullable String aspspAccountId, @NotNull PsuIdData psuIdData, @Nullable String instanceId) {
        List<AisConsent> aisConsents = aisConsentJpaRepository.findAll(aisConsentSpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(aspspAccountId, psuIdData, instanceId));
        List<PiisConsentEntity> piisConsents = piisConsentRepository.findAll(piisConsentEntitySpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(aspspAccountId, psuIdData, instanceId));

        List<AisConsent> filteredAisConsents = aisConsents.stream()
                                                   .filter(cst -> !cst.getConsentStatus().isFinalisedStatus())
                                                   .collect(Collectors.toList());

        List<PiisConsentEntity> filteredPiisConsents = piisConsents.stream()
                                                           .filter(cst -> !cst.getConsentStatus().isFinalisedStatus())
                                                           .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(filteredAisConsents) &&
                CollectionUtils.isEmpty(filteredPiisConsents)) {
            return false;
        }

        filteredAisConsents.forEach(cst -> {
            cst.setLastActionDate(LocalDate.now());
            cst.setConsentStatus(ConsentStatus.REVOKED_BY_PSU);
            aisConsentJpaRepository.save(cst);
        });

        filteredPiisConsents.forEach(cst -> {
            cst.setLastActionDate(LocalDate.now());
            cst.setConsentStatus(ConsentStatus.REVOKED_BY_PSU);
            piisConsentRepository.save(cst);

        });

        return true;
    }
}
