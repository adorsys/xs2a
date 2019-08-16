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

package de.adorsys.psd2.report.mapper;

import de.adorsys.psd2.event.persist.model.PsuIdDataPO;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.report.entity.EventEntityForReport;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EventReportDBMapper {

    ReportEvent mapToReportEvent(EventEntityForReport event);

    @AfterMapping
    default void mapToReportEventAfterMapping(EventEntityForReport event,
                                              @MappingTarget ReportEvent reportEvent) {
        reportEvent.setPsuIdData(getPsuIdDataPOSet(event));
    }

    default Set<PsuIdDataPO> getPsuIdDataPOSet(EventEntityForReport event) {
        Set<PsuIdDataPO> psus = new HashSet<>();
        if (StringUtils.isNotBlank(event.getPsuId())) {
            psus.add(mapToPsuIdDataPO(event.getPsuId(), event.getPsuIdType(), event.getPsuCorporateId(), event.getPsuCorporateIdType()));
        } else if (StringUtils.isNotBlank(event.getPsuExId())) {
            psus.add(mapToPsuIdDataPO(event.getPsuExId(), event.getPsuExIdType(), event.getPsuExCorporateId(), event.getPsuExCorporateIdType()));
        }
        return psus;
    }

    default PsuIdDataPO mapToPsuIdDataPO(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        PsuIdDataPO psuIdDataPO = new PsuIdDataPO();
        psuIdDataPO.setPsuId(psuId);
        psuIdDataPO.setPsuIdType(psuIdType);
        psuIdDataPO.setPsuCorporateId(psuCorporateId);
        psuIdDataPO.setPsuCorporateIdType(psuCorporateIdType);
        return psuIdDataPO;
    }

    default List<ReportEvent> mapToAspspReportEvents(List<EventEntityForReport> events) {
        Collection<ReportEvent> eventCollection = events.stream()
                                                      .map(this::mapToReportEvent)
                                                      .collect(Collectors.toMap(ReportEvent::getId,
                                                                                Function.identity(),
                                                                                ReportEvent::merge))
                                                      .values();
        return new ArrayList<>(eventCollection);
    }
}
