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
