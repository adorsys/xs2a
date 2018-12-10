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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Service to be used to update payment status ONLY after getting SPI service result.
 * Should not be used for any other business logic purposes.
 */
public interface UpdatePaymentStatusAfterSpiService {

    /**
     * Updates a Status of Payment object by its ID and PSU ID
     *
     * @param encryptedPaymentId ID of Payment
     * @param status             Status of Payment to be set
     */
    boolean updatePaymentStatus(@NotNull String encryptedPaymentId, @NotNull TransactionStatus status);
}
