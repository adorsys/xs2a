/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.report.entity.EventReportEntity;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EventReportDBMapperImpl.class})
class EventReportDBMapperTest {

    private static final byte[] PAYLOAD = "payload".getBytes();

    @Autowired
    private EventReportDBMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToReportEvent_PsuIdIsPresent() {
        EventReportEntity event = jsonReader.getObjectFromFile("json/event-entity-report.json", EventReportEntity.class);
        ReportEvent actualReportEvent = mapper.mapToReportEvent(event);

        ReportEvent expectedReportEvent = jsonReader.getObjectFromFile("json/report-event.json", ReportEvent.class);
        expectedReportEvent.setPayload(PAYLOAD);
        assertEquals(expectedReportEvent, actualReportEvent);
    }

    @Test
    void mapToReportEvent_PsuIdIsNotPresentUseEx() {
        EventReportEntity event = jsonReader.getObjectFromFile("json/event-entity-report-ex.json", EventReportEntity.class);
        ReportEvent actualReportEvent = mapper.mapToReportEvent(event);

        ReportEvent expectedReportEvent = jsonReader.getObjectFromFile("json/report-event-ex.json", ReportEvent.class);
        expectedReportEvent.setPayload(PAYLOAD);
        assertEquals(expectedReportEvent, actualReportEvent);
    }

    @Test
    void mapToReportEvent_nullValue() {
        ReportEvent actualReportEvent = mapper.mapToReportEvent(null);
        assertNull(actualReportEvent);
    }
}
