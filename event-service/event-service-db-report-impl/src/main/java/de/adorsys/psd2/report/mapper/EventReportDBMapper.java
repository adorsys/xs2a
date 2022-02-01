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
