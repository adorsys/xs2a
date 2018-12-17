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
import de.adorsys.psd2.consent.api.service.UpdatePaymentStatusAfterSpiServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UpdatePaymentStatusAfterSpiServiceInternalEncrypted implements UpdatePaymentStatusAfterSpiServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final UpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;

    @Override
    @Transactional
    public boolean updatePaymentStatus(@NotNull String encryptedPaymentId, @NotNull TransactionStatus status) {
        return securityDataService.decryptId(encryptedPaymentId)
                   .map(id -> updatePaymentStatusAfterSpiService.updatePaymentStatus(id, status))
                   .orElse(false);
    }
}
