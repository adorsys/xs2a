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
