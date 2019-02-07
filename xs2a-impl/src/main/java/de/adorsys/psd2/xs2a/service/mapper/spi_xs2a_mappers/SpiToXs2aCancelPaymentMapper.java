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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpiToXs2aCancelPaymentMapper {

    public CancelPaymentResponse mapToCancelPaymentResponse(SpiPaymentCancellationResponse spiCancelPayment) {
        return Optional.ofNullable(spiCancelPayment)
                   .map(c -> {
                       CancelPaymentResponse response = new CancelPaymentResponse();
                       response.setStartAuthorisationRequired(c.isCancellationAuthorisationMandated());
                       response.setTransactionStatus(c.getTransactionStatus());
                       return response;
                   }).orElse(null);
    }
}
