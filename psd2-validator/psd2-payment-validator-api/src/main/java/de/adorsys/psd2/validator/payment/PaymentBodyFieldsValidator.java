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

package de.adorsys.psd2.validator.payment;


import de.adorsys.psd2.xs2a.core.error.MessageError;

import javax.servlet.http.HttpServletRequest;

public interface PaymentBodyFieldsValidator {
    /**
     * Validates payment body format.
     *
     * @param request        information about incoming request
     * @param paymentService payment service for which the initiation request has been sent
     * @param messageError   error holder that contains already found errors
     * @return error container with no errors if the payment is correct,
     * container with populated errors to be displayed to the TPP otherwise
     */
    MessageError validate(HttpServletRequest request, String paymentService, MessageError messageError);
}
