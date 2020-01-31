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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.aspsp.api.psu.CmsAspspPsuAccountService;
import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsAspspPsuConsentManagementControllerTest {
    private final String GET_TPP_INFO_URL = "/aspsp-api/v1/psu/consent/all";
    private final String INSTANCE_ID = "UNDEFINED";
    private final String PSU_ID = "marion.mueller";
    private final String ASPSP_ACCOUNT_ID = "11111-11118";

    private MockMvc mockMvc;
    private HttpHeaders httpHeaders = new HttpHeaders();
    private PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null);

    @InjectMocks
    private CmsAspspPsuConsentManagementController cmsAspspPsuConsentManagementController;

    @Mock
    private CmsAspspPsuAccountService cmsAspspPsuAccountService;

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();
        mockMvc = MockMvcBuilders
                      .standaloneSetup(cmsAspspPsuConsentManagementController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void closeAllConsents_Success() throws Exception {
        when(cmsAspspPsuAccountService.revokeAllConsents(ASPSP_ACCOUNT_ID, psuIdData, INSTANCE_ID))
            .thenReturn(true);

        httpHeaders.add("psu-id", PSU_ID);
        httpHeaders.add("account-id", ASPSP_ACCOUNT_ID);
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("instance-id", INSTANCE_ID);

        mockMvc.perform(delete(GET_TPP_INFO_URL)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().string("true"));
    }

    @Test
    void closeAllConsents_Fail() throws Exception {
        when(cmsAspspPsuAccountService.revokeAllConsents(ASPSP_ACCOUNT_ID, psuIdData, INSTANCE_ID))
            .thenReturn(false);

        httpHeaders.add("psu-id", PSU_ID);
        httpHeaders.add("account-id", ASPSP_ACCOUNT_ID);
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("instance-id", INSTANCE_ID);

        mockMvc.perform(delete(GET_TPP_INFO_URL)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andExpect(content().string("false"));
    }

    @Test
    void closeAllConsents_EmptyData() throws Exception {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("instance-id", INSTANCE_ID);

        mockMvc.perform(delete(GET_TPP_INFO_URL)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andExpect(content().string("false"));
    }
}

