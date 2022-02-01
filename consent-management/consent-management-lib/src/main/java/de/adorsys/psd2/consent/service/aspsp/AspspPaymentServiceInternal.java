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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.aspsp.api.pis.AspspPaymentService;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.service.CommonPaymentDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AspspPaymentServiceInternal implements AspspPaymentService {

    private final CommonPaymentDataService commonPaymentDataService;

    @Override
    @Transactional
    public boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status, @NotNull String instanceId) {
        Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(paymentId, instanceId);

        return paymentDataOptional
                   .map(pd -> commonPaymentDataService.updateStatusInPaymentData(pd, status))
                   .orElseGet(() -> {
                       log.info("Payment ID [{}], Instance ID: [{}]. Update payment status failed, because PIS common payment data not found",
                                paymentId, instanceId);
                       return false;
                   });
    }
}
