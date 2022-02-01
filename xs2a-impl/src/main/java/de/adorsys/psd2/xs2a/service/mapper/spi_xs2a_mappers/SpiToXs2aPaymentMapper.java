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

import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.web.mapper.ScaMethodsMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
uses = {ScaMethodsMapper.class})
public interface SpiToXs2aPaymentMapper {
    @Mapping(target = "psuMessage", source = "spi.psuMessage")
    @Mapping(target = "tppMessageInformation", source ="spi.tppMessages")
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentDataProvider", source = "aspspConsentDataProvider")
    SinglePaymentInitiationResponse mapToPaymentInitiateResponse(SpiSinglePaymentInitiationResponse spi,
                                                                 InitialSpiAspspConsentDataProvider aspspConsentDataProvider);

    @Mapping(target = "psuMessage", source = "spi.psuMessage")
    @Mapping(target = "tppMessageInformation", source ="spi.tppMessages")
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentDataProvider", source = "aspspConsentDataProvider")
    PeriodicPaymentInitiationResponse mapToPaymentInitiateResponse(SpiPeriodicPaymentInitiationResponse spi,
                                                                   InitialSpiAspspConsentDataProvider aspspConsentDataProvider);

    @Mapping(target = "psuMessage", source = "spi.psuMessage")
    @Mapping(target = "tppMessageInformation", source ="spi.tppMessages")
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentDataProvider", source = "aspspConsentDataProvider")
    BulkPaymentInitiationResponse mapToPaymentInitiateResponse(SpiBulkPaymentInitiationResponse spi,
                                                               InitialSpiAspspConsentDataProvider aspspConsentDataProvider);

    @Mapping(target = "psuMessage", source = "spi.psuMessage")
    @Mapping(target = "tppMessageInformation", source ="spi.tppMessages")
    @Mapping(target = "paymentType", source = "type")
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentDataProvider", source = "aspspConsentDataProvider")
    CommonPaymentInitiationResponse mapToCommonPaymentInitiateResponse(SpiPaymentInitiationResponse spi,
                                                                       PaymentType type, InitialSpiAspspConsentDataProvider aspspConsentDataProvider);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Xs2aAmount spiAmountToXs2aAmount(SpiAmount spiAmount);
}
