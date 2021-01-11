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
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class Xs2aEventBOMapper {

    @Autowired
    protected Xs2aObjectMapper xs2aObjectMapper;

    @Mapping(target = "XRequestId", source = "XRequestId", qualifiedByName = "mapToXRequestId")
    @Mapping(target = "internalRequestId", source = "internalRequestId", qualifiedByName = "mapToInternalRequestId")
    @Mapping(target = "payload", qualifiedByName = "mapToBytes")
    public abstract EventPO toEventPO(EventBO eventBO);

    protected byte[] mapToBytes(Object object) {
        try {
            return xs2aObjectMapper.writeValueAsBytes(object);
        } catch (IOException e) {
            log.info("Can't convert json to object: {}", e.getMessage());
            return new byte[0];
        }
    }

    @Named("mapToXRequestId")
    protected String mapToXRequestId(UUID xRequestId) {
        return xRequestId != null ? xRequestId.toString() : null;
    }

    @Named("mapToInternalRequestId")
    protected String mapToInternalRequestId(UUID internalRequestId) {
        return internalRequestId != null ? internalRequestId.toString() : null;
    }
}
