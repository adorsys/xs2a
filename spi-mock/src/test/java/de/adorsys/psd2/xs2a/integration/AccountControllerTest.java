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


import de.adorsys.aspsp.xs2a.spi.ASPSPXs2aApplication;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.service.EventServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.consent.service.AisConsentServiceRemote;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration-test", "mockspi"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = ASPSPXs2aApplication.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    ObjectMapperConfig.class,
    ScaAuthorizationConfig.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class
})
public class AccountControllerTest {
    private final static String ACCOUNT_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private final static String CONSENT_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private final static TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
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
    private EventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private AisConsentServiceRemote aisConsentServiceRemote;
    @MockBean
    private Xs2aAisConsentMapper xs2aAisConsentMapper;
    @MockBean
    @Qualifier("consentRestTemplate")
    private RestTemplate consentRestTemplate;

    @Before
    public void init() {
        // common actions for all tests
        given(aspspProfileService.getScaApproach()).willReturn(ScaApproach.REDIRECT);
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
        given(aisConsentServiceRemote.getAisAccountConsentById(CONSENT_ID)).willReturn(Optional.of(new AisAccountConsent()));
        given(consentRestTemplate.getForEntity(any(String.class), any(Class.class))).willReturn(ResponseEntity.ok(Void.class));

        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("tpp-qwac-certificate", "qwac certificate");
        httpHeaders.add("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        httpHeaders.add("consent-id", "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc");
        httpHeaders.add("PSU-ID", "PSU-123");
        httpHeaders.add("PSU-ID-Type", "Some type");
        httpHeaders.add("PSU-Corporate-ID", "Some corporate id");
        httpHeaders.add("PSU-Corporate-ID-Type", "Some corporate id type");
        httpHeaders.add("PSU-IP-Address", "1.1.1.1");
        httpHeaders.add("accept", "application/json, application/xml");
    }

    @Test
    public void getTransactions_ShouldFail_WithoutEndSlash() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetTransactionsUrlWithoutSlash(ACCOUNT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    public void getTransactions_ShouldFail_WithEndSlash() throws Exception {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetTransactionsUrlWithSlash(ACCOUNT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }
}

