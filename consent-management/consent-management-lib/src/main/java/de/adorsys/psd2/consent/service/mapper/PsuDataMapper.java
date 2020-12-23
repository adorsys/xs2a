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
