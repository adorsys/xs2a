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

package de.adorsys.psd2.xs2a.service.payment.support;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
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

        return readPaymentStatusFactory.getService(ReadPaymentStatusFactory.SERVICE_PREFIX + pisCommonPaymentResponse.getPaymentType().getValue());
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
