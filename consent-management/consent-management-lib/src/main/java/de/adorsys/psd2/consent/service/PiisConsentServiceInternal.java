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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.CmsConsentMapper;
import de.adorsys.psd2.consent.service.migration.PiisConsentLazyMigrationService;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PiisConsentServiceInternal implements PiisConsentService {
    private final ConsentJpaRepository consentJpaRepository;
    private final CmsConsentMapper cmsConsentMapper;
    private final PiisConsentEntitySpecification piisConsentEntitySpecification;
    private final PiisConsentLazyMigrationService piisConsentLazyMigrationService;

    @Override
    @Transactional
    public CmsResponse<List<CmsConsent>> getPiisConsentListByAccountIdentifier(@Nullable Currency currency, AccountReferenceSelector accountReferenceSelector) {
        Specification<ConsentEntity> specification;

        specification = currency == null
                            ? piisConsentEntitySpecification.byAccountReferenceSelector(accountReferenceSelector)
                            : piisConsentEntitySpecification.byCurrencyAndAccountReferenceSelector(currency, accountReferenceSelector);

        List<CmsConsent> consents = consentJpaRepository.findAll(specification).stream()
            .map( piisConsentLazyMigrationService::migrateIfNeeded )
            .map( consentEntity -> cmsConsentMapper.mapToCmsConsent(consentEntity, Collections.emptyList(), Collections.emptyMap()) )
            .collect(Collectors.toList());

        return CmsResponse.<List<CmsConsent>>builder()
                   .payload(consents)
                   .build();
    }
}
