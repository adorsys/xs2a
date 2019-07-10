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

package de.adorsys.psd2.event.service;

import de.adorsys.psd2.event.persist.EventRepository;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.service.mapper.EventBOMapper;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aEventServiceImplTest {

    @InjectMocks
    private Xs2aEventServiceImpl xs2aEventService;

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventBOMapper mapper;

    private JsonReader jsonReader = new JsonReader();

    @Test
    public void recordEvent() {
        EventBO eventBO = jsonReader.getObjectFromFile("json/event-po.json", EventBO.class);
        EventPO eventPO = new EventPO();
        when(mapper.toEventPO(eventBO)).thenReturn(eventPO);
        when(eventRepository.save(eventPO)).thenReturn(100L);

        assertTrue(xs2aEventService.recordEvent(eventBO));

        verify(eventRepository, times(1)).save(any(EventPO.class));
    }
}
