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

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotConfirmedConsentExpirationScheduleTask {
    private final AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    private final ConsentJpaRepository consentJpaRepository;

    @Scheduled(cron = "${xs2a.cms.not-confirmed-consent-expiration.cron.expression}")
    @Transactional
    public void obsoleteNotConfirmedConsentIfExpired() {
        long start = System.currentTimeMillis();
        log.info("Not confirmed consent expiration schedule task is run!");

        List<String> expiredNotConfirmedConsentIds = consentJpaRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED))
                                                     .stream()
                                                     .filter(c -> !c.isSigningBasketBlocked())
                                                     .filter(aisConsentConfirmationExpirationService::isConfirmationExpired)
                                                     .map(ConsentEntity::getExternalId)
                                                     .collect(Collectors.toList());
        log.info("Found {} non confirmed consent items for expiration", expiredNotConfirmedConsentIds.size());

        if (CollectionUtils.isNotEmpty(expiredNotConfirmedConsentIds)) {
            aisConsentConfirmationExpirationService.updateConsentListOnConfirmationExpirationByExternalIds(expiredNotConfirmedConsentIds);
        }

        log.info("Not confirmed consent expiration schedule task completed in {}ms!", System.currentTimeMillis() - start);
    }
}
