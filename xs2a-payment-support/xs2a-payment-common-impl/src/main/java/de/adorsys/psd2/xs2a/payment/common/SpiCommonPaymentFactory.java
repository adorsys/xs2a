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

package de.adorsys.psd2.xs2a.payment.common;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.payment.common.mapper.CommonPaymentSupportMapper;
import de.adorsys.psd2.xs2a.service.mapper.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Factory class to be used to get SpiPayment from common payment
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpiCommonPaymentFactory implements SpiPaymentFactory {
    private final CommonPaymentSupportMapper mapper;

    /**
     * Creates Optional of SpiPayment from PisPayment, PaymentProduct and PaymentType. Should be used, when general SpiPayment type is needed.
     *
     * @param commonPaymentData {@link CommonPaymentData} object
     *
     * @return Optional of SpiPayment subclass of requested payment type or throws IllegalArgumentException for unknown payment type
     */
    @Override
    public Optional<SpiPayment> getSpiPayment(CommonPaymentData commonPaymentData) {
        return Optional.of(commonPaymentData).map(mapper::toSpiCommonPayment);
    }
}
