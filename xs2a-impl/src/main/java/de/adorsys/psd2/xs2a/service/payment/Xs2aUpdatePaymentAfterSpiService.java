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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Xs2aUpdatePaymentAfterSpiService {
    private final UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiService;
    private final LoggingContextService loggingContextService;

    public boolean updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updatePaymentStatus(paymentId, status);
        boolean statusUpdated = response.isSuccessful() && response.getPayload();

        if (statusUpdated) {
            loggingContextService.storeTransactionStatus(status);
        }

        return statusUpdated;
    }

    public boolean updateInternalPaymentStatus(@NotNull String paymentId, @NotNull InternalPaymentStatus status) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updateInternalPaymentStatus(paymentId, status);
        return response.isSuccessful() && response.getPayload();
    }

    public boolean updatePaymentCancellationTppRedirectUri(@NotNull String paymentId, @NotNull TppRedirectUri tppRedirectUri) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(paymentId, tppRedirectUri);
        return response.isSuccessful() && response.getPayload();
    }

    public boolean updatePaymentCancellationInternalRequestId(@NotNull String paymentId, @NotNull String internalRequestId) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(paymentId, internalRequestId);
        return response.isSuccessful() && response.getPayload();
    }
}
