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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.event.service.AspspEventService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CmsAspspEventControllerTest {

    private static final String START = "2019-07-11T11:51:00Z";
    private static final String END = "2019-07-11T20:00:00Z";
    private static final String INSTANCE_ID = "UNDEFINED";

    @Mock
    private AspspEventService aspspEventService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                      .standaloneSetup(new CmsAspspEventController(aspspEventService))
                      .build();
    }

    @Test
    public void getEventsForDates_success() throws Exception {
        when(aspspEventService.getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/aspsp-api/v1/events/")
                            .header("start-date", START)
                            .header("end-date", END)
                            .header("instance-id", INSTANCE_ID)
                            )
            .andDo(print())
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriod(eq(OffsetDateTime.parse(START)), eq(OffsetDateTime.parse(END)), eq(INSTANCE_ID));
    }

    @Test
    public void getEventsForDates_withoutInstanceId() throws Exception {
        when(aspspEventService.getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/aspsp-api/v1/events/")
                            .header("start-date", START)
                            .header("end-date", END)
        )
            .andDo(print())
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriod(eq(OffsetDateTime.parse(START)), eq(OffsetDateTime.parse(END)), eq(INSTANCE_ID));
    }
}
