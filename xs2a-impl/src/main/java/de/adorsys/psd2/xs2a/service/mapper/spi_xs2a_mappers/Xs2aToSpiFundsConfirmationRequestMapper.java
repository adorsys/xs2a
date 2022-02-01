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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiFundsConfirmationRequestMapper {
    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
    private final Xs2aToSpiAmountMapper xs2aToSpiAmountMapper;

    public SpiFundsConfirmationRequest mapToSpiFundsConfirmationRequest(FundsConfirmationRequest request) {
        SpiFundsConfirmationRequest spiRequest = new SpiFundsConfirmationRequest();
        spiRequest.setPsuAccount(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(request.getPsuAccount()));
        spiRequest.setInstructedAmount(xs2aToSpiAmountMapper.mapToSpiAmount(request.getInstructedAmount()));
        spiRequest.setCardNumber(request.getCardNumber());
        spiRequest.setPayee(request.getPayee());
        return spiRequest;
    }
}
