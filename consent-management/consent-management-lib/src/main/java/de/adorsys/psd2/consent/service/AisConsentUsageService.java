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

import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
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
    public void incrementUsage(ConsentEntity consent, AisConsentActionRequest request) {
        AisConsentUsage aisConsentUsage = getUsage(consent, request.getRequestUri());
        int usage = aisConsentUsage.getUsage();
        aisConsentUsage.setUsage(++usage);
        aisConsentUsage.setResourceId(request.getResourceId());
        aisConsentUsage.setTransactionId(request.getTransactionId());
        aisConsentUsageRepository.save(aisConsentUsage);
    }

    @Transactional
    public void resetUsage(ConsentEntity consent) {
        List<AisConsentUsage> aisConsentUsageList = aisConsentUsageRepository.findReadByConsentAndUsageDate(consent, LocalDate.now());
        aisConsentUsageList.forEach(acu -> acu.setUsage(0));
        aisConsentUsageRepository.saveAll(aisConsentUsageList);
    }

    @Transactional
    public Map<String, Integer> getUsageCounterMap(ConsentEntity consent) {
        return aisConsentUsageRepository.findReadByConsentAndUsageDate(consent, LocalDate.now())
                   .stream()
                   .collect(Collectors.toMap(AisConsentUsage::getRequestUri,
                                             u -> Math.max(consent.getFrequencyPerDay() - u.getUsage(), 0)));
    }

    private AisConsentUsage getUsage(ConsentEntity consent, String requestUri) {
        return aisConsentUsageRepository.findWriteByConsentAndUsageDateAndRequestUri(consent, LocalDate.now(), requestUri)
                   .orElseGet(() -> {
                       AisConsentUsage usage = new AisConsentUsage(consent, requestUri);
                       consent.addUsage(usage);
                       return usage;
                   });
    }
}
