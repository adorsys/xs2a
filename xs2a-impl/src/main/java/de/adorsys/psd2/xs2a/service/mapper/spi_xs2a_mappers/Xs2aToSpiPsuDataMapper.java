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

import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Xs2aToSpiPsuDataMapper {
    public List<SpiPsuData> mapToSpiPsuDataList(List<PsuIdData> psuIdDataList) {
        if (psuIdDataList == null) {
            return Collections.emptyList();
        }

        return psuIdDataList.stream()
                   .map(this::mapToSpiPsuData)
                   .collect(Collectors.toList());
    }

    public SpiPsuData mapToSpiPsuData(PsuIdData psuIdData) {
        return Optional.ofNullable(psuIdData)
                   .map(this::builderWithPsuData)
                   .orElseGet(SpiPsuData::builder)
                   .build();
    }

    private SpiPsuData.SpiPsuDataBuilder builderWithPsuData(PsuIdData psuIdData) {
        SpiPsuData.SpiPsuDataBuilder builder = SpiPsuData.builder()
                                                   .psuId(psuIdData.getPsuId())
                                                   .psuIdType(psuIdData.getPsuIdType())
                                                   .psuCorporateId(psuIdData.getPsuCorporateId())
                                                   .psuCorporateIdType(psuIdData.getPsuCorporateIdType())
                                                   .psuIpAddress(psuIdData.getPsuIpAddress());

        return Optional.ofNullable(psuIdData.getAdditionalPsuIdData())
                   .map(dta -> addAdditionalPsuIdData(builder, dta))
                   .orElse(builder);
    }

    private SpiPsuData.SpiPsuDataBuilder addAdditionalPsuIdData(SpiPsuData.SpiPsuDataBuilder builder, AdditionalPsuIdData data) {
        return builder.psuIpPort(data.getPsuIpPort())
                   .psuUserAgent(data.getPsuUserAgent())
                   .psuGeoLocation(data.getPsuGeoLocation())
                   .psuAccept(data.getPsuAccept())
                   .psuAcceptCharset(data.getPsuAcceptCharset())
                   .psuAcceptEncoding(data.getPsuAcceptEncoding())
                   .psuAcceptLanguage(data.getPsuAcceptLanguage())
                   .psuHttpMethod(data.getPsuHttpMethod())
                   .psuDeviceId(data.getPsuDeviceId());
    }
}
