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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.AdditionalPsuData;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PsuDataMapper {
    public List<PsuData> mapToPsuDataList(List<PsuIdData> psuIdDataList, String instanceId) {
        return psuIdDataList.stream()
                   .map(psu -> mapToPsuData(psu, instanceId))
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    public List<PsuIdData> mapToPsuIdDataList(List<PsuData> psuIdDataList) {
        return psuIdDataList.stream()
                   .map(this::mapToPsuIdData)
                   .collect(Collectors.toList());
    }

    public PsuData mapToPsuData(PsuIdData psuIdData, String instanceId) {
        return Optional.ofNullable(psuIdData)
                   .filter(PsuIdData::isNotEmpty)
                   .map(psu -> new PsuData(
                       psu.getPsuId(),
                       psu.getPsuIdType(),
                       psu.getPsuCorporateId(),
                       psu.getPsuCorporateIdType(),
                       psu.getPsuIpAddress(),
                       mapToAdditionalPsuData(psu.getAdditionalPsuIdData())
                   ))
                   .map(psu -> setInstanceId(psu, instanceId))
                   .orElse(null);
    }

    private PsuData setInstanceId(PsuData psuData, String instanceId) {
        if (psuData != null && instanceId != null) {
            psuData.setInstanceId(instanceId);
        }
        return psuData;
    }

    public PsuIdData mapToPsuIdData(PsuData psuData) {
        return Optional.ofNullable(psuData)
                   .filter(PsuData::isNotEmpty)
                   .map(psu -> new PsuIdData(
                       psu.getPsuId(),
                       psu.getPsuIdType(),
                       psu.getPsuCorporateId(),
                       psu.getPsuCorporateIdType(),
                       psu.getPsuIpAddress(),
                       mapToAdditionalPsuIdData(psu.getAdditionalPsuData())
                   ))
                   .orElse(null);
    }

    private AdditionalPsuData mapToAdditionalPsuData(AdditionalPsuIdData additionalPsuIdData) {
        return Optional.ofNullable(additionalPsuIdData)
                   .filter(AdditionalPsuIdData::isNotEmpty)
                   .map(dta ->
                            new AdditionalPsuData()
                                .psuIpPort(dta.getPsuIpPort())
                                .psuUserAgent(dta.getPsuUserAgent())
                                .psuGeoLocation(dta.getPsuGeoLocation())
                                .psuAccept(dta.getPsuAccept())
                                .psuAcceptCharset(dta.getPsuAcceptCharset())
                                .psuAcceptEncoding(dta.getPsuAcceptEncoding())
                                .psuAcceptLanguage(dta.getPsuAcceptLanguage())
                                .psuHttpMethod(dta.getPsuHttpMethod())
                                .psuDeviceId(Optional.ofNullable(dta.getPsuDeviceId()).map(UUID::toString).orElse(null))
                   )
                   .orElse(null);
    }

    private AdditionalPsuIdData mapToAdditionalPsuIdData(AdditionalPsuData additionalPsuData) {
        return Optional.ofNullable(additionalPsuData)
                   .map(dta -> new AdditionalPsuIdData(
                            dta.getPsuIpPort(),
                            dta.getPsuUserAgent(),
                            dta.getPsuGeoLocation(),
                            dta.getPsuAccept(),
                            dta.getPsuAcceptCharset(),
                            dta.getPsuAcceptEncoding(),
                            dta.getPsuAcceptLanguage(),
                            dta.getPsuHttpMethod(),
                            Optional.ofNullable(dta.getPsuDeviceId()).map(UUID::fromString).orElse(null)
                        )
                   ).orElse(null);
    }
}
