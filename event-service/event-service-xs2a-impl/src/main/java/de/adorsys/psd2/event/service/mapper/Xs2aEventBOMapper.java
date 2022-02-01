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

    @Named("mapToBytes")
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
