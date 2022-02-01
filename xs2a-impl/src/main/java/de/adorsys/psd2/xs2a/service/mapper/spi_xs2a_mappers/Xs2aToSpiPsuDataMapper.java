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
