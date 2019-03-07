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

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AisConsentUsageService {
    private final AisConsentUsageRepository aisConsentUsageRepository;

    @Transactional
    public void incrementUsage(AisConsent aisConsent) {
        AisConsentUsage aisConsentUsage = getUsage(aisConsent);
        int usage = aisConsentUsage.getUsage();
        aisConsentUsage.setUsage(++usage);
        aisConsentUsageRepository.save(aisConsentUsage);
    }

    @Transactional
    public void resetUsage(AisConsent aisConsent) {
        AisConsentUsage aisConsentUsage = getUsage(aisConsent);
        aisConsentUsage.setUsage(0);
        aisConsentUsageRepository.save(aisConsentUsage);
    }

    @Transactional(readOnly = true)
    public int getUsageCounter(AisConsent aisConsent) {
        Integer usage = aisConsentUsageRepository.findReadByConsentAndUsageDate(aisConsent, LocalDate.now())
                            .map(AisConsentUsage::getUsage)
                            .orElse(0);

        return Math.max(aisConsent.getAllowedFrequencyPerDay() - usage, 0);
    }

    private AisConsentUsage getUsage(AisConsent aisConsent) {
        return aisConsentUsageRepository.findWriteByConsentAndUsageDate(aisConsent, LocalDate.now())
                   .orElseGet(() -> {
                       AisConsentUsage usage = new AisConsentUsage(aisConsent);
                       aisConsent.addUsage(usage);
                       return usage;
                   });
    }
}
