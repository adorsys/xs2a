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
