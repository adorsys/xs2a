/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.context;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service for storing and retrieving information about the current request for further usage in logs.
 * Should be cleared manually after every request.
 */
public interface LoggingContextService {
    /**
     * Records given consent status into current context
     *
     * @param consentStatus status to be stored
     */
    void storeConsentStatus(@NotNull ConsentStatus consentStatus);

    /**
     * Retrieves consent status from current logging context as string
     *
     * @return string representation of consent status
     */
    String getConsentStatus();

    /**
     * Records given transaction status into current context
     *
     * @param transactionStatus status to be stored
     */
    void storeTransactionStatus(@NotNull TransactionStatus transactionStatus);

    /**
     * Retrieves transaction status from current logging context as string
     *
     * @return string representation of transaction status
     */
    String getTransactionStatus();

    /**
     * Records given SCA status into current context
     *
     * @param scaStatus status to be stored
     */
    void storeScaStatus(@NotNull ScaStatus scaStatus);

    /**
     * Records given transaction status and optional SCA status into current context
     *
     * @param transactionStatus transaction status to be stored
     * @param scaStatus         optional SCA status to be stored
     */
    void storeTransactionAndScaStatus(@NotNull TransactionStatus transactionStatus, @Nullable ScaStatus scaStatus);

    /**
     * Retrieves SCA status from current logging context as string
     *
     * @return string representation of SCA status
     */
    String getScaStatus();

    /**
     * Records information about the request into current logging context
     *
     * @param internalRequestId current internal request id
     * @param xRequestId        current used x-request-id
     */
    void storeRequestInformation(String internalRequestId, String xRequestId);

    /**
     * Clears current logging context.
     * <p>
     * Should be called for every request.
     */
    void clearContext();
}
