/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.logger.context;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.vop.PartyIdentificationMatchCode;
import de.adorsys.psd2.xs2a.core.vop.PartyNameMatchCode;
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
     * Records given VOP bulk status into current context
     *
     * @param vopBulkStatus status to be stored
     */
    void storeVopBulkStatus(@NotNull String vopBulkStatus);

    /**
     * Retrieves VOP bulk status from current logging context as string
     *
     * @return string representation of VOP bulk status
     */
    String getVopBulkStatus();

    /**
     * Records given VOP bulk ID into current context
     *
     * @param vopBulkId bulk ID to be stored
     */
    void storeVopBulkId(@NotNull String vopBulkId);

    /**
     * Retrieves VOP bulk ID from current logging context as string
     *
     * @return string representation of VOP bulk ID
     */
    String getVopBulkId();

    /**
     * Records VOP verification status (party ID match and party name match) into current context
     *
     * @param partyIdMatch   party ID match status to be stored
     * @param partyNameMatch party name match status to be stored
     */
    void storeVerificationStatus(PartyIdentificationMatchCode partyIdMatch, PartyNameMatchCode partyNameMatch);

    /**
     * Records bulk VOP verification status into current context
     *
     * @param vopBulkStatus the bulk verification status to be stored
     * @param partyVerificationResponses list of individual party verification responses
     */
    void storeBulkVerificationStatus(@NotNull de.adorsys.psd2.xs2a.core.vop.VopBulkStatus vopBulkStatus, 
                                      @Nullable java.util.List<de.adorsys.psd2.xs2a.core.vop.PartyVerificationResponse> partyVerificationResponses);

    /**
     * Records VOP bulk ID and status when retrieving bulk verification results
     *
     * @param vopBulkId the bulk verification ID to be stored
     * @param vopBulkStatus the bulk verification status to be stored
     */
    void storeGetBulkVopIdAndStatus(@NotNull java.util.UUID vopBulkId, @NotNull de.adorsys.psd2.xs2a.core.vop.VopBulkStatus vopBulkStatus);

    /**
     * Records VOP bulk ID and status when retrieving bulk verification status
     *
     * @param vopBulkId the bulk verification ID to be stored
     * @param vopBulkStatus the bulk verification status to be stored
     */
    void storeGetBulkVopStatusIdAndStatus(@NotNull java.util.UUID vopBulkId, @NotNull de.adorsys.psd2.xs2a.core.vop.VopBulkStatus vopBulkStatus);

    /**
     * Clears current logging context.
     * <p>
     * Should be called for every request.
     */
    void clearContext();
}
