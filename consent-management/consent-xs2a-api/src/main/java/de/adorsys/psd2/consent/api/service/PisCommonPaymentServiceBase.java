/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
