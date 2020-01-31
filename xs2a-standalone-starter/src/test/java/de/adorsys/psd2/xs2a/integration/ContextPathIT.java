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


package de.adorsys.psd2.xs2a.integration;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentAuthorizationResponseBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentBuilder;
import org.apache.commons.io.IOUtils;
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class ContextPathIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String CREATE_CONSENT_REQUEST_JSON_PATH = "/json/account/req/DedicatedConsent.json";
    private static final String CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_CONTEXT_PATH = "/json/account/res/contextpath/CreateAisConsent_implicit_redirect_withContextPath_response.json";
    private static final String CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_FORCED_PATH = "/json/account/res/contextpath/CreateAisConsent_implicit_redirect_forcedUrl_response.json";
    private static final String CONTEXT_PATH = "/mypath";

    private static final String ENCRYPT_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;

    private HttpHeaders httpHeadersImplicit = new HttpHeaders();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;
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
    private AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;

    @BeforeEach
    void init() {
        httpHeadersImplicit.add("Content-Type", "application/json");
        httpHeadersImplicit.add("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        httpHeadersImplicit.add("PSU-ID", "PSU-123");
        httpHeadersImplicit.add("PSU-ID-Type", "Some type");
        httpHeadersImplicit.add("PSU-Corporate-ID", "Some corporate id");
        httpHeadersImplicit.add("PSU-Corporate-ID-Type", "Some corporate id type");
        httpHeadersImplicit.add("PSU-IP-Address", "1.1.1.1");
        httpHeadersImplicit.add("TPP-Implicit-Authorisation-Preferred", "false");
        httpHeadersImplicit.add("TPP-Redirect-URI", "ok.uri");


        given(aspspProfileService.getAspspSettings())
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
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
    void createConsent_withCustomContextPath_shouldCreateConsent() throws Exception {
        createConsent_withCustomContextPath(httpHeadersImplicit, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_CONTEXT_PATH);
    }

    @Test
    void createConsent_withCustomContextPathAndForcedXs2aBaseUrl_shouldCreateConsentWithForcedLinks() throws Exception {
        when(aspspProfileService.getAspspSettings())
            .thenReturn(AspspSettingsBuilder.buildAspspSettingsWithForcedXs2aBaseUrl("http://myhost.com/mypath/"));

        createConsent_withCustomContextPath(httpHeadersImplicit, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_FORCED_PATH);
    }

    private void createConsent_withCustomContextPath(HttpHeaders headers, String responseJsonPath) throws Exception {
        // Given
        AisAccountConsent aisAccountConsent = AisConsentBuilder.buildAisAccountConsent(CREATE_CONSENT_REQUEST_JSON_PATH, SCA_APPROACH, ENCRYPT_CONSENT_ID, xs2aObjectMapper);

        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(SCA_APPROACH));
        given(aisConsentAuthorisationServiceEncrypted.createAuthorizationWithResponse(any(String.class), any(AisConsentAuthorizationRequest.class)))
            .willReturn(CmsResponse.<CreateAisConsentAuthorizationResponse>builder()
                            .payload(new CreateAisConsentAuthorizationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null))
                            .build());
        given(aisConsentServiceEncrypted.createConsent(any(CreateAisConsentRequest.class)))
            .willReturn(CmsResponse.<CreateAisConsentResponse>builder()
                            .payload(new CreateAisConsentResponse(ENCRYPT_CONSENT_ID, aisAccountConsent, Arrays.asList(NotificationSupportedMode.LAST, NotificationSupportedMode.SCA)))
                            .build());
        given(aisConsentServiceEncrypted.updateAspspAccountAccessWithResponse(eq(ENCRYPT_CONSENT_ID), any(AisAccountAccessInfo.class)))
            .willReturn(CmsResponse.<AisAccountConsent>builder()
                            .payload(aisAccountConsent)
                            .build());
        given(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(any(String.class), any(String.class)))
            .willReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(AisConsentAuthorizationResponseBuilder.buildAisConsentAuthorizationResponse(SCA_APPROACH))
                            .build());
        given(aspspDataService.readAspspConsentData(any(String.class)))
            .willReturn(Optional.of(new AspspConsentData(null, ENCRYPT_CONSENT_ID)));
        when(aisConsentAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(SCA_APPROACH))
                            .build());

        String requestUrl = CONTEXT_PATH + UrlBuilder.buildConsentCreation();
        MockHttpServletRequestBuilder requestBuilder = post(requestUrl);
        requestBuilder.headers(headers);
        requestBuilder.content(resourceToString(CREATE_CONSENT_REQUEST_JSON_PATH, UTF_8));
        requestBuilder.contextPath(CONTEXT_PATH);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(responseJsonPath, UTF_8)));
    }
}
