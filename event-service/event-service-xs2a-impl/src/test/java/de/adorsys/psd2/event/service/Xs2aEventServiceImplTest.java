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

package de.adorsys.psd2.event.service;

import de.adorsys.psd2.event.persist.EventRepository;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.service.mapper.Xs2aEventBOMapper;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aEventServiceImplTest {

    @InjectMocks
    private Xs2aEventServiceImpl xs2aEventService;

    @Mock
    private EventRepository eventRepository;
    @Mock
    private Xs2aEventBOMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void recordEvent() {
        EventBO eventBO = jsonReader.getObjectFromFile("json/event-po.json", EventBO.class);
        EventPO eventPO = new EventPO();
        when(mapper.toEventPO(eventBO)).thenReturn(eventPO);
        when(eventRepository.save(eventPO)).thenReturn(100L);

        assertTrue(xs2aEventService.recordEvent(eventBO));

        verify(eventRepository, times(1)).save(any(EventPO.class));
    }
}
