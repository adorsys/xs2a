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
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
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
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentAuthorizationResponseBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.CmsConsentBuilder;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
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
class ConsentCreationNotSuccessfulIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String BANK_OFFERED_CONSENT_REQUEST_JSON_PATH = "/json/account/req/BankOfferedConsent.json";
    private static final String TPP_ERROR_MESSAGE_JSON_PATH = "/json/account/res/TppErrorMessage.json";
    private static final String PSU_IP_ADDRESS_MISSING_ERROR_MESSAGE_JSON_PATH = "/json/account/res/PsuIpAddressMissingErrorMessage.json";
    private static final String ENCRYPT_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";

    private final HttpHeaders httpHeadersImplicit = new HttpHeaders();
    private final HttpHeaders httpHeadersExplicit = new HttpHeaders();
    private final HttpHeaders httpHeadersWithoutPsuIpAddress = new HttpHeaders();

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
    private ConsentServiceEncrypted aisConsentServiceEncrypted;
    @MockBean
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;

    @BeforeEach
    void init() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("PSU-ID", "PSU-123");
        headerMap.put("PSU-ID-Type", "Some type");
        headerMap.put("PSU-Corporate-ID", "Some corporate id");
        headerMap.put("PSU-Corporate-ID-Type", "Some corporate id type");

        httpHeadersWithoutPsuIpAddress.setAll(headerMap);

        headerMap.put("PSU-IP-Address", "1.1.1.1");

        httpHeadersImplicit.setAll(headerMap);
        // when Implicit auth mode we need to set 'false'
        httpHeadersImplicit.add("TPP-Implicit-Authorisation-Preferred", "false");

        httpHeadersExplicit.setAll(headerMap);
        // when we use Explicit auth mode we need to set 'true' and value 'signingBasketSupported' in profile also should be 'true'
        httpHeadersExplicit.add("TPP-Explicit-Authorisation-Preferred", "true");

        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo(), null))
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

    // =============== IMPLICIT MODE
    //
    @Test
    void creation_bank_offered_consent_implicit_embedded_notSuccessful() throws Exception {
        consentCreation_notSuccessful(httpHeadersImplicit, ScaApproach.EMBEDDED, BANK_OFFERED_CONSENT_REQUEST_JSON_PATH);
    }

    // =============== EXPLICIT MODE
    //
    @Test
    void creation_bank_offered_consent_explicit_embedded_notSuccessful() throws Exception {
        consentCreation_notSuccessful(httpHeadersExplicit, ScaApproach.EMBEDDED, BANK_OFFERED_CONSENT_REQUEST_JSON_PATH);
    }

    @Test
    void creation_consent_withoutPsuIpAddress_notSuccessful() throws Exception {
        //Given
        MockHttpServletRequestBuilder requestBuilder = makeRequestBuilder(UrlBuilder.buildConsentCreation(), httpHeadersWithoutPsuIpAddress, resourceToString(BANK_OFFERED_CONSENT_REQUEST_JSON_PATH, UTF_8));
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(ScaApproach.EMBEDDED));

        //When
        ResultActions resultActions = mockMvc.perform(requestBuilder);
        //Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(IOUtils.resourceToString(PSU_IP_ADDRESS_MISSING_ERROR_MESSAGE_JSON_PATH, UTF_8)));
    }

    private void consentCreation_notSuccessful(HttpHeaders headers, ScaApproach scaApproach, String requestJsonPath) throws Exception {
        //Given
        makePreparations(scaApproach, requestJsonPath);
        MockHttpServletRequestBuilder requestBuilder = makeRequestBuilder(UrlBuilder.buildConsentCreation(), headers, resourceToString(requestJsonPath, UTF_8));
        //When
        ResultActions resultActions = mockMvc.perform(requestBuilder);
        //Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(TPP_ERROR_MESSAGE_JSON_PATH, UTF_8)));
    }

    private void makePreparations(ScaApproach scaApproach, String requestJsonPath) throws Exception {
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(scaApproach));
        CmsConsent aisAccountConsent = CmsConsentBuilder.buildCmsConsent(requestJsonPath, scaApproach, ENCRYPT_CONSENT_ID, xs2aObjectMapper);
        CmsCreateConsentResponse cmsCreateConsentResponse = new CmsCreateConsentResponse(ENCRYPT_CONSENT_ID, aisAccountConsent);
        given(aisConsentServiceEncrypted.createConsent(any(CmsConsent.class))).willReturn(CmsResponse.<CmsCreateConsentResponse>builder().payload(cmsCreateConsentResponse).build());
        given(authorisationServiceEncrypted.getAuthorisationById(any(String.class)))
            .willReturn(CmsResponse.<Authorisation>builder()
                            .payload(AisConsentAuthorizationResponseBuilder.buildAisConsentAuthorizationResponse(scaApproach))
                            .build());
        given(aspspDataService.readAspspConsentData(any(String.class)))
            .willReturn(Optional.of(new AspspConsentData(null, ENCRYPT_CONSENT_ID)));
    }

    private MockHttpServletRequestBuilder makeRequestBuilder(String url, HttpHeaders headers, String content) {
        return MockMvcRequestBuilders.post(url)
                   .headers(headers)
                   .content(content);
    }
}
