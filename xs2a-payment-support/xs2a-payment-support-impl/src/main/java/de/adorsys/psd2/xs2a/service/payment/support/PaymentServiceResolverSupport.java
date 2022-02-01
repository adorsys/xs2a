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

package de.adorsys.psd2.xs2a.service.payment.support;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.config.factory.Prefixes;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentFactory;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentStatusFactory;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.payment.PaymentServiceResolver;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreateCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreatePaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadCommonPaymentStatusService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadPaymentStatusService;
import de.adorsys.psd2.xs2a.service.payment.support.cancel.CancelCertainPaymentService;
import de.adorsys.psd2.xs2a.service.payment.support.create.CreateBulkPaymentService;
import de.adorsys.psd2.xs2a.service.payment.support.create.CreatePeriodicPaymentService;
import de.adorsys.psd2.xs2a.service.payment.support.create.CreateSinglePaymentService;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentServiceResolverSupport implements PaymentServiceResolver {
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;

    private final CreateCommonPaymentService createCommonPaymentService;
    private final CreateSinglePaymentService createSinglePaymentService;
    private final CreatePeriodicPaymentService createPeriodicPaymentService;
    private final CreateBulkPaymentService createBulkPaymentService;

    private final ReadCommonPaymentService readCommonPaymentService;
    private final ReadPaymentFactory readPaymentFactory;

    private final ReadCommonPaymentStatusService readCommonPaymentStatusService;
    private final ReadPaymentStatusFactory readPaymentStatusFactory;

    private final CancelCommonPaymentService cancelCommonPaymentService;
    private final CancelCertainPaymentService cancelCertainPaymentService;

    /**
     * Returns definite service for payment creation depending on the payment initiation parameters.
     *
     * @param paymentInitiationParameters {@link PaymentInitiationParameters} object
     * @return definite implementation of {@link CreatePaymentService}
     */
    @Override
    public CreatePaymentService getCreatePaymentService(PaymentInitiationParameters paymentInitiationParameters) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())) {
            return createCommonPaymentService;
        }

        if (PaymentType.SINGLE == paymentInitiationParameters.getPaymentType()) {
            return createSinglePaymentService;
        } else if (PaymentType.PERIODIC == paymentInitiationParameters.getPaymentType()) {
            return createPeriodicPaymentService;
        } else {
            return createBulkPaymentService;
        }
    }

    /**
     * Returns definite service for getting payment details depending on the input payment data.
     *
     * @param commonPaymentData {@link CommonPaymentData} object
     * @return definite implementation of {@link ReadPaymentService}
     */
    @Override
    public ReadPaymentService getReadPaymentService(CommonPaymentData commonPaymentData) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(commonPaymentData.getPaymentProduct())) {
            return readCommonPaymentService;
        }

        return readPaymentFactory.getService(commonPaymentData.getPaymentType().getValue());
    }

    /**
     * Returns definite service for getting payment status depending on the input payment data.
     *
     * @param pisCommonPaymentResponse {@link PisCommonPaymentResponse} object
     * @return definite implementation of {@link ReadPaymentStatusService}
     */
    @Override
    public ReadPaymentStatusService getReadPaymentStatusService(PisCommonPaymentResponse pisCommonPaymentResponse) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(pisCommonPaymentResponse.getPaymentProduct())) {
            return readCommonPaymentStatusService;
        }

        return readPaymentStatusFactory.getService(Prefixes.SERVICE_PREFIX.getValue() + pisCommonPaymentResponse.getPaymentType().getValue());
    }

    /**
     * Returns definite service for payment cancellation depending on the input payment cancellation data.
     *
     * @param paymentCancellationRequest {@link PisPaymentCancellationRequest} object
     * @return definite implementation of {@link CancelPaymentService}
     */
    @Override
    public CancelPaymentService getCancelPaymentService(PisPaymentCancellationRequest paymentCancellationRequest) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(paymentCancellationRequest.getPaymentProduct())) {
            return cancelCommonPaymentService;
        }

        return cancelCertainPaymentService;
    }
}
