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
