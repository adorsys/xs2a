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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.service.context.LoggingContextService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Xs2aUpdatePaymentAfterSpiService {
    private final UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiService;
    private final LoggingContextService loggingContextService;

    public boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status) {
        boolean statusUpdated = updatePaymentStatusAfterSpiService.updatePaymentStatus(paymentId, status);

        if (statusUpdated) {
            loggingContextService.storeTransactionStatus(status);
        }

        return statusUpdated;
    }

    public boolean updatePaymentCancellationTppRedirectUri(@NotNull String paymentId, @NotNull TppRedirectUri tppRedirectUri) {
        return updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(paymentId, tppRedirectUri);
    }

    public boolean updatePaymentCancellationInternalRequestId(@NotNull String paymentId, @NotNull String internalRequestId) {
        return updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(paymentId, internalRequestId);
    }
}
