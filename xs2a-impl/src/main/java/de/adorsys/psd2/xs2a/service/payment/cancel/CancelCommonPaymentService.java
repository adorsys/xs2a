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

package de.adorsys.psd2.xs2a.service.payment.cancel;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.payment.CancelPaymentService;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CancelCommonPaymentService extends AbstractCancelPaymentService {

    private final Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;

    @Autowired
    public CancelCommonPaymentService(CancelPaymentService cancelPaymentService,
                                      Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper) {
        super(cancelPaymentService);
        this.xs2aToSpiPaymentInfoMapper = xs2aToSpiPaymentInfoMapper;
    }

    @Override
    protected Optional<SpiPayment> createSpiPayment(CommonPaymentData commonPaymentData) {
        return Optional.of(xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPaymentData));
    }
}
