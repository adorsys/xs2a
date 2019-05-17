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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface SpiToXs2aPaymentMapper {

    @Mapping(target = "scaMethods", ignore = true)
    @Mapping(target = "psuMessage", ignore = true)
    @Mapping(target = "tppMessages", ignore = true)
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentData", source = "aspspConsentData")
    SinglePaymentInitiationResponse mapToPaymentInitiateResponse(SpiSinglePaymentInitiationResponse spi,
                                                                 AspspConsentData aspspConsentData);

    @Mapping(target = "scaMethods", ignore = true)
    @Mapping(target = "psuMessage", ignore = true)
    @Mapping(target = "tppMessages", ignore = true)
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentData", source = "aspspConsentData")
    PeriodicPaymentInitiationResponse mapToPaymentInitiateResponse(SpiPeriodicPaymentInitiationResponse spi,
                                                                   AspspConsentData aspspConsentData);

    @Mapping(target = "scaMethods", ignore = true)
    @Mapping(target = "psuMessage", ignore = true)
    @Mapping(target = "tppMessages", ignore = true)
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentData", source = "aspspConsentData")
    BulkPaymentInitiationResponse mapToPaymentInitiateResponse(SpiBulkPaymentInitiationResponse spi,
                                                               AspspConsentData aspspConsentData);

    @Mapping(target = "scaMethods", ignore = true)
    @Mapping(target = "psuMessage", ignore = true)
    @Mapping(target = "tppMessages", ignore = true)
    @Mapping(target = "paymentType", source = "type")
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentData", source = "aspspConsentData")
    CommonPaymentInitiationResponse mapToCommonPaymentInitiateResponse(SpiPaymentInitiationResponse spi,
                                                                       PaymentType type, AspspConsentData aspspConsentData);
}
