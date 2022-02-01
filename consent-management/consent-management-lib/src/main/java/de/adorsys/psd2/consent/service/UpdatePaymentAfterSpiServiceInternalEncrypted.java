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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiService;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UpdatePaymentAfterSpiServiceInternalEncrypted implements UpdatePaymentAfterSpiServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final UpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;

    @Override
    @Transactional
    public CmsResponse<Boolean> updatePaymentStatus(@NotNull String encryptedPaymentId, @NotNull TransactionStatus status) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID [{}]. Update payment status by id failed, couldn't decrypt payment id",
                     encryptedPaymentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return updatePaymentStatusAfterSpiService.updatePaymentStatus(decryptIdOptional.get(), status);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateInternalPaymentStatus(@NotNull String encryptedPaymentId, @NotNull InternalPaymentStatus status) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID [{}]. Update payment status by id failed, couldn't decrypt payment id",
                     encryptedPaymentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return updatePaymentStatusAfterSpiService.updateInternalPaymentStatus(decryptIdOptional.get(), status);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updatePaymentCancellationTppRedirectUri(@NotNull String encryptedPaymentId, @NotNull TppRedirectUri tppRedirectUri) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID [{}]. Update cancellation payment tpp redirect URIs by id failed, couldn't decrypt payment id",
                     encryptedPaymentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(decryptIdOptional.get(), tppRedirectUri);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updatePaymentCancellationInternalRequestId(@NotNull String encryptedPaymentId, @NotNull String internalRequestId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID [{}]. Update cancellation payment internal request ID failed, couldn't decrypt payment id",
                     encryptedPaymentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(decryptIdOptional.get(), internalRequestId);
    }
}
