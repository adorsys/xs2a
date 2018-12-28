/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.scheduler;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotConfirmedConsentExpirationScheduleTask {
    private final AspspProfileService aspspProfileService;
    private final AisConsentRepository aisConsentRepository;

    @Scheduled(cron = "not-confirmed-consent-expiration.cron.expression")
    @Transactional
    public void obsoleteNotConfirmedConsentIfExpired() {
        log.info("Not confirmed consent expiration schedule task is run!");

        long expirationPeriodMs = aspspProfileService.getAspspSettings().getNotConfirmedConsentExpirationPeriodMs();

        List<AisConsent> expiredNotConfirmedConsents = aisConsentRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED))
                                       .stream()
                                       .filter(c -> isConsentExpired(c, expirationPeriodMs))
                                       .collect(Collectors.toList());

        if (!expiredNotConfirmedConsents.isEmpty()) {
            aisConsentRepository.save(obsoleteConsents(expiredNotConfirmedConsents));
        }
    }

    private boolean isConsentExpired(AisConsent consent, long expirationPeriodMs) {
        return consent.getCreationTimestamp().plus(expirationPeriodMs, ChronoUnit.MILLIS).isAfter(OffsetDateTime.now());
    }

    private List<AisConsent> obsoleteConsents(List<AisConsent> expiredConsents) {
        return expiredConsents.stream()
            .map(this::obsoleteConsentParameters)
            .collect(Collectors.toList());
    }

    private AisConsent obsoleteConsentParameters(AisConsent consent) {
        consent.setConsentStatus(ConsentStatus.EXPIRED);
        consent.getAuthorizations().forEach(auth -> auth.setScaStatus(ScaStatus.FAILED));
        return consent;
    }
}
