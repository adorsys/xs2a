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

import de.adorsys.psd2.consent.aspsp.api.tpp.CmsAspspTppService;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspStopListControllerTest {
    private final String tppAuthorisationNumber = "PSDDE-FAKENCA-87B2AC";
    private final String GET_STOP_LIST_BY_TPP = "/aspsp-api/v1/tpp/stop-list";
    private final String BLOCK_TPP_AUTH_NUMBER = "/aspsp-api/v1/tpp/stop-list/block";
    private final String UNBLOCK_TPP_AUTH_NUMBER = "/aspsp-api/v1/tpp/stop-list/unblock";
    private final String INSTANCE_ID = "UNDEFINED";
    private final String TPP_STOP_LIST_PATH = "json/tpp-stop-list-record.json";
    private final Long lockPeriod = 1000L;
    private final String TRUE = "true";

    @InjectMocks
    private CmsAspspStopListController cmsAspspStopListController;

    @Mock
    private CmsAspspTppService cmsAspspTppService;

    private MockMvc mockMvc;
    private JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders = new HttpHeaders();
    private TppStopListRecord tppStopListRecord;

    @Before
    public void setUp() {
        tppStopListRecord = jsonReader.getObjectFromFile( "json/tpp-stop-list-record.json", TppStopListRecord.class);

        httpHeaders.add("instance-id", INSTANCE_ID);
        httpHeaders.add("tpp-authorisation-number", tppAuthorisationNumber);
        httpHeaders.add("lock-period", lockPeriod.toString());

        mockMvc = MockMvcBuilders.standaloneSetup(cmsAspspStopListController).build();
    }

    @Test
    public void getTppStopListRecord_Success() throws Exception {
        when(cmsAspspTppService.getTppStopListRecord(tppAuthorisationNumber, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListRecord));

        mockMvc.perform(get(GET_STOP_LIST_BY_TPP)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(TPP_STOP_LIST_PATH)));

        verify(cmsAspspTppService, times(1)).getTppStopListRecord(tppAuthorisationNumber, INSTANCE_ID);
    }

    @Test
    public void getTppStopListRecord_404() throws Exception {
        when(cmsAspspTppService.getTppStopListRecord(tppAuthorisationNumber, INSTANCE_ID))
            .thenReturn(Optional.empty());

        mockMvc.perform(get(GET_STOP_LIST_BY_TPP)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

        verify(cmsAspspTppService, times(1)).getTppStopListRecord(tppAuthorisationNumber, INSTANCE_ID);
    }

    @Test
    public void blockTpp() throws Exception {
        Duration lockPeriodDuration = Duration.ofMillis(lockPeriod);
        when(cmsAspspTppService.blockTpp(tppAuthorisationNumber, INSTANCE_ID, lockPeriodDuration))
            .thenReturn(true);

        mockMvc.perform(put(BLOCK_TPP_AUTH_NUMBER)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().string(TRUE));

        verify(cmsAspspTppService, times(1)).blockTpp(tppAuthorisationNumber, INSTANCE_ID, lockPeriodDuration);
    }

    @Test
    public void unblockTpp() throws Exception {
        when(cmsAspspTppService.unblockTpp(tppAuthorisationNumber, INSTANCE_ID))
            .thenReturn(true);

        mockMvc.perform(delete(UNBLOCK_TPP_AUTH_NUMBER)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().string(TRUE));

        verify(cmsAspspTppService, times(1)).unblockTpp(tppAuthorisationNumber, INSTANCE_ID);
    }
}
