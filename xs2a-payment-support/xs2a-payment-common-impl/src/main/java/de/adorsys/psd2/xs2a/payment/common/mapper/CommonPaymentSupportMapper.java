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

package de.adorsys.psd2.xs2a.payment.common.mapper;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiCommonPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommonPaymentSupportMapper {
    @Mapping(target = "paymentId", source = "externalId")
    @Mapping(target = "paymentStatus", source = "transactionStatus")
    @Mapping(target = "psuDataList", expression = "java(toSpiPsuDataList(commonPaymentData.getPsuData()))")
    SpiCommonPayment toSpiCommonPayment(CommonPaymentData commonPaymentData);

    @Mapping(target = "psuIpPort", source = "additionalPsuIdData.psuIpPort")
    @Mapping(target = "psuUserAgent", source = "additionalPsuIdData.psuUserAgent")
    @Mapping(target = "psuGeoLocation", source = "additionalPsuIdData.psuGeoLocation")
    @Mapping(target = "psuAccept", source = "additionalPsuIdData.psuAccept")
    @Mapping(target = "psuAcceptCharset", source = "additionalPsuIdData.psuAcceptCharset")
    @Mapping(target = "psuAcceptEncoding", source = "additionalPsuIdData.psuAcceptEncoding")
    @Mapping(target = "psuAcceptLanguage", source = "additionalPsuIdData.psuAcceptLanguage")
    @Mapping(target = "psuHttpMethod", source = "additionalPsuIdData.psuHttpMethod")
    @Mapping(target = "psuDeviceId", source = "additionalPsuIdData.psuDeviceId")
    SpiPsuData toSpiPsuData(PsuIdData psuIdData);

    List<SpiPsuData> toSpiPsuDataList(List<PsuIdData> psuIdData);
}
