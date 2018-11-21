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

import de.adorsys.psd2.xs2a.service.consent.PisConsentDataService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.springframework.stereotype.Service;

@Service
public class EmbeddedScaPaymentService extends RedirectAndEmbeddedPaymentService {
    public EmbeddedScaPaymentService(SinglePaymentSpi singlePaymentSpi, PeriodicPaymentSpi periodicPaymentSpi, BulkPaymentSpi bulkPaymentSpi, Xs2aToSpiSinglePaymentMapper xs2AToSpiSinglePaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, SpiToXs2aPaymentMapper spiToXs2aPaymentMapper, Xs2aToSpiPsuDataMapper psuDataMapper, PisConsentDataService pisConsentDataService, SpiErrorMapper spiErrorMapper) {
        super(singlePaymentSpi, periodicPaymentSpi, bulkPaymentSpi, xs2AToSpiSinglePaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiBulkPaymentMapper, spiToXs2aPaymentMapper, psuDataMapper, pisConsentDataService, spiErrorMapper);
    }
}
