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

import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.consent.api.service.PisPaymentService;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PisPaymentServiceInternal implements PisPaymentService {
    private final PisPaymentDataRepository pisPaymentDataRepository;
    private final PisConsentService pisConsentService;

    @Override
    @Transactional
    public void updatePaymentStatus(@NotNull String encryptedPaymentId, @NotNull TransactionStatus status) {
        List<PisPaymentData> list = getPaymentDataList(encryptedPaymentId)
                                        .orElse(Collections.emptyList());

        updateStatusInPaymentDataList(list, status);
    }

    private void updateStatusInPaymentDataList(List<PisPaymentData> dataList, TransactionStatus givenStatus) {
        for (PisPaymentData pisPaymentData : dataList) {
            if (!pisPaymentData.getTransactionStatus().isFinalisedStatus()) {
                pisPaymentData.setTransactionStatus(givenStatus);
                pisPaymentDataRepository.save(pisPaymentData);
            }
        }
    }

    private Optional<List<PisPaymentData>> getPaymentDataList(String encryptedPaymentId) {
        return pisConsentService.getDecryptedId(encryptedPaymentId)
                   .flatMap(pisPaymentDataRepository::findByPaymentId);
    }
}
