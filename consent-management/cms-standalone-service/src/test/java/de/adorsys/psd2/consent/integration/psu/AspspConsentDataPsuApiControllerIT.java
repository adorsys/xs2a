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

package de.adorsys.psd2.consent.integration.psu;

import de.adorsys.psd2.consent.ConsentManagementStandaloneApp;
import de.adorsys.psd2.consent.config.WebConfig;
import de.adorsys.psd2.consent.domain.AspspConsentDataEntity;
import de.adorsys.psd2.consent.integration.UrlBuilder;
import de.adorsys.psd2.consent.repository.AspspConsentDataRepository;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"api-integration-test"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = ConsentManagementStandaloneApp.class)
@ContextConfiguration(classes = WebConfig.class)
class AspspConsentDataPsuApiControllerIT {

    private static final String CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AspspConsentDataRepository aspspConsentDataRepository;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;
    private AspspConsentDataEntity aspspConsentDataEntity;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        aspspConsentDataEntity = new AspspConsentDataEntity();
        aspspConsentDataEntity.setConsentId(CONSENT_ID);
        aspspConsentDataEntity.setData("data".getBytes());
    }

    @Test
    void getAspspConsentData() throws Exception {
        given(aspspConsentDataRepository.findByConsentId(CONSENT_ID)).willReturn(Optional.of(aspspConsentDataEntity));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getAspspConsentData(CONSENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-aspsp-consent-data-base64.json")));
    }

    @Test
    void updateAspspConsentData() throws Exception {
        given(aspspConsentDataRepository.findByConsentId(CONSENT_ID)).willReturn(Optional.of(aspspConsentDataEntity));
        given(aspspConsentDataRepository.save(aspspConsentDataEntity)).willReturn(aspspConsentDataEntity);

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.getAspspConsentData(CONSENT_ID))
                                                           .content(jsonReader.getStringFromFile("json/consent/integration/psu/expect/cms-aspsp-consent-data-base64.json"));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    void deleteAspspConsentData() throws Exception {
        given(aspspConsentDataRepository.existsById(CONSENT_ID)).willReturn(true);

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.getAspspConsentData(CONSENT_ID));
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verify(aspspConsentDataRepository).deleteById(CONSENT_ID);
    }
}
