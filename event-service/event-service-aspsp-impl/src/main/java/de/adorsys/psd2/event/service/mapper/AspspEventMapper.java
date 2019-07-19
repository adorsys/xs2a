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

package de.adorsys.psd2.event.service.mapper;

import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.event.persist.model.PsuIdDataPO;
import de.adorsys.psd2.event.service.model.AspspEvent;
import de.adorsys.psd2.event.service.model.AspspPsuIdData;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class AspspEventMapper {

    @Autowired
    protected JsonConverterService jsonConverterService;

    @Mapping(target = "xRequestId", source = "XRequestId", qualifiedByName = "mapToXRequestId")
    @Mapping(target = "psuIdData", source = "psuIdData", qualifiedByName = "mapToPduIdDataSet")
    @Mapping(target = "payload", expression = "java(jsonConverterService.toObject(event.getPayload(), Object.class).orElse(null))")
    public abstract AspspEvent toAspspEvent(ReportEvent event);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    public abstract List<AspspEvent> toAspspEventList(List<ReportEvent> events);

    protected Set<AspspPsuIdData> mapToPduIdDataSet(Set<PsuIdDataPO> psuIdData) {
        if (psuIdData == null) {
            return null;
        }

        return psuIdData.stream()
                   .map(this::mapToPduIdData)
                   .collect(Collectors.toSet());
    }

    protected UUID mapToXRequestId(String xRequestId) {
        return xRequestId != null ? UUID.fromString(xRequestId) : null;
    }

    protected AspspPsuIdData mapToPduIdData(PsuIdDataPO psuIdDataPO) {
        if (psuIdDataPO == null) {
            return null;
        }
        return new AspspPsuIdData(psuIdDataPO.getPsuId(),
                                  psuIdDataPO.getPsuIdType(),
                                  psuIdDataPO.getPsuCorporateId(),
                                  psuIdDataPO.getPsuCorporateIdType());
    }
}
