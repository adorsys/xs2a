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

import de.adorsys.psd2.consent.aspsp.api.tpp.CmsAspspTppService;
import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsAspspStopListControllerTest {
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

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        tppStopListRecord = jsonReader.getObjectFromFile( "json/tpp-stop-list-record.json", TppStopListRecord.class);

        httpHeaders.add("instance-id", INSTANCE_ID);
        httpHeaders.add("tpp-authorisation-number", tppAuthorisationNumber);
        httpHeaders.add("lock-period", lockPeriod.toString());

        mockMvc = MockMvcBuilders.standaloneSetup(cmsAspspStopListController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void getTppStopListRecord_Success() throws Exception {
        when(cmsAspspTppService.getTppStopListRecord(tppAuthorisationNumber, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListRecord));

        mockMvc.perform(get(GET_STOP_LIST_BY_TPP)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(TPP_STOP_LIST_PATH)));

        verify(cmsAspspTppService, times(1)).getTppStopListRecord(tppAuthorisationNumber, INSTANCE_ID);
    }

    @Test
    void getTppStopListRecord_404() throws Exception {
        when(cmsAspspTppService.getTppStopListRecord(tppAuthorisationNumber, INSTANCE_ID))
            .thenReturn(Optional.empty());

        mockMvc.perform(get(GET_STOP_LIST_BY_TPP)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

        verify(cmsAspspTppService, times(1)).getTppStopListRecord(tppAuthorisationNumber, INSTANCE_ID);
    }

    @Test
    void blockTpp() throws Exception {
        Duration lockPeriodDuration = Duration.ofMillis(lockPeriod);
        when(cmsAspspTppService.blockTpp(tppAuthorisationNumber, INSTANCE_ID, lockPeriodDuration))
            .thenReturn(true);

        mockMvc.perform(put(BLOCK_TPP_AUTH_NUMBER)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().string(TRUE));

        verify(cmsAspspTppService, times(1)).blockTpp(tppAuthorisationNumber, INSTANCE_ID, lockPeriodDuration);
    }

    @Test
    void unblockTpp() throws Exception {
        when(cmsAspspTppService.unblockTpp(tppAuthorisationNumber, INSTANCE_ID))
            .thenReturn(true);

        mockMvc.perform(delete(UNBLOCK_TPP_AUTH_NUMBER)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().string(TRUE));

        verify(cmsAspspTppService, times(1)).unblockTpp(tppAuthorisationNumber, INSTANCE_ID);
    }
}
