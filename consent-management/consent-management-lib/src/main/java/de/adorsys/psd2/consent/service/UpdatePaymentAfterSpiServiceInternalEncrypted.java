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
