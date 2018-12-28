/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.scheduler;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotConfirmedPaymentExpirationScheduleTask {
    private final AspspProfileService aspspProfileService;
    private final PisCommonPaymentDataRepository paymentDataRepository;

    @Scheduled(cron = "not-confirmed-payment-expiration.cron.expression")
    @Transactional
    public void obsoleteNotConfirmedPaymentIfExpired() {
        log.info("Not confirmed payment expiration schedule task is run!");

        long expirationPeriodMs = aspspProfileService.getAspspSettings().getNotConfirmedPaymentExpirationPeriodMs();

        List<PisCommonPaymentData> expiredNotConfirmedPaymentDatas = paymentDataRepository.findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD))
                                                                         .stream()
                                                                         .filter(pd -> isPaymentDataExpired(pd, expirationPeriodMs))
                                                                         .collect(Collectors.toList());

        if (!expiredNotConfirmedPaymentDatas.isEmpty()) {
            paymentDataRepository.save(obsoletePaymentDatas(expiredNotConfirmedPaymentDatas));
        }
    }

    private boolean isPaymentDataExpired(PisCommonPaymentData paymentData, long expirationPeriodMs) {
        return paymentData.getCreationTimestamp().plus(expirationPeriodMs, ChronoUnit.MILLIS).isAfter(OffsetDateTime.now());
    }

    private List<PisCommonPaymentData> obsoletePaymentDatas(List<PisCommonPaymentData> expiredPaymentDatas) {
        return expiredPaymentDatas.stream()
                   .map(this::obsoletePaymentDataParameters)
                   .collect(Collectors.toList());
    }

    private PisCommonPaymentData obsoletePaymentDataParameters(PisCommonPaymentData paymentData) {
        paymentData.setTransactionStatus(TransactionStatus.RJCT);
        paymentData.getAuthorizations().forEach(auth -> auth.setScaStatus(ScaStatus.FAILED));
        return paymentData;
    }
}
