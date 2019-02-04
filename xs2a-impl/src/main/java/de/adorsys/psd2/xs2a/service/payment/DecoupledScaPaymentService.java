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

import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.springframework.stereotype.Service;

@Service
public class DecoupledScaPaymentService extends ScaPaymentService {
    public DecoupledScaPaymentService(SinglePaymentSpi singlePaymentSpi, PeriodicPaymentSpi periodicPaymentSpi, BulkPaymentSpi bulkPaymentSpi, CommonPaymentSpi commonPaymentSpi, Xs2aToSpiSinglePaymentMapper xs2AToSpiSinglePaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, Xs2aToSpiPaymentInfo xs2aToSpiPaymentInfo, SpiToXs2aPaymentMapper spiToXs2aPaymentMapper, SpiContextDataProvider spiContextDataProvider, SpiErrorMapper spiErrorMapper) {
        super(singlePaymentSpi, periodicPaymentSpi, bulkPaymentSpi, commonPaymentSpi, xs2AToSpiSinglePaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiBulkPaymentMapper, xs2aToSpiPaymentInfo, spiToXs2aPaymentMapper, spiContextDataProvider, spiErrorMapper);
    }
}
