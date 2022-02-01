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
