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
