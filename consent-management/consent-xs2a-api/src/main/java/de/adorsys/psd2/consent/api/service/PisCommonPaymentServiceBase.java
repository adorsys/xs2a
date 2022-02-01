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
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;

import java.util.List;

/**
 * Base version of PisCommonPaymentService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see PisCommonPaymentService
 * @see PisCommonPaymentServiceEncrypted
 */
interface PisCommonPaymentServiceBase {

    CmsResponse<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request);

    /**
     * Retrieves common payment status from pis payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Information about the status of a common payment
     */
    CmsResponse<TransactionStatus> getPisCommonPaymentStatusById(String paymentId);

    /**
     * Reads full information of pis payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Response containing full information about pis payment
     */
    CmsResponse<PisCommonPaymentResponse> getCommonPaymentById(String paymentId);

    /**
     * Updates pis payment status by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @param status    new payment status
     * @return Response containing result of status changing
     */
    CmsResponse<Boolean> updateCommonPaymentStatusById(String paymentId, TransactionStatus status);

    /**
     * Updates multilevelScaRequired and stores changes into database
     *
     * @param paymentId             Payment ID
     * @param multilevelScaRequired new value for boolean multilevel sca required
     * @return true if payment was found and  updated, false otherwise.
     */
    CmsResponse<Boolean> updateMultilevelSca(String paymentId, boolean multilevelScaRequired);

    /**
     * Get information about PSU list by payment identifier
     *
     * @param paymentId String representation of the payment identifier
     * @return Response containing information about PSU
     */
    CmsResponse<List<PsuIdData>> getPsuDataListByPaymentId(String paymentId);
}
