/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

/**
 * Factory class to be used to get SpiPayment from PisPayment, PaymentProduct and PaymentType
 * or concrete SpiPayment (SINGLE/PERIODIC/BULK) from PisPayment and PaymentProduct
 */
@Component
@RequiredArgsConstructor
public class SpiPaymentFactory {
    private final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    private final Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    private final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    private final Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;

    /**
     * Creates Optional of SpiPayment from PisPayment, PaymentProduct and PaymentType. Should be used, when general SpiPayment type is needed.
     *
     * @param pisPayment     PisPayment
     * @param paymentProduct PaymentProduct
     * @param paymentType    PaymentType
     * @return Optional of SpiPayment of requested payment type or empty Optional for unknown payment type
     */
    public Optional<SpiPayment> createSpiPaymentByPaymentType(PisPayment pisPayment, PaymentProduct paymentProduct, PaymentType paymentType) {
        switch (paymentType) {
            case SINGLE:
                return Optional.of(createSpiSinglePayment(pisPayment, paymentProduct));
            case PERIODIC:
                return Optional.of(createSpiPeriodicPayment(pisPayment, paymentProduct));
            case BULK:
                return Optional.of(createSpiBulkPayment(pisPayment, paymentProduct));
            default:
                return Optional.empty();
        }
    }

    /**
     * Creates SpiSinglePayment from PisPayment and PaymentProduct. Should be used, when concrete SpiSinglePayment type is needed.
     *
     * @param pisPayment     PisPayment
     * @param paymentProduct PaymentProduct
     * @return SpiSinglePayment from PisPayment
     */
    public SpiSinglePayment createSpiSinglePayment(PisPayment pisPayment, PaymentProduct paymentProduct) {
        SinglePayment singlePayment = cmsToXs2aPaymentMapper.mapToSinglePayment(pisPayment);
        return xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(singlePayment, paymentProduct);
    }

    /**
     * Creates SpiPeriodicPayment from PisPayment and PaymentProduct. Should be used, when concrete SpiPeriodicPayment type is needed.
     *
     * @param pisPayment     PisPayment
     * @param paymentProduct PaymentProduct
     * @return SpiPeriodicPayment from PisPayment
     */
    public SpiPeriodicPayment createSpiPeriodicPayment(PisPayment pisPayment, PaymentProduct paymentProduct) {
        PeriodicPayment periodicPayment = cmsToXs2aPaymentMapper.mapToPeriodicPayment(pisPayment);
        return xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(periodicPayment, paymentProduct);
    }

    /**
     * Creates SpiBulkPayment from PisPayment and PaymentProduct. Should be used, when concrete SpiBulkPayment type is needed.
     *
     * @param pisPayment     PisPayment
     * @param paymentProduct PaymentProduct
     * @return SpiBulkPayment from PisPayment
     */
    public SpiBulkPayment createSpiBulkPayment(PisPayment pisPayment, PaymentProduct paymentProduct) {
        BulkPayment bulkPayment = cmsToXs2aPaymentMapper.mapToBulkPayment(Collections.singletonList(pisPayment));
        return xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(bulkPayment, paymentProduct);
    }
}
