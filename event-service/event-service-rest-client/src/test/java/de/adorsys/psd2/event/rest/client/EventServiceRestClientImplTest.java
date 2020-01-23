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
import static org.mockito.ArgumentMatchers.eq;
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
        when(consentRestTemplate.postForEntity(eq(CREATE_URL), eq(event), eq(Boolean.class))).thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(true);

        assertTrue(eventServiceRestClient.recordEvent(event));

        verify(eventRemoteUrls, times(1)).createEvent();
        verify(consentRestTemplate, times(1)).postForEntity(eq(CREATE_URL), eq(event), eq(Boolean.class));
        verify(responseEntity, times(1)).getBody();
    }
}
