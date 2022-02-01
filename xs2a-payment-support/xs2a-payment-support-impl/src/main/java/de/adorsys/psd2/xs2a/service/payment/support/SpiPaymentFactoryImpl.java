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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.CmsToXs2aPaymentSupportMapper;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Factory class to be used to get SpiPayment from PisPayment, PaymentProduct and PaymentType
 * or concrete SpiPayment (SINGLE/PERIODIC/BULK) from PisPayment and PaymentProduct
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpiPaymentFactoryImpl implements SpiPaymentFactory {
    private final CmsToXs2aPaymentSupportMapper cmsToXs2aPaymentSupportMapper;
    private final Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    private final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    private final Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;

    /**
     * Creates Optional of SpiPayment from PisPayment, PaymentProduct and PaymentType. Should be used, when general SpiPayment type is needed.
     *
     * @param commonPaymentData {@link CommonPaymentData} object
     *
     * @return Optional of SpiPayment subclass of requested payment type or throws IllegalArgumentException for unknown payment type
     */
    @Override
    public Optional<SpiPayment> getSpiPayment(CommonPaymentData commonPaymentData) {
        PaymentType paymentType = commonPaymentData.getPaymentType();

        switch (paymentType) {
            case SINGLE:
                return createSpiSinglePayment(commonPaymentData);
            case PERIODIC:
                return createSpiPeriodicPayment(commonPaymentData);
            case BULK:
                return createSpiBulkPayment(commonPaymentData);
            default:
                log.info("Unknown payment type: [{}]", paymentType);
                throw new IllegalArgumentException("Unknown payment type");
        }
    }

    /**
     * Creates SpiSinglePayment from PisPayment and PaymentProduct. Should be used, when concrete SpiSinglePayment type is needed.
     *
     * @param commonPaymentData {@link CommonPaymentData} object
     *
     * @return Optional of SpiSinglePayment from PisPayment
     */
    private Optional<SpiPayment> createSpiSinglePayment(CommonPaymentData commonPaymentData) {
        String paymentProduct = commonPaymentData.getPaymentProduct();
        SinglePayment singlePayment = cmsToXs2aPaymentSupportMapper.mapToSinglePayment(commonPaymentData);

        if (singlePayment == null) {
            log.warn("Can't map PIS Payment with paymentProduct [{}] to SINGLE payment.", paymentProduct);
            return Optional.empty();
        }

        return Optional.ofNullable(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(singlePayment, paymentProduct));
    }

    /**
     * Creates SpiPeriodicPayment from PisPayment and PaymentProduct. Should be used, when concrete SpiPeriodicPayment type is needed.
     *
     * @param commonPaymentData {@link CommonPaymentData} object
     *
     * @return Optional of SpiPeriodicPayment from PisPayment
     */
    private Optional<SpiPayment> createSpiPeriodicPayment(CommonPaymentData commonPaymentData) {
        String paymentProduct = commonPaymentData.getPaymentProduct();
        PeriodicPayment periodicPayment = cmsToXs2aPaymentSupportMapper.mapToPeriodicPayment(commonPaymentData);

        if (periodicPayment == null) {
            log.warn("Can't map PIS Payment with paymentProduct [{}] to PERIODIC payment.", paymentProduct);
            return Optional.empty();
        }

        return Optional.ofNullable(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(periodicPayment, paymentProduct));
    }

    /**
     * Creates SpiBulkPayment from PisPayment and PaymentProduct. Should be used, when concrete SpiBulkPayment type is needed.
     *
     * @param commonPaymentData {@link CommonPaymentData} object
     *
     * @return Optional of SpiBulkPayment from PisPayment
     */
    private Optional<SpiPayment> createSpiBulkPayment(CommonPaymentData commonPaymentData) {
        String paymentProduct = commonPaymentData.getPaymentProduct();
        BulkPayment bulkPayment = cmsToXs2aPaymentSupportMapper.mapToBulkPayment(commonPaymentData);

        if (bulkPayment == null) {
            log.warn("Can't map list of PIS Payments with paymentProduct [{}] to BULK payment.", paymentProduct);
            return Optional.empty();
        }

        return Optional.ofNullable(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(bulkPayment, paymentProduct));
    }
}
