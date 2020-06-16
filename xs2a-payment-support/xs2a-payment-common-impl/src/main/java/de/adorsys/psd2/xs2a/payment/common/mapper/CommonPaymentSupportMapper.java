/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
