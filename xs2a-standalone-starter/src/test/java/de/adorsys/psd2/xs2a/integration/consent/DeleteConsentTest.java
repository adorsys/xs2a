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


package de.adorsys.psd2.xs2a.integration.consent;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisAuthorisationConfirmationService;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration-test", "mock-qwac"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class
})
class DeleteConsentTest {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String CONSENT_PATH = "json/consent/AisAccountConsentInternalResponse.json";
    private static final String WRONG_TPP_RESPONSE_PATH = "/json/consent/res/WrongTppResponse.json";

    private static final String ENCRYPTED_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @MockBean
    private PisAuthorisationConfirmationService pisAuthorisationConfirmationService;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void init() {
        httpHeaders.setAll(buildRequestHeaders());

        given(aspspProfileService.getAspspSettings())
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(aspspProfileService.getScaApproaches())
            .willReturn(Collections.singletonList(ScaApproach.REDIRECT));
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo()))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(eventServiceEncrypted.recordEvent(any(EventBO.class)))
            .willReturn(true);
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    @Test
    void deleteConsent_successful() throws Exception {
        // Given
        given(aisConsentServiceEncrypted.getAisAccountConsentById(ENCRYPTED_CONSENT_ID))
            .willReturn(CmsResponse.<AisAccountConsent>builder()
                            .payload(buildAisAccountConsent(TPP_INFO))
                            .build());
        given(aisConsentServiceEncrypted.updateConsentStatusById(ENCRYPTED_CONSENT_ID, ConsentStatus.TERMINATED_BY_TPP))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.buildDeleteConsentUrl(ENCRYPTED_CONSENT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isNoContent());
    }

    @Test
    void deleteConsent_withWrongTpp_shouldReturnConsentInvalid() throws Exception {
        // Given
        String wrongTppId = "Wrong TPP ID";
        given(aisConsentServiceEncrypted.getAisAccountConsentById(ENCRYPTED_CONSENT_ID))
            .willReturn(CmsResponse.<AisAccountConsent>builder()
                            .payload(buildAisAccountConsent(TppInfoBuilder.buildTppInfo(wrongTppId)))
                            .build());

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.buildDeleteConsentUrl(ENCRYPTED_CONSENT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isForbidden())
            .andExpect(content().json(IOUtils.resourceToString(WRONG_TPP_RESPONSE_PATH, UTF_8)));
    }

    @NotNull
    private HashMap<String, String> buildRequestHeaders() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("PSU-ID", "PSU-123");
        headerMap.put("PSU-ID-Type", "Some type");
        headerMap.put("PSU-Corporate-ID", "Some corporate id");
        headerMap.put("PSU-Corporate-ID-Type", "Some corporate id type");
        headerMap.put("PSU-IP-Address", "1.1.1.1");
        headerMap.put("TPP-Redirect-URI", "ok.uri");
        return headerMap;
    }

    private AisAccountConsent buildAisAccountConsent(TppInfo tppInfo) {
        AisAccountConsent consent = jsonReader.getObjectFromFile(CONSENT_PATH, AisAccountConsent.class);
        consent.setTppInfo(tppInfo);
        return consent;
    }
}
