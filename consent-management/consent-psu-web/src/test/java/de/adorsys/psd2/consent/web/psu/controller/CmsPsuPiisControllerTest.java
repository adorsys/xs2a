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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.psu.api.CmsPsuPiisService;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsPsuPiisControllerTest {
    private static final String CONSENT_ID = "someConsentId";
    private static final String PSU_ID = "psu id";
    private static final String PSU_ID_TYPE = "psu id type";
    private static final String PSU_CORPORATE_ID = "psu corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "psu corporate id type";
    private static final String INSTANCE_ID = "instance id";

    private static final String INSTANCE_ID_HEADER_NAME = "instance-id";
    private static final String PSU_ID_HEADER_NAME = "psu-id";
    private static final String PSU_ID_TYPE_HEADER_NAME = "psu-id-type";
    private static final String PSU_CORPORATE_ID_HEADER_NAME = "psu-corporate-id";
    private static final String PSU_CORPORATE_ID_TYPE_HEADER_NAME = "psu-corporate-id-type";
    private static final HttpHeaders PSU_HEADERS = buildPsuHeaders();
    private static final HttpHeaders INSTANCE_ID_HEADERS = buildInstanceIdHeaders();
    private static final byte[] EMPTY_BODY = new byte[0];

    private JsonReader jsonReader = new JsonReader();
    private MockMvc mockMvc;
    private PsuIdData psuIdData;

    @Mock
    private CmsPsuPiisService cmsPsuPiisService;

    @InjectMocks
    private CmsPsuPiisController cmsPsuPiisController;

    @BeforeEach
    void setUp() {
        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);

        Xs2aObjectMapper xs2aObjectMapper = new ObjectMapperConfig().xs2aObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(cmsPsuPiisController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(xs2aObjectMapper))
                      .build();
    }

    @Test
    void getConsent_withValidRequest_shouldReturnConsent() throws Exception {
        String cmsPiisConsentJson = jsonReader.getStringFromFile("json/piis/response/cms-piis-consent.json");
        CmsPiisConsent cmsPiisConsent = jsonReader.getObjectFromString(cmsPiisConsentJson, CmsPiisConsent.class);
        when(cmsPsuPiisService.getConsent(psuIdData, CONSENT_ID, INSTANCE_ID))
            .thenReturn(Optional.of(cmsPiisConsent));

        mockMvc.perform(get("/psu-api/v1/piis/consents/{consent-id}", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsPiisConsentJson));
    }

    @Test
    void getConsent_withEmptyServiceResponse_shouldReturnNotFound() throws Exception {
        when(cmsPsuPiisService.getConsent(psuIdData, CONSENT_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/psu-api/v1/piis/consents/{consent-id}", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));
    }

    @Test
    void getConsentsForPsu_withValidRequest_shouldReturnList() throws Exception {
        String cmsPiisConsentsJson = jsonReader.getStringFromFile("json/piis/response/cms-piis-consent-list.json");
        List<CmsPiisConsent> cmsPiisConsents = jsonReader.getListFromString(cmsPiisConsentsJson, CmsPiisConsent.class);
        when(cmsPsuPiisService.getConsentsForPsu(psuIdData, INSTANCE_ID, null, null))
            .thenReturn(cmsPiisConsents);

        mockMvc.perform(get("/psu-api/v1/piis/consents")
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsPiisConsentsJson));
    }

    @Test
    void getConsentsForPsu_withEmptyListServiceResponse_shouldReturnList() throws Exception {
        String emptyListJson = "[]";
        when(cmsPsuPiisService.getConsentsForPsu(psuIdData, INSTANCE_ID, null, null))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/psu-api/v1/piis/consents")
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(emptyListJson));
    }

    @Test
    void revokeConsent_withTrueServiceResponse_shouldReturnTrue() throws Exception {
        when(cmsPsuPiisService.revokeConsent(psuIdData, CONSENT_ID, INSTANCE_ID)).thenReturn(true);

        mockMvc.perform(put("/psu-api/v1/piis/consents/{consent-id}/revoke-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    void revokeConsent_withFalseServiceResponse_shouldReturnFalse() throws Exception {
        when(cmsPsuPiisService.revokeConsent(psuIdData, CONSENT_ID, INSTANCE_ID)).thenReturn(false);

        mockMvc.perform(put("/psu-api/v1/piis/consents/{consent-id}/revoke-consent", CONSENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
    }

    private static HttpHeaders buildInstanceIdHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(INSTANCE_ID_HEADER_NAME, INSTANCE_ID);
        return httpHeaders;
    }

    private static HttpHeaders buildPsuHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(PSU_ID_HEADER_NAME, PSU_ID);
        httpHeaders.add(PSU_ID_TYPE_HEADER_NAME, PSU_ID_TYPE);
        httpHeaders.add(PSU_CORPORATE_ID_HEADER_NAME, PSU_CORPORATE_ID);
        httpHeaders.add(PSU_CORPORATE_ID_TYPE_HEADER_NAME, PSU_CORPORATE_ID_TYPE);
        return httpHeaders;
    }
}
