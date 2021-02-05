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

import de.adorsys.psd2.event.persist.model.PsuIdDataPO;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.event.service.model.AspspEvent;
import de.adorsys.psd2.event.service.model.AspspPsuIdData;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class AspspEventMapper {

    @Autowired
    protected Xs2aObjectMapper xs2aObjectMapper;

    @Mapping(target = "xRequestId", source = "XRequestId", qualifiedByName = "mapToXRequestId")
    @Mapping(target = "internalRequestId", source = "internalRequestId", qualifiedByName = "mapToInternalRequestId")
    @Mapping(target = "psuIdData", source = "psuIdData", qualifiedByName = "mapToPsuIdDataList")
    @Mapping(target = "payload", qualifiedByName = "mapToPayload")
    public abstract AspspEvent toAspspEvent(ReportEvent event);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    public abstract List<AspspEvent> toAspspEventList(List<ReportEvent> events);

    @Named("mapToPsuIdDataList")
    protected abstract List<AspspPsuIdData> mapToPsuIdDataList(Set<PsuIdDataPO> psuIdData);

    @Named("mapToPayload")
    protected Object mapToPayload(byte[] array) {
        try {
            return xs2aObjectMapper.readValue(array, Object.class);
        } catch (IOException e) {
            log.info("Can't convert json to object: {}", e.getMessage());
            return null;
        }
    }

    @Named("mapToXRequestId")
    protected UUID mapToXRequestId(String xRequestId) {
        return xRequestId != null ? UUID.fromString(xRequestId) : null;
    }

    @Named("mapToInternalRequestId")
    protected UUID mapToInternalRequestId(String internalRequestId) {
        return internalRequestId != null ? UUID.fromString(internalRequestId) : null;
    }

    protected AspspPsuIdData mapToPsuIdData(PsuIdDataPO psuIdDataPO) {
        if (psuIdDataPO == null) {
            return null;
        }
        return new AspspPsuIdData(psuIdDataPO.getPsuId(),
                                  psuIdDataPO.getPsuIdType(),
                                  psuIdDataPO.getPsuCorporateId(),
                                  psuIdDataPO.getPsuCorporateIdType());
    }
}
