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

import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.event.service.model.PsuIdDataBO;
import de.adorsys.psd2.event.persist.model.PsuIdDataPO;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class EventBOMapper {

    @Autowired
    protected JsonConverterService jsonConverterService;

    @Mapping(target = "xRequestId", source = "XRequestId", qualifiedByName = "mapToXRequestId")
    @Mapping(target = "psuIdData", source = "psuIdData", qualifiedByName = "mapToPduIdData")
    @Mapping(target = "payload", expression = "java(jsonConverterService.toObject(eventPO.getPayload(), Object.class).orElse(null))")
    public abstract EventBO toEventBO(EventPO eventPO);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    public abstract List<EventBO> toEventBOList(List<EventPO> result);

    protected UUID mapToXRequestId(String xRequestId) {
        return xRequestId != null ? UUID.fromString(xRequestId) : null;
    }

    protected PsuIdDataBO mapToPduIdData(PsuIdDataPO psuIdDataPO) {
        if (psuIdDataPO == null) {
            return null;
        }
        return new PsuIdDataBO(psuIdDataPO.getPsuId(),
                               psuIdDataPO.getPsuIdType(),
                               psuIdDataPO.getPsuCorporateId(),
                               psuIdDataPO.getPsuCorporateIdType());
    }
}
