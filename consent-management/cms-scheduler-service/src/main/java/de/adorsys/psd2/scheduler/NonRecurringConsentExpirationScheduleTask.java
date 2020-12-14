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

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NonRecurringConsentExpirationScheduleTask {
    private final ConsentJpaRepository consentJpaRepository;

    @Scheduled(cron = "${xs2a.cms.used-non-recurring-consent-expiration.cron.expression}")
    @Transactional
    public void expireUsedNonRecurringConsent() {
        long start = System.currentTimeMillis();
        log.info("Non-recurring consent expiration task has started!");
        consentJpaRepository.expireUsedNonRecurringConsents(EnumSet.of(RECEIVED, VALID));
        log.info("Non-recurring consent expiration task completed in {}ms!", System.currentTimeMillis() - start);
    }
}
