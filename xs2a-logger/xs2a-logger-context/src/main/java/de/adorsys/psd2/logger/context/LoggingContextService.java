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

package de.adorsys.psd2.logger.context;

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
     * @param requestInfo information about the request
     */
    void storeRequestInformation(RequestInfo requestInfo);

    /**
     * Retrieves information about the request from current logging context
     *
     * @return information about the request
     */
    RequestInfo getRequestInformation();

    /**
     * Clears current logging context.
     * <p>
     * Should be called for every request.
     */
    void clearContext();
}
