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

import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService;
import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsAspspAisExportControllerTest {
    private final String PSU_ID = "marion.mueller";
    private final String TPP_ID = "PSDDE-FAKENCA-87B2AC";
    private final String ACCOUNT_ID = "account_id";
    private final String EXPORT_AIS_CONSENT_BY_TPP = "/aspsp-api/v1/ais/consents/tpp/PSDDE-FAKENCA-87B2AC";
    private final String EXPORT_AIS_CONSENT_BY_PSU = "/aspsp-api/v1/ais/consents/psu/";
    private final String EXPORT_AIS_CONSENT_BY_ACCOUNT = "/aspsp-api/v1/ais/consents/account/account_id";
    private final LocalDate START_DATE = LocalDate.of(2019, 2, 25);
    private final LocalDate END_DATE = LocalDate.of(2020, 7, 22);
    private final String INSTANCE_ID = "UNDEFINED";
    private final String LIST_OF_AIS_ACCOUNT_CONSENT_PATH = "json/ais/list-ais-account-consent.json";

    private MockMvc mockMvc;
    private JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders = new HttpHeaders();
    private PsuIdData psuIdData;
    private Collection<CmsAisAccountConsent> consents;

    @InjectMocks
    private CmsAspspAisExportController cmsAspspAisExportController;

    @Mock
    private CmsAspspAisExportService cmsAspspAisExportService;

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
        CmsAisAccountConsent aisAccountConsent = jsonReader.getObjectFromFile("json/ais/ais-account-consent.json", CmsAisAccountConsent.class);
        consents = Collections.singletonList(aisAccountConsent);

        httpHeaders.add("psu-id", PSU_ID);
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Start-Date", START_DATE.toString());
        httpHeaders.add("End-Date", END_DATE.toString());
        httpHeaders.add("instance-id", INSTANCE_ID);

        mockMvc = MockMvcBuilders
                      .standaloneSetup(cmsAspspAisExportController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void getConsentsByTpp_Success() throws Exception {
        when(cmsAspspAisExportService.exportConsentsByTpp(TPP_ID, START_DATE, END_DATE, psuIdData, INSTANCE_ID, null))
            .thenReturn(consents);

        mockMvc.perform(get(EXPORT_AIS_CONSENT_BY_TPP)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_AIS_ACCOUNT_CONSENT_PATH)));

        verify(cmsAspspAisExportService, times(1)).exportConsentsByTpp(TPP_ID, START_DATE, END_DATE, psuIdData, INSTANCE_ID, null);
    }

    @Test
    void getConsentsByPsu_Success() throws Exception {
        when(cmsAspspAisExportService.exportConsentsByPsuAndAdditionalTppInfo(psuIdData, START_DATE, END_DATE, INSTANCE_ID, null))
            .thenReturn(consents);

        mockMvc.perform(get(EXPORT_AIS_CONSENT_BY_PSU)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_AIS_ACCOUNT_CONSENT_PATH)));

        verify(cmsAspspAisExportService, times(1)).exportConsentsByPsuAndAdditionalTppInfo(psuIdData, START_DATE, END_DATE, INSTANCE_ID, null);
    }

    @Test
    void getConsentsByAccount_Success() throws Exception {
        when(cmsAspspAisExportService.exportConsentsByAccountIdAndAdditionalTppInfo(ACCOUNT_ID, START_DATE, END_DATE, INSTANCE_ID, null))
            .thenReturn(consents);

        mockMvc.perform(get(EXPORT_AIS_CONSENT_BY_ACCOUNT)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_AIS_ACCOUNT_CONSENT_PATH)));

        verify(cmsAspspAisExportService, times(1)).exportConsentsByAccountIdAndAdditionalTppInfo(ACCOUNT_ID, START_DATE, END_DATE, INSTANCE_ID, null);
    }
}
