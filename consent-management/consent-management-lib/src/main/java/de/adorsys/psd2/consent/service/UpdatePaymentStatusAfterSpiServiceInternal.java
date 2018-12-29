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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.service.UpdatePaymentStatusAfterSpiService;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdatePaymentStatusAfterSpiServiceInternal implements UpdatePaymentStatusAfterSpiService {
    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final CommonPaymentDataService commonPaymentDataService;

    @Override
    @Transactional
    public boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status) {
        Optional<List<PisPaymentData>> list = pisPaymentDataRepository.findByPaymentId(paymentId);

        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        if (list.isPresent()) {
            return updateStatusInPaymentDataList(list.get(), status);
        } else {
            Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(paymentId, null);

            return paymentDataOptional.isPresent()
                       && commonPaymentDataService.updateStatusInPaymentData(paymentDataOptional.get(), status);
        }
    }

    private boolean updateStatusInPaymentDataList(List<PisPaymentData> payments, TransactionStatus newStatus) {
        if (CollectionUtils.isEmpty(payments)) {
            return false;
        }

        if (isChangingFinaliseStatus(payments, newStatus)) {
            return false;
        }

        for (PisPaymentData pisPaymentData : payments) {
            if (pisPaymentData.getTransactionStatus().isFinalisedStatus()) {
                continue;
            }

            pisPaymentData.setTransactionStatus(newStatus);
            pisPaymentDataRepository.save(pisPaymentData);
        }

        return true;
    }

    private boolean isChangingFinaliseStatus(List<PisPaymentData> payments, TransactionStatus newStatus) {
        return payments.stream()
                   .map(PisPaymentData::getTransactionStatus)
                   .filter(TransactionStatus::isFinalisedStatus)
                   .anyMatch(t -> t != newStatus);
    }
}
