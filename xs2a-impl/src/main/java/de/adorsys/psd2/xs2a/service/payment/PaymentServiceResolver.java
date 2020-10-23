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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreatePaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadPaymentStatusService;

public interface PaymentServiceResolver {
    /**
     * Returns definite service for payment creation depending on the payment initiation parameters.
     *
     * @param paymentInitiationParameters {@link PaymentInitiationParameters} object
     * @return definite implementation of {@link CreatePaymentService}
     */
    CreatePaymentService getCreatePaymentService(PaymentInitiationParameters paymentInitiationParameters);

    /**
     * Returns definite service for getting payment details depending on the input payment data.
     *
     * @param commonPaymentData {@link CommonPaymentData} object
     * @return definite implementation of {@link ReadPaymentService}
     */
    ReadPaymentService getReadPaymentService(CommonPaymentData commonPaymentData);

    ReadPaymentStatusService getReadPaymentStatusService(PisCommonPaymentResponse pisCommonPaymentResponse);

    /**
     * Returns definite service for payment cancellation depending on the input payment cancellation data.
     *
     * @param paymentCancellationRequest {@link PisPaymentCancellationRequest} object
     * @return definite implementation of {@link CancelPaymentService}
     */
    CancelPaymentService getCancelPaymentService(PisPaymentCancellationRequest paymentCancellationRequest);
}
