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

import de.adorsys.psd2.consent.domain.PsuDataEmbeddable;
import de.adorsys.psd2.event.persist.model.PsuIdDataPO;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.report.entity.EventPsuDataList;
import de.adorsys.psd2.report.entity.EventReportEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EventReportDBMapper {
    @Mapping(target = "consentId", source = "event.consent.externalId")
    @Mapping(target = "paymentId", source = "event.payment.paymentId")
    ReportEvent mapToReportEvent(EventReportEntity event);

    @AfterMapping
    default void mapToReportEventAfterMappingFromEventEntity(EventReportEntity event,
                                                             @MappingTarget ReportEvent reportEvent) {
        reportEvent.setPsuIdData(getPsuIdDataPOSet(event));
    }

    default Set<PsuIdDataPO> getPsuIdDataPOSet(EventReportEntity event) {
        Set<PsuIdDataPO> psus = new HashSet<>();
        PsuDataEmbeddable psuDataEmbeddable = event.getPsuData();
        if (psuDataEmbeddable != null && psuDataEmbeddable.getPsuId() != null) {
            psus.add(mapToPsuIdDataPO(psuDataEmbeddable.getPsuId(), psuDataEmbeddable.getPsuIdType(),
                                      psuDataEmbeddable.getPsuCorporateId(), psuDataEmbeddable.getPsuCorporateIdType()));
        }

        populateByPsuIdDataPO(psus, event.getConsent());
        populateByPsuIdDataPO(psus, event.getPayment());

        return psus;
    }

    default void populateByPsuIdDataPO(Set<PsuIdDataPO> psus, EventPsuDataList eventPsuDataList) {
        if (eventPsuDataList != null && CollectionUtils.isNotEmpty(eventPsuDataList.getPsuDataList())) {
            psus.addAll(eventPsuDataList.getPsuDataList().stream()
                            .map(p -> mapToPsuIdDataPO(p.getPsuId(), p.getPsuIdType(), p.getPsuCorporateId(), p.getPsuCorporateIdType()))
                            .collect(Collectors.toList()));
        }
    }

    default PsuIdDataPO mapToPsuIdDataPO(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        PsuIdDataPO psuIdDataPO = new PsuIdDataPO();
        psuIdDataPO.setPsuId(psuId);
        psuIdDataPO.setPsuIdType(psuIdType);
        psuIdDataPO.setPsuCorporateId(psuCorporateId);
        psuIdDataPO.setPsuCorporateIdType(psuCorporateIdType);
        return psuIdDataPO;
    }

    default List<ReportEvent> mapToAspspReportEvents(List<EventReportEntity> events) {
        Collection<ReportEvent> eventCollection = events.stream()
                                                      .map(this::mapToReportEvent)
                                                      .collect(Collectors.toMap(ReportEvent::getId,
                                                                                Function.identity(),
                                                                                ReportEvent::merge))
                                                      .values();
        return new ArrayList<>(eventCollection);
    }
}
