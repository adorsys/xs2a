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
                                           .findAll(consentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(psuIdData, aspspAccountId, instanceId))
                                           .stream()
                                           .distinct()
                                           .collect(Collectors.toList());

        List<ConsentEntity> filteredConsents = consents.stream()
                                                   .filter(cst -> !cst.getConsentStatus().isFinalisedStatus())
                                                   .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(filteredConsents)) {
            return false;
        }

        filteredConsents.forEach(cst -> {
            cst.setLastActionDate(LocalDate.now());
            cst.setConsentStatus(ConsentStatus.REVOKED_BY_PSU);
        });

        return true;
    }
}
