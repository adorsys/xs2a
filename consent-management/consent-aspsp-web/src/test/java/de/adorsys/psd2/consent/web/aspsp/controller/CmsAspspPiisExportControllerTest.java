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

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisFundsExportService;
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
class CmsAspspPiisExportControllerTest {
    private final String PSU_ID = "marion.mueller";
    private final String TPP_ID = "PSDDE-FAKENCA-87B2AC";
    private final String ACCOUNT_ID = "account_id";
    private final String EXPORT_PIIS_CONSENT_BY_TPP = "/aspsp-api/v1/piis/consents/tpp/PSDDE-FAKENCA-87B2AC";
    private final String EXPORT_PIIS_CONSENT_BY_PSU = "/aspsp-api/v1/piis/consents/psu";
    private final String EXPORT_PIIS_CONSENT_BY_ACCOUNT = "/aspsp-api/v1/piis/consents/account/account_id";
    private final LocalDate START_DATE = LocalDate.of(2019, 2, 25);
    private final LocalDate END_DATE = LocalDate.of(2020, 7, 22);
    private final String INSTANCE_ID = "UNDEFINED";
    private final String LIST_OF_PIIS_CONSENTS_PATH = "json/piis/list-piis-consent.json";
    private static final Integer PAGE_INDEX = 0;
    private static final Integer ITEMS_PER_PAGE = 20;

    private MockMvc mockMvc;
    private final JsonReader jsonReader = new JsonReader();
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private PsuIdData psuIdData;
    private Collection<CmsPiisConsent> cmsPiisConsents;

    @InjectMocks
    private CmsAspspPiisExportController cmsAspspPiisExportController;

    @Mock
    private CmsAspspPiisFundsExportService cmsAspspPiisExportService;

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
        CmsPiisConsent cmsPiisConsent = jsonReader.getObjectFromFile("json/piis/cms-piis-consent.json", CmsPiisConsent.class);
        cmsPiisConsents = Collections.singletonList(cmsPiisConsent);

        httpHeaders.add("psu-id", PSU_ID);
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Start-Date", START_DATE.toString());
        httpHeaders.add("End-Date", END_DATE.toString());
        httpHeaders.add("instance-id", INSTANCE_ID);

        mockMvc = MockMvcBuilders
                      .standaloneSetup(cmsAspspPiisExportController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void getConsentsByTpp_Success() throws Exception {
        when(cmsAspspPiisExportService.exportConsentsByTpp(TPP_ID, START_DATE, END_DATE, psuIdData, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(new PageData<>(cmsPiisConsents, 0, 20, cmsPiisConsents.size()));

        mockMvc.perform(get(EXPORT_PIIS_CONSENT_BY_TPP)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().isOk())
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPiisExportService, times(1)).exportConsentsByTpp(TPP_ID, START_DATE, END_DATE, psuIdData, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
    }

    @Test
    void getConsentsByPsu_Success() throws Exception {
        when(cmsAspspPiisExportService.exportConsentsByPsu(psuIdData, START_DATE, END_DATE, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(new PageData<>(cmsPiisConsents, 0, 20, cmsPiisConsents.size()));

        mockMvc.perform(get(EXPORT_PIIS_CONSENT_BY_PSU)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().isOk())
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPiisExportService, times(1)).exportConsentsByPsu(psuIdData, START_DATE, END_DATE, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
    }

    @Test
    void getConsentsByAccount_Success() throws Exception {
        when(cmsAspspPiisExportService.exportConsentsByAccountId(ACCOUNT_ID, START_DATE, END_DATE, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE))
            .thenReturn(new PageData<>(cmsPiisConsents, 0, 20, cmsPiisConsents.size()));

        mockMvc.perform(get(EXPORT_PIIS_CONSENT_BY_ACCOUNT)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().isOk())
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPiisExportService, times(1)).exportConsentsByAccountId(ACCOUNT_ID, START_DATE, END_DATE, INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
    }
}
