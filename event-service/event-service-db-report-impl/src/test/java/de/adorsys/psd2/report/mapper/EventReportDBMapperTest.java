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
