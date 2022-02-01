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

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AspspConsentDataPsuApiControllerTest {
    private static final String CONSENT_ID = "8cf490d3-5d5a-4b80-8d4d-11501125319e";
    private static final String ASPSP_CONSENT_DATA = "ASPSP consent data";
    private static final byte[] EMPTY_BODY = new byte[0];

    private JsonReader jsonReader = new JsonReader();
    private MockMvc mockMvc;

    @Mock
    private AspspDataService aspspDataService;

    @InjectMocks
    private AspspConsentDataPsuApiController aspspConsentDataPsuApiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aspspConsentDataPsuApiController)
                      .build();
    }

    @Test
    void getAspspConsentData_withValidRequest_shouldReturnBase64EncodedData() throws Exception {
        AspspConsentData aspspConsentData = new AspspConsentData(ASPSP_CONSENT_DATA.getBytes(), CONSENT_ID);
        when(aspspDataService.readAspspConsentData(CONSENT_ID))
            .thenReturn(Optional.of(aspspConsentData));
        String cmsAspspConsentDataBase64Json = jsonReader.getStringFromFile("json/aspsp/cms-aspsp-consent-data-base64.json");

        mockMvc.perform(get("/psu-api/v1/aspsp-consent-data/consents/{consent-id}", CONSENT_ID)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsAspspConsentDataBase64Json));
    }

    @Test
    void getAspspConsentData_withEmptyServiceResponse_shouldReturnNotFound() throws Exception {
        when(aspspDataService.readAspspConsentData(CONSENT_ID))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/psu-api/v1/aspsp-consent-data/consents/{consent-id}", CONSENT_ID))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));
    }

    @Test
    void updateAspspConsentData_withValidRequest_shouldReturnOk() throws Exception {
        String cmsAspspConsentDataBase64Json = jsonReader.getStringFromFile("json/aspsp/cms-aspsp-consent-data-base64.json");
        AspspConsentData aspspConsentData = new AspspConsentData(ASPSP_CONSENT_DATA.getBytes(), CONSENT_ID);
        when(aspspDataService.updateAspspConsentData(aspspConsentData))
            .thenReturn(true);

        mockMvc.perform(put("/psu-api/v1/aspsp-consent-data/consents/{consent-id}", CONSENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cmsAspspConsentDataBase64Json))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));
    }

    @Test
    void updateAspspConsentData_withFalseServiceResponse_shouldReturnNotFound() throws Exception {
        String cmsAspspConsentDataBase64Json = jsonReader.getStringFromFile("json/aspsp/cms-aspsp-consent-data-base64.json");
        AspspConsentData aspspConsentData = new AspspConsentData(ASPSP_CONSENT_DATA.getBytes(), CONSENT_ID);
        when(aspspDataService.updateAspspConsentData(aspspConsentData))
            .thenReturn(false);

        mockMvc.perform(put("/psu-api/v1/aspsp-consent-data/consents/{consent-id}", CONSENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cmsAspspConsentDataBase64Json))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));
    }

    @Test
    void deleteAspspConsentData_withValidRequest_shouldReturnNoContent() throws Exception {
        when(aspspDataService.deleteAspspConsentData(CONSENT_ID))
            .thenReturn(true);

        mockMvc.perform(delete("/psu-api/v1/aspsp-consent-data/consents/{consent-id}", CONSENT_ID))
            .andExpect(status().isNoContent())
            .andExpect(content().bytes(EMPTY_BODY));
    }

    @Test
    void deleteAspspConsentData_withFalseServiceResponse_shouldReturnNotFound() throws Exception {
        when(aspspDataService.deleteAspspConsentData(CONSENT_ID))
            .thenReturn(false);

        mockMvc.perform(delete("/psu-api/v1/aspsp-consent-data/consents/{consent-id}", CONSENT_ID))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));
    }
}
