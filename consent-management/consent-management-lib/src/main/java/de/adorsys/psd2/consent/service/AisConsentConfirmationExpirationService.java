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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
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
public class AisConsentConfirmationExpirationService {
    private final AisConsentRepository aisConsentRepository;
    private final AspspProfileService aspspProfileService;

    @Transactional
    public AisConsent checkAndUpdateOnConfirmationExpiration(AisConsent consent) {
        if (isConsentConfirmationExpired(consent)) {
            log.info("Consent ID: [{}]. Consent is expired", consent.getExternalId());
            return updateConsentOnConfirmationExpiration(consent);
        }
        return consent;
    }

    public boolean isConsentConfirmationExpired(AisConsent consent) {
        long expirationPeriodMs = aspspProfileService.getAspspSettings().getNotConfirmedConsentExpirationPeriodMs();
        return consent != null && consent.isConfirmationExpired(expirationPeriodMs);
    }

    @Transactional
    public AisConsent updateConsentOnConfirmationExpiration(AisConsent consent) {
        return aisConsentRepository.save(obsoleteConsent(consent));
    }

    @Transactional
    public List<AisConsent> updateConsentListOnConfirmationExpiration(List<AisConsent> consents) {
        return IterableUtils.toList(aisConsentRepository.save(obsoleteConsentList(consents)));
    }

    private List<AisConsent> obsoleteConsentList(List<AisConsent> consents) {
        return consents.stream()
                   .map(this::obsoleteConsent)
                   .collect(Collectors.toList());
    }

    private AisConsent obsoleteConsent(AisConsent consent) {
        consent.setConsentStatus(ConsentStatus.EXPIRED);
        consent.getAuthorizations().forEach(auth -> auth.setScaStatus(ScaStatus.FAILED));
        consent.setLastActionDate(LocalDate.now());
        return consent;
    }
}
