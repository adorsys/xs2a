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
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
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
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
        verify(cmsAspspTppService, times(1)).getTppInfo(tppAuthorisationNumber, INSTANCE_ID);
    }
}

