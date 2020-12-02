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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiService;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdatePaymentAfterSpiServiceInternal implements UpdatePaymentAfterSpiService {
    private final CommonPaymentDataService commonPaymentDataService;

    @Override
    @Transactional
    public CmsResponse<Boolean> updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status) {
        Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(paymentId, null);
        if (paymentDataOptional.isEmpty() || paymentDataOptional.get().isFinalised()) {
            log.info("Payment ID [{}]. Update payment status by id failed, because pis payment data not found or payment is finalized",
                     paymentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        boolean updated = commonPaymentDataService.updateStatusInPaymentData(paymentDataOptional.get(), status);
        return CmsResponse.<Boolean>builder()
                   .payload(updated)
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateInternalPaymentStatus(@NotNull String paymentId, @NotNull InternalPaymentStatus status) {
        Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(paymentId, null);
        if (paymentDataOptional.isEmpty()) {
            log.info("Payment ID [{}]. Update internal payment status by id failed, because pis payment data not found or payment is finalized",
                     paymentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        boolean updated = commonPaymentDataService.updateInternalStatusInPaymentData(paymentDataOptional.get(), status);
        return CmsResponse.<Boolean>builder()
                   .payload(updated)
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updatePaymentCancellationTppRedirectUri(@NotNull String paymentId, @NotNull TppRedirectUri tppRedirectUri) {
        Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(paymentId, null);

        if (paymentDataOptional.isEmpty() || paymentDataOptional.get().isFinalised()) {
            log.info("Payment ID [{}]. Update payment status by id failed, because pis payment data not found or payment is finalized",
                     paymentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        boolean updated = commonPaymentDataService.updateCancelTppRedirectURIs(paymentDataOptional.get(), tppRedirectUri);
        return CmsResponse.<Boolean>builder()
                   .payload(updated)
                   .build();

    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updatePaymentCancellationInternalRequestId(@NotNull String paymentId, @NotNull String internalRequestId) {
        Optional<PisCommonPaymentData> paymentDataOptional = commonPaymentDataService.getPisCommonPaymentData(paymentId, null);
        if (paymentDataOptional.isEmpty() || paymentDataOptional.get().isFinalised()) {
            log.info("Payment ID [{}]. Update payment intrenal request id failed, because pis payment data not found or payment is finalized",
                     paymentId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        boolean updated = commonPaymentDataService.updatePaymentCancellationInternalRequestId(paymentDataOptional.get(), internalRequestId);
        return CmsResponse.<Boolean>builder()
                   .payload(updated)
                   .build();
    }
}
