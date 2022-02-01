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
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AspspEventMapperImpl.class, Xs2aObjectMapper.class})
class EventBOMapperTest {

    private static final String PAYLOAD = "payload";

    @Autowired
    private AspspEventMapper mapper;

    private JsonReader jsonReader = new JsonReader();
    private Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
    private byte[] payloadAsBytes;

    @BeforeEach
    void setUp() throws Exception {
        payloadAsBytes = xs2aObjectMapper.writeValueAsBytes(PAYLOAD);
    }

    @Test
    void toAspspEvent() {
        ReportEvent reportEvent = jsonReader.getObjectFromFile("json/aspsp-event-po.json", ReportEvent.class);
        reportEvent.setPayload(payloadAsBytes);

        AspspEvent actualAspspEvent = mapper.toAspspEvent(reportEvent);

        AspspEvent expectedEventBO = jsonReader.getObjectFromFile("json/aspsp-event-bo.json", AspspEvent.class);
        assertEquals(expectedEventBO, actualAspspEvent);
    }

    @Test
    void toAspspEvent_nullValue() {
        AspspEvent actualAspspEvent = mapper.toAspspEvent(null);

        assertNull(actualAspspEvent);
    }

    @Test
    void toAspspEventList() {
        ReportEvent reportEvent = jsonReader.getObjectFromFile("json/aspsp-event-po.json", ReportEvent.class);
        reportEvent.setPayload(payloadAsBytes);

        List<AspspEvent> actualAspspEventList = mapper.toAspspEventList(Collections.singletonList(reportEvent));

        AspspEvent expectedAspspEvent = jsonReader.getObjectFromFile("json/aspsp-event-bo.json", AspspEvent.class);
        assertEquals(1, actualAspspEventList.size());
        assertEquals(expectedAspspEvent, actualAspspEventList.get(0));
    }

    @Test
    void toAspspEventBOList_nullValue() {
        List<AspspEvent> actualAspspEventList = mapper.toAspspEventList(null);
        assertNotNull(actualAspspEventList);
        assertTrue(actualAspspEventList.isEmpty());
    }

    @Test
    void mapToPduIdDataList() {
        PsuIdDataPO psuIdDataPO = jsonReader.getObjectFromFile("json/aspsp-psu-id-data.json", PsuIdDataPO.class);
        Set<PsuIdDataPO> psuIdDataPOSet = new HashSet<>();
        psuIdDataPOSet.add(psuIdDataPO);

        List<AspspPsuIdData> actual = mapper.mapToPsuIdDataList(psuIdDataPOSet);

        AspspPsuIdData expected = jsonReader.getObjectFromFile("json/aspsp-psu-id-data.json", AspspPsuIdData.class);
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    void mapToPduIdDataList_nullValue() {
        assertNull(mapper.mapToPsuIdDataList(null));
    }

    @Test
    void mapToPduIdData_nullValue() {
        assertNull(mapper.mapToPsuIdData(null));
    }

    @Test
    void mapToPayload_wrongContent() {
        assertNull(mapper.mapToPayload("not json".getBytes(StandardCharsets.UTF_8)));
    }
}
