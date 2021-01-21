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

package de.adorsys.psd2.xs2a.service.payment.support.cancel;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.service.mapper.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.payment.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.cancel.AbstractCancelPaymentService;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CancelCertainPaymentService extends AbstractCancelPaymentService {
    private final SpiPaymentFactory spiPaymentFactory;

    @Autowired
    public CancelCertainPaymentService(CancelPaymentService cancelPaymentService, SpiPaymentFactory spiPaymentFactory) {
        super(cancelPaymentService);
        this.spiPaymentFactory = spiPaymentFactory;
    }

    @Override
    protected Optional<SpiPayment> createSpiPayment(CommonPaymentData commonPaymentData) {
        return spiPaymentFactory.getSpiPayment(commonPaymentData);
    }
}
