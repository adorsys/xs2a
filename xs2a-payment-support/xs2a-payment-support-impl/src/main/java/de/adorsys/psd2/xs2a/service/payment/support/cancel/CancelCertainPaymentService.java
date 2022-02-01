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
