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

package de.adorsys.psd2.event.persist.mapper;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.entity.EventEntity;
import de.adorsys.psd2.event.persist.entity.EventEntityForReport;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.persist.model.PsuIdDataPO;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface EventDBMapper {

    @Mapping(target = "psuData", source = "psuIdData")
    @Mapping(target = "instanceId", defaultValue = "UNDEFINED")
    EventEntity toEventEntity(EventPO eventPO);

    static ReportEvent mapToReportEvent(EventEntityForReport event) {
        ReportEvent reportEvent = new ReportEvent();
        reportEvent.setId(event.getId());
        reportEvent.setTimestamp(event.getTimestamp());
        reportEvent.setConsentId(event.getConsentId());
        reportEvent.setPaymentId(event.getPaymentId());
        reportEvent.setPayload(event.getPayload().getBytes());
        reportEvent.setEventOrigin(EventOrigin.valueOf(event.getEventOrigin()));
        reportEvent.setEventType(EventType.valueOf(event.getEventType()));
        reportEvent.setInstanceId(event.getInstanceId());
        reportEvent.setTppAuthorisationNumber(event.getTppAuthorisationNumber());
        reportEvent.setXRequestId(event.getXRequestId());
        reportEvent.setPsuIdData(getPsuIdDataPOSet(event));

        return reportEvent;
    }

    static Set<PsuIdDataPO> getPsuIdDataPOSet(EventEntityForReport event) {
        Set<PsuIdDataPO> psus = null;

        if (StringUtils.isNotBlank(event.getPsuId())) {
            PsuIdDataPO psuIdDataPO = mapToPsuIdDataPO(
                event.getPsuId(),
                event.getPsuIdType(),
                event.getPsuCorporateId(),
                event.getPsuCorporateIdType());

            psus = new HashSet<>();
            psus.add(psuIdDataPO);
        } else if (StringUtils.isNotBlank(event.getPsuExId())) {
            PsuIdDataPO psuIdDataPO = mapToPsuIdDataPO(
                event.getPsuExId(),
                event.getPsuExIdType(),
                event.getPsuExCorporateId(),
                event.getPsuExCorporateIdType());

            psus = new HashSet<>();
            psus.add(psuIdDataPO);
        }

        return psus;
    }

    static PsuIdDataPO mapToPsuIdDataPO(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        PsuIdDataPO psuIdDataPO = new PsuIdDataPO();
        psuIdDataPO.setPsuId(psuId);
        psuIdDataPO.setPsuIdType(psuIdType);
        psuIdDataPO.setPsuCorporateId(psuCorporateId);
        psuIdDataPO.setPsuCorporateIdType(psuCorporateIdType);
        return psuIdDataPO;
    }
}
