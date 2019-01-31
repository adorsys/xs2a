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

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SpiToXs2aPaymentMapper {
    private final SpiToXs2aTransactionalStatusMapper spiToXs2aTransactionalStatusMapper;

    public <T extends SpiPaymentInitiationResponse, R extends PaymentInitiationResponse> R mapToPaymentInitiateResponse(T spi, Supplier<R> xs2a, AspspConsentData aspspConsentData) {
        R response = xs2a.get();
        response.setPaymentId(spi.getPaymentId());
        response.setMultilevelScaRequired(spi.isMultilevelScaRequired());
        response.setTransactionStatus(TransactionStatus.getByValue(spi.getTransactionStatus().getName()));
        response.setAspspConsentData(aspspConsentData);
        response.setAspspAccountId(spi.getAspspAccountId());
        return response;
    }

    public CommonPaymentInitiationResponse mapToCommonPaymentInitiateResponse(SpiPaymentInitiationResponse spiResponse, PaymentType type, AspspConsentData aspspConsentData) {
        CommonPaymentInitiationResponse response = new CommonPaymentInitiationResponse();
        response.setPaymentType(type);
        response.setPaymentId(spiResponse.getPaymentId());
        response.setMultilevelScaRequired(spiResponse.isMultilevelScaRequired());
        response.setTransactionStatus(spiToXs2aTransactionalStatusMapper.mapToTransactionStatus(spiResponse.getTransactionStatus()));
        response.setAspspConsentData(aspspConsentData);
        response.setAspspAccountId(spiResponse.getAspspAccountId());
        return response;
    }
}
