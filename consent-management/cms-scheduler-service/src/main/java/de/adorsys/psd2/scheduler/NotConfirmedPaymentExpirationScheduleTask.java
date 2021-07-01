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

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.service.PisCommonPaymentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
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
public class NotConfirmedPaymentExpirationScheduleTask extends PageableSchedulerTask {
    private final PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;
    private final PisCommonPaymentDataRepository paymentDataRepository;

    @Scheduled(cron = "${xs2a.cms.not-confirmed-payment-expiration.cron.expression}")
    @Transactional
    public void obsoleteNotConfirmedPaymentIfExpired() {
        long start = System.currentTimeMillis();
        log.info("Not confirmed payment expiration schedule task is run!");

        Long totalItems = paymentDataRepository.countByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC));
        log.debug("Found {} non confirmed payment items for expiration checking", totalItems);

        execute(totalItems);
        log.info("Not confirmed payment expiration schedule task completed in {}ms!", System.currentTimeMillis() - start);
    }

    @Override
    protected void executePageable(Pageable pageable) {
        List<PisCommonPaymentData> expiredNotConfirmedPayments = paymentDataRepository.findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC), pageable)
                                                                     .stream()
                                                                     .filter(p -> !p.isSigningBasketBlocked())
                                                                     .filter(pisCommonPaymentConfirmationExpirationService::isConfirmationExpired)
                                                                     .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(expiredNotConfirmedPayments)) {
            pisCommonPaymentConfirmationExpirationService.updatePaymentDataListOnConfirmationExpiration(expiredNotConfirmedPayments);
        }
    }
}
