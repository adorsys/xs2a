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

package de.adorsys.psd2.event.rest.client;

import de.adorsys.psd2.event.service.model.EventBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceRestClientImplTest {
    private static final String CREATE_URL = "create-url";

    @InjectMocks
    private EventServiceRestClientImpl eventServiceRestClient;

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private EventRemoteUrls eventRemoteUrls;
    @Mock
    private ResponseEntity<Boolean> responseEntity;

    @Test
    void recordEvent() {
        EventBO event = EventBO.builder().build();

        when(eventRemoteUrls.createEvent()).thenReturn(CREATE_URL);
        when(consentRestTemplate.postForEntity(CREATE_URL, event, Boolean.class)).thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(true);

        assertTrue(eventServiceRestClient.recordEvent(event));

        verify(eventRemoteUrls, times(1)).createEvent();
        verify(consentRestTemplate, times(1)).postForEntity(CREATE_URL, event, Boolean.class);
        verify(responseEntity, times(1)).getBody();
    }
}
