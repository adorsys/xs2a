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
