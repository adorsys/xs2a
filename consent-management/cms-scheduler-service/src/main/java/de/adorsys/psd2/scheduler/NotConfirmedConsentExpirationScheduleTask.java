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

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotConfirmedConsentExpirationScheduleTask extends PageableSchedulerTask {
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final ConsentJpaRepository consentJpaRepository;

    @Scheduled(cron = "${xs2a.cms.not-confirmed-consent-expiration.cron.expression}")
    @Transactional
    public void obsoleteNotConfirmedConsentIfExpired() {
        long start = System.currentTimeMillis();
        log.info("Not confirmed consent expiration schedule task is run!");

        Long totalItems = consentJpaRepository.countByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED));
        log.debug("Found {} non confirmed consent items for expiration checking", totalItems);

        execute(totalItems);
        log.info("Not confirmed consent expiration schedule task completed in {}ms!", System.currentTimeMillis() - start);
    }

    @Override
    protected void executePageable(Pageable pageable) {
        List<ConsentEntity> expiredNotConfirmedConsents = consentJpaRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED), pageable)
                                                              .stream()
                                                              .filter(aisConsentConfirmationExpirationService::isConfirmationExpired)
                                                              .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(expiredNotConfirmedConsents)) {
            aisConsentConfirmationExpirationService.updateConsentListOnConfirmationExpiration(expiredNotConfirmedConsents);
        }
    }
}
