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

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AisConsentConfirmationExpirationServiceImpl implements AisConsentConfirmationExpirationService {
    private final ConsentJpaRepository consentJpaRepository;
    private final AuthorisationRepository authorisationRepository;
    private final AspspProfileService aspspProfileService;

    @Transactional
    @Override
    public ConsentEntity checkAndUpdateOnConfirmationExpiration(ConsentEntity consent) {
        if (isConfirmationExpired(consent)) {
            log.info("Consent ID: [{}]. Consent is expired", consent.getExternalId());
            return updateOnConfirmationExpiration(consent);
        }
        return consent;
    }

    @Override
    public boolean isConfirmationExpired(ConsentEntity consent) {
        if (consent == null) {
            return false;
        }
        long expirationPeriodMs = aspspProfileService.getAspspSettings(consent.getInstanceId()).getAis().getConsentTypes().getNotConfirmedConsentExpirationTimeMs();
        return consent.isConfirmationExpired(expirationPeriodMs);
    }

    @Transactional
    @Override
    public ConsentEntity expireConsent(ConsentEntity consent) {
        LocalDate now = LocalDate.now();
        consent.setConsentStatus(ConsentStatus.EXPIRED);
        consent.setExpireDate(now);
        consent.setLastActionDate(now);
        return consentJpaRepository.save(consent);
    }

    @Transactional
    @Override
    public ConsentEntity updateOnConfirmationExpiration(ConsentEntity consent) {
        return consentJpaRepository.save(obsoleteConsent(consent));
    }

    @Transactional
    @Override
    public List<ConsentEntity> updateConsentListOnConfirmationExpiration(List<ConsentEntity> consents) {
        return IterableUtils.toList(consentJpaRepository.saveAll(obsoleteConsentList(consents)));
    }

    private List<ConsentEntity> obsoleteConsentList(List<ConsentEntity> consents) {
        return consents.stream()
                   .map(this::obsoleteConsent)
                   .collect(Collectors.toList());
    }

    private ConsentEntity obsoleteConsent(ConsentEntity consent) {
        consent.setConsentStatus(ConsentStatus.REJECTED);
        List<AuthorisationEntity> authorisations = authorisationRepository.findAllByParentExternalIdAndType(consent.getExternalId(), AuthorisationType.CONSENT);
        authorisations.forEach(auth -> auth.setScaStatus(ScaStatus.FAILED));
        authorisationRepository.saveAll(authorisations);
        consent.setLastActionDate(LocalDate.now());
        return consent;
    }
}
