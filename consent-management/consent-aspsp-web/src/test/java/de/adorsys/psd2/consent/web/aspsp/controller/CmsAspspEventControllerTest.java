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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.service.AspspEventService;
import de.adorsys.psd2.event.service.model.AspspEvent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CmsAspspEventControllerTest {

    private static final String START = "2019-07-11T11:51:00Z";
    private static final String END = "2019-07-11T20:00:00Z";
    private static final String INSTANCE_ID = "UNDEFINED";
    private static final String CONSENT_ID = "consentId";
    private static final String PAYMENT_ID = "paymentId";
    private static final String EVENT_LIST_PATH = "json/list-aspsp-event.json";
    private static final String GET_ASPSP_EVENT_LIST_URL = "/aspsp-api/v1/events/";
    private static final String GET_ASPSP_EVENT_LIST_BY_CONSENT_ID_URL = "/aspsp-api/v1/events/consent/consentId";
    private static final String GET_ASPSP_EVENT_LIST_BY_PAYMENT_ID_URL = "/aspsp-api/v1/events/payment/paymentId";
    private static final String GET_ASPSP_EVENT_LIST_BY_EVENT_TYPE_URL = "/aspsp-api/v1/events/type/GET_SIGNING_BASKET_REQUEST_RECEIVED";
    private static final String GET_ASPSP_EVENT_LIST_BY_EVENT_ORIGIN_URL = "/aspsp-api/v1/events/origin/TPP";
    private static final String GET_ASPSP_EVENT_LIST_BY_EVENT_ORIGIN_URL_INVALID = "/aspsp-api/v1/events/origin/get";
    private static final String GET_ASPSP_EVENT_LIST_BY_EVENT_TYPE_URL_INVALID = "/aspsp-api/v1/events/type/GET";

    @Mock
    private AspspEventService aspspEventService;

    private final JsonReader jsonReader = new JsonReader();
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private MockMvc mockMvc;
    private List<AspspEvent> events;

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        AspspEvent event = jsonReader.getObjectFromFile("json/aspsp-event.json", AspspEvent.class);
        events = Collections.singletonList(event);

        httpHeaders.add("start-date", START);
        httpHeaders.add("end-date", END);

        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                      .standaloneSetup(new CmsAspspEventController(aspspEventService))
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void getEventsForDates_success() throws Exception {
        when(aspspEventService.getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID, 0, 20))
            .thenReturn(events);

        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_URL)
                            .headers(httpHeaders)
                            .header("instance-id", INSTANCE_ID))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile(EVENT_LIST_PATH)))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID, 0, 20);
    }

    @Test
    void getEventsForDatesAndConsentId_success() throws Exception {
        when(aspspEventService.getEventsForPeriodAndConsentId(OffsetDateTime.parse(START), OffsetDateTime.parse(END), CONSENT_ID, INSTANCE_ID, 0, 20))
            .thenReturn(events);

        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_BY_CONSENT_ID_URL)
                            .headers(httpHeaders)
                            .header("instance-id", INSTANCE_ID))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile(EVENT_LIST_PATH)))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriodAndConsentId(OffsetDateTime.parse(START), OffsetDateTime.parse(END), CONSENT_ID, INSTANCE_ID, 0, 20);
    }

    @Test
    void getEventsForDatesAndPaymentId_success() throws Exception {
        when(aspspEventService.getEventsForPeriodAndPaymentId(OffsetDateTime.parse(START), OffsetDateTime.parse(END), PAYMENT_ID, INSTANCE_ID, 0, 20))
            .thenReturn(events);

        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_BY_PAYMENT_ID_URL)
                            .headers(httpHeaders)
                            .header("instance-id", INSTANCE_ID))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile(EVENT_LIST_PATH)))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriodAndPaymentId(OffsetDateTime.parse(START), OffsetDateTime.parse(END), PAYMENT_ID, INSTANCE_ID, 0, 20);
    }

    @Test
    void getEventsForDatesAndEventType_success() throws Exception {
        when(aspspEventService.getEventsForPeriodAndEventType(OffsetDateTime.parse(START), OffsetDateTime.parse(END), EventType.GET_SIGNING_BASKET_REQUEST_RECEIVED, INSTANCE_ID, 0, 20))
            .thenReturn(events);

        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_BY_EVENT_TYPE_URL)
                            .headers(httpHeaders)
                            .header("instance-id", INSTANCE_ID))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile(EVENT_LIST_PATH)))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriodAndEventType(OffsetDateTime.parse(START), OffsetDateTime.parse(END), EventType.GET_SIGNING_BASKET_REQUEST_RECEIVED, INSTANCE_ID, 0, 20);
    }

    @Test
    void getEventsForDatesAndEventType_badRequest() throws Exception {
        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_BY_EVENT_TYPE_URL_INVALID)
                            .headers(httpHeaders)
                            .header("instance-id", INSTANCE_ID))
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andReturn();

        verify(aspspEventService, never()).getEventsForPeriodAndEventType(any(), any(), any(), any(), any(), any());
    }

    @Test
    void getEventsForDatesAndEventOrigin_success() throws Exception {
        when(aspspEventService.getEventsForPeriodAndEventOrigin(OffsetDateTime.parse(START), OffsetDateTime.parse(END), EventOrigin.TPP, INSTANCE_ID, 0, 20))
            .thenReturn(events);

        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_BY_EVENT_ORIGIN_URL)
                            .headers(httpHeaders)
                            .header("instance-id", INSTANCE_ID))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile(EVENT_LIST_PATH)))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriodAndEventOrigin(OffsetDateTime.parse(START), OffsetDateTime.parse(END), EventOrigin.TPP, INSTANCE_ID, 0, 20);
    }

    @Test
    void getEventsForDatesAndEventOrigin_badRequest() throws Exception {
        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_BY_EVENT_ORIGIN_URL_INVALID)
                            .headers(httpHeaders)
                            .header("instance-id", INSTANCE_ID))
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andReturn();

        verify(aspspEventService, never()).getEventsForPeriodAndEventOrigin(any(), any(), any(), any(), any(), any());
    }

    @Test
    void getEventsForDates_withoutInstanceId() throws Exception {
        when(aspspEventService.getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID, 0, 20))
            .thenReturn(events);

        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_URL)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile(EVENT_LIST_PATH)))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID, 0, 20);
    }
}
