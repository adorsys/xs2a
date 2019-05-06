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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AisConsentUsageService {
    private final AisConsentUsageRepository aisConsentUsageRepository;

    @Transactional
    public void incrementUsage(AisConsent aisConsent, String requestUri) {
        AisConsentUsage aisConsentUsage = getUsage(aisConsent, requestUri);
        int usage = aisConsentUsage.getUsage();
        aisConsentUsage.setUsage(++usage);
        aisConsentUsageRepository.save(aisConsentUsage);
    }

    @Transactional
    public void resetUsage(AisConsent aisConsent) {
        List<AisConsentUsage> aisConsentUsageList = aisConsentUsageRepository.findReadByConsentAndUsageDate(aisConsent, LocalDate.now());
        aisConsentUsageList.forEach(acu -> acu.setUsage(0));
        aisConsentUsageRepository.save(aisConsentUsageList);
    }

    @Transactional
    public Map<String, Integer> getUsageCounterMap(AisConsent aisConsent) {
        return aisConsentUsageRepository.findReadByConsentAndUsageDate(aisConsent, LocalDate.now())
                   .stream()
                   .collect(Collectors.toMap(AisConsentUsage::getRequestUri,
                                             u -> Math.max(aisConsent.getAllowedFrequencyPerDay() - u.getUsage(), 0)));
    }

    private AisConsentUsage getUsage(AisConsent aisConsent, String requestUri) {
        return aisConsentUsageRepository.findWriteByConsentAndUsageDateAndRequestUri(aisConsent, LocalDate.now(), requestUri)
                   .orElseGet(() -> {
                       AisConsentUsage usage = new AisConsentUsage(aisConsent, requestUri);
                       aisConsent.addUsage(usage);
                       return usage;
                   });
    }
}
