/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.event.service;

import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class Xs2aEventServiceImplTest {

    @InjectMocks
    private Xs2aEventServiceImpl xs2aEventService;

    @Mock
    private Xs2aEventAsyncServiceImpl xs2aEventAsyncService;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void recordEvent() {
        EventBO eventBO = jsonReader.getObjectFromFile("json/event-po.json", EventBO.class);

        xs2aEventService.recordEvent(eventBO);

        verify(xs2aEventAsyncService, times(1)).recordEventAsync(any(EventBO.class));
    }
}
