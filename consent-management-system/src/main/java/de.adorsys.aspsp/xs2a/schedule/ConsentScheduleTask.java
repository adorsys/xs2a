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

package de.adorsys.aspsp.xs2a.schedule;

import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.aspsp.xs2a.repository.AisConsentRepository;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsentScheduleTask {
    private final AisConsentRepository aisConsentRepository;
    private final AspspProfileService profileService;

    @Scheduled(cron = "${consent.cron.expression}")
    public void checkConsentStatus() {
        log.info("Consent schedule task is run!");

        List<AisConsent> availableConsents = Optional.ofNullable(aisConsentRepository.findByConsentStatusIn(EnumSet.of(SpiConsentStatus.RECEIVED, SpiConsentStatus.VALID)))
                                                 .orElse(Collections.emptyList());
        aisConsentRepository.save(updateConsent(availableConsents));
    }

    private List<AisConsent> updateConsent(List<AisConsent> availableConsents) {
        return availableConsents.stream()
                   .map(this::updateConsentParameters)
                   .collect(Collectors.toList());
    }

    private AisConsent updateConsentParameters(AisConsent consent) {
        int minFrequencyPerDay = profileService.getMinFrequencyPerDay(consent.getTppFrequencyPerDay());
        consent.setExpectedFrequencyPerDay(minFrequencyPerDay);
        consent.setUsageCounter(minFrequencyPerDay);
        consent.setConsentStatus(updateConsentStatus(consent));
        return consent;
    }

    private SpiConsentStatus updateConsentStatus(AisConsent consent) {
        return LocalDateTime.now().isAfter(consent.getExpireDate())
                   ? SpiConsentStatus.EXPIRED
                   : consent.getConsentStatus();
    }
}
