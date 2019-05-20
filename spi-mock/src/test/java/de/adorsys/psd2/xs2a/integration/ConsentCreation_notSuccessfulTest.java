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

package de.adorsys.psd2.xs2a.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.spi.ASPSPXs2aApplication;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.EventServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentAuthorizationResponseBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = ASPSPXs2aApplication.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    ObjectMapperConfig.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class
})
public class ConsentCreation_notSuccessfulTest {
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String BANK_OFFERED_CONSENT_REQUEST_JSON_PATH = "/json/account/req/BankOfferedConsent.json";
    private static final String TPP_ERROR_MESSAGE_JSON_PATH = "/json/account/res/TppErrorMessage.json";
    private static final String PSU_IP_ADDRESS_MISSING_ERROR_MESSAGE_JSON_PATH = "/json/account/res/PsuIpAddressMissingErrorMessage.json";
    private static final String ENCRYPT_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();

    private HttpHeaders httpHeadersImplicit = new HttpHeaders();
    private HttpHeaders httpHeadersExplicit = new HttpHeaders();
    private HttpHeaders httpHeadersWithoutPsuIpAddress = new HttpHeaders();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private EventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @MockBean
    private AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;
    @MockBean
    @Qualifier("aspspRestTemplate")
    private RestTemplate aspspRestTemplate;

    @Before
    public void init() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("tpp-qwac-certificate", "qwac certificate");
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

        given(aspspProfileService.getAspspSettings())
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppService.getTppInfo())
            .willReturn(TPP_INFO);
        given(tppService.getTppId())
            .willReturn(TPP_INFO.getAuthorisationNumber());
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.buildTppUniqueParamsHolder()))
            .willReturn(false);
        given(eventServiceEncrypted.recordEvent(any(Event.class)))
            .willReturn(true);
    }

    // =============== IMPLICIT MODE
    //
    @Test
    public void creation_bank_offered_consent_implicit_embedded_notSuccessful() throws Exception {
        consentCreation_notSuccessful(httpHeadersImplicit, ScaApproach.EMBEDDED, BANK_OFFERED_CONSENT_REQUEST_JSON_PATH);
    }

    // =============== EXPLICIT MODE
    //
    @Test
    public void creation_bank_offered_consent_explicit_embedded_notSuccessful() throws Exception {
        consentCreation_notSuccessful(httpHeadersExplicit, ScaApproach.EMBEDDED, BANK_OFFERED_CONSENT_REQUEST_JSON_PATH);
    }

    @Test
    public void creation_consent_withoutPsuIpAddress_notSuccessful() throws Exception {
        //Given
        MockHttpServletRequestBuilder requestBuilder = makeRequestBuilder(UrlBuilder.buildConsentCreation(), httpHeadersWithoutPsuIpAddress, resourceToString(BANK_OFFERED_CONSENT_REQUEST_JSON_PATH, UTF_8));
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
        resultActions.andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(TPP_ERROR_MESSAGE_JSON_PATH, UTF_8)));
    }

    private void makePreparations(ScaApproach scaApproach, String requestJsonPath) throws Exception {
        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(scaApproach));
        given(aisConsentAuthorisationServiceEncrypted.createAuthorization(any(String.class), any(AisConsentAuthorizationRequest.class)))
            .willReturn(Optional.of(AUTHORISATION_ID));
        given(aisConsentServiceEncrypted.createConsent(any(CreateAisConsentRequest.class)))
            .willReturn(Optional.of(ENCRYPT_CONSENT_ID));
        given(aisConsentServiceEncrypted.getInitialAisAccountConsentById(any(String.class)))
            .willReturn(Optional.of(AisConsentBuilder.buildAisAccountConsent(requestJsonPath, scaApproach, ENCRYPT_CONSENT_ID, mapper)));
        given(aisConsentServiceEncrypted.getAisAccountConsentById(any(String.class)))
            .willReturn(Optional.of(AisConsentBuilder.buildAisAccountConsent(requestJsonPath, scaApproach, ENCRYPT_CONSENT_ID, mapper)));
        given(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(any(String.class), any(String.class)))
            .willReturn(Optional.of(AisConsentAuthorizationResponseBuilder.buildAisConsentAuthorizationResponse(scaApproach)));
        given(aspspDataService.readAspspConsentData(any(String.class)))
            .willReturn(Optional.of(new AspspConsentData(null, ENCRYPT_CONSENT_ID)));
        given(aspspRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class), any(String.class)))
            .willReturn(ResponseEntity.ok(new ArrayList<SpiAccountDetails>()));
    }

    private MockHttpServletRequestBuilder makeRequestBuilder(String url, HttpHeaders headers, String content) {
        return MockMvcRequestBuilders.post(url)
                   .headers(headers)
                   .content(content);
    }
}
