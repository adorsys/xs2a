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
