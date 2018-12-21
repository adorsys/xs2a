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

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PsuDataMapper {
    public List<PsuData> mapToPsuDataList(List<PsuIdData> psuIdDataList) {
        return psuIdDataList.stream()
                   .map(this::mapToPsuData)
                   .collect(Collectors.toList());
    }

    public List<PsuIdData> mapToPsuIdDataList(List<PsuData> psuIdDataList) {
        return psuIdDataList.stream()
                   .map(this::mapToPsuIdData)
                   .collect(Collectors.toList());
    }

    public PsuData mapToPsuData(PsuIdData psuIdData) {
        return Optional.ofNullable(psuIdData)
                   .filter(psu -> StringUtils.isNotBlank(psu.getPsuId()))
                   .map(psu -> new PsuData(
                       psu.getPsuId(),
                       psu.getPsuIdType(),
                       psu.getPsuCorporateId(),
                       psu.getPsuCorporateIdType()
                   ))
                   .orElse(null);
    }

    public PsuIdData mapToPsuIdData(PsuData psuData) {
        return Optional.ofNullable(psuData)
                   .filter(psu -> StringUtils.isNotBlank(psu.getPsuId()))
                   .map(psu ->
                            new PsuIdData(
                                psu.getPsuId(),
                                psu.getPsuIdType(),
                                psu.getPsuCorporateId(),
                                psu.getPsuCorporateIdType()
                            ))
                   .orElse(null);
    }
}
