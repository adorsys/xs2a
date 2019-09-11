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
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.starter.config.validation.PaymentValidationConfigImpl;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
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
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("integration-test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    ObjectMapperConfig.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class,
    PaymentValidationConfigImpl.class
})
public class ContextPathTest {
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String CREATE_CONSENT_REQUEST_JSON_PATH = "/json/account/req/DedicatedConsent.json";
    private static final String CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_CONTEXT_PATH = "/json/account/res/contextpath/CreateAisConsent_implicit_redirect_withContextPath_response.json";
    private static final String CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_FORCED_PATH = "/json/account/res/contextpath/CreateAisConsent_implicit_redirect_forcedUrl_response.json";
    private static final String CONTEXT_PATH = "/mypath";

    private static final String ENCRYPT_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;

    private HttpHeaders httpHeadersImplicit = new HttpHeaders();

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
    private Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @MockBean
    private AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;
    @MockBean
    @Qualifier("aspspRestTemplate")
    private RestTemplate aspspRestTemplate;
    @MockBean
    @Qualifier("consentRestTemplate")
    private RestTemplate consentRestTemplate;

    @Before
    public void init() {
        httpHeadersImplicit.add("Content-Type", "application/json");
        httpHeadersImplicit.add("tpp-qwac-certificate", "qwac certificate");
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
        given(tppService.getTppInfo())
            .willReturn(TPP_INFO);
        given(tppService.getTppId())
            .willReturn(TPP_INFO.getAuthorisationNumber());
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo()))
            .willReturn(false);
        given(eventServiceEncrypted.recordEvent(any(EventBO.class)))
            .willReturn(true);
        given(consentRestTemplate.postForEntity(anyString(), any(EventBO.class), eq(Boolean.class)))
            .willReturn(new ResponseEntity<>(true, HttpStatus.OK));
    }

    @Test
    public void createConsent_withCustomContextPath_shouldCreateConsent() throws Exception {
        createConsent_withCustomContextPath(httpHeadersImplicit, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_CONTEXT_PATH);
    }

    @Test
    public void createConsent_withCustomContextPathAndForcedXs2aBaseUrl_shouldCreateConsentWithForcedLinks() throws Exception {
        when(aspspProfileService.getAspspSettings())
            .thenReturn(AspspSettingsBuilder.buildAspspSettingsWithForcedXs2aBaseUrl("http://myhost.com/mypath/"));

        createConsent_withCustomContextPath(httpHeadersImplicit, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_FORCED_PATH);
    }

    private void createConsent_withCustomContextPath(HttpHeaders headers, String responseJsonPath) throws Exception {
        // Given
        AisAccountConsent aisAccountConsent = AisConsentBuilder.buildAisAccountConsent(CREATE_CONSENT_REQUEST_JSON_PATH, SCA_APPROACH, ENCRYPT_CONSENT_ID, mapper);

        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(SCA_APPROACH));
        given(aisConsentAuthorisationServiceEncrypted.createAuthorizationWithResponse(any(String.class), any(AisConsentAuthorizationRequest.class)))
            .willReturn(Optional.of(new CreateAisConsentAuthorizationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED)));
        given(aisConsentServiceEncrypted.createConsent(any(CreateAisConsentRequest.class)))
            .willReturn(Optional.of(ENCRYPT_CONSENT_ID));
        given(aisConsentServiceEncrypted.getInitialAisAccountConsentById(any(String.class)))
            .willReturn(Optional.of(aisAccountConsent));
        given(aisConsentServiceEncrypted.getAisAccountConsentById(any(String.class)))
            .willReturn(Optional.of(aisAccountConsent));
        given(aisConsentServiceEncrypted.updateAspspAccountAccessWithResponse(eq(ENCRYPT_CONSENT_ID), any(AisAccountAccessInfo.class)))
            .willReturn(Optional.of(aisAccountConsent));
        given(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(any(String.class), any(String.class)))
            .willReturn(Optional.of(AisConsentAuthorizationResponseBuilder.buildAisConsentAuthorizationResponse(SCA_APPROACH)));
        given(aspspDataService.readAspspConsentData(any(String.class)))
            .willReturn(Optional.of(new AspspConsentData(null, ENCRYPT_CONSENT_ID)));
        // noinspection unchecked Supress warning on using generic class in Mockito's any(...) method
        given(aspspRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class), any(String.class)))
            .willReturn(ResponseEntity.ok(new ArrayList<SpiAccountDetails>()));
        when(aisConsentAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(Optional.of(new AuthorisationScaApproachResponse(SCA_APPROACH)));

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
