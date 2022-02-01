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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.jetbrains.annotations.NotNull;

/**
 * Service to be used to update payment status ONLY after getting SPI service result.
 * Should not be used for any other business logic purposes.
 * <p>
 * This is base version of the service that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see UpdatePaymentAfterSpiService
 * @see UpdatePaymentAfterSpiServiceEncrypted
 */
interface UpdatePaymentAfterSpiServiceBase {

    /**
     * Updates a Status of Payment object by its ID and PSU ID
     *
     * @param paymentId ID of Payment
     * @param status    Status of Payment to be set
     * @return true if the status was updated, false otherwise
     */
    CmsResponse<Boolean> updatePaymentStatus(@NotNull String paymentId, @NotNull TransactionStatus status);

    /**
     * Updates a Status of Payment object by its ID and PSU ID
     *
     * @param paymentId ID of Payment
     * @param status    Status of Payment to be set
     * @return true if the status was updated, false otherwise
     */
    CmsResponse<Boolean> updateInternalPaymentStatus(@NotNull String paymentId, @NotNull InternalPaymentStatus status);

    /**
     * Updates a Tpp Info of Payment object by its ID
     *
     * @param paymentId      ID of Payment
     * @param tppRedirectUri Tpp redirect URIs
     * @return true if the status was updated, false otherwise
     */
    CmsResponse<Boolean> updatePaymentCancellationTppRedirectUri(@NotNull String paymentId, @NotNull TppRedirectUri tppRedirectUri);

    /**
     * Updates cancellation internal request ID of Payment object by its ID
     *
     * @param paymentId         ID of Payment
     * @param internalRequestId Internal Request ID
     * @return true if the internal request ID was updated, false otherwise
     */
    CmsResponse<Boolean> updatePaymentCancellationInternalRequestId(@NotNull String paymentId, @NotNull String internalRequestId);
}
