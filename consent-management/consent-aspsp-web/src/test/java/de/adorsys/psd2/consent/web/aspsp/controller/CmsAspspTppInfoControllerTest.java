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
import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
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

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsAspspTppInfoControllerTest {
    private final String tppAuthorisationNumber = "PSDDE-FAKENCA-87B2AC";
    private final String GET_TPP_INFO_URL = "/aspsp-api/v1/tpp";
    private final String INSTANCE_ID = "UNDEFINED";
    private final String TPP_INFO_PATH = "json/tpp-info.json";

    private MockMvc mockMvc;
    private JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders = new HttpHeaders();
    private TppInfo tppInfo;

    @InjectMocks
    private CmsAspspTppInfoController cmsAspspTppInfoController;

    @Mock
    private CmsAspspTppService cmsAspspTppService;

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        tppInfo = jsonReader.getObjectFromFile("json/tpp-info.json", TppInfo.class);

        httpHeaders.add("instance-id", INSTANCE_ID);
        httpHeaders.add("tpp-authorisation-number", tppAuthorisationNumber);

        mockMvc = MockMvcBuilders.standaloneSetup(cmsAspspTppInfoController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void getTppInfo_Success() throws Exception {
        when(cmsAspspTppService.getTppInfo(tppAuthorisationNumber, INSTANCE_ID))
            .thenReturn(Optional.of(tppInfo));

        mockMvc.perform(get(GET_TPP_INFO_URL)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(TPP_INFO_PATH)));

        verify(cmsAspspTppService, times(1)).getTppInfo(tppAuthorisationNumber, INSTANCE_ID);
    }

    @Test
    void getTppInfo_404() throws Exception {
        when(cmsAspspTppService.getTppInfo(tppAuthorisationNumber, INSTANCE_ID))
            .thenReturn(Optional.empty());

        mockMvc.perform(get(GET_TPP_INFO_URL)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
        verify(cmsAspspTppService, times(1)).getTppInfo(tppAuthorisationNumber, INSTANCE_ID);
    }
}

