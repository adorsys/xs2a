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
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.*;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentAuthorizationResponseBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles("integration-test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(
    classes = {
        CorsConfigurationProperties.class,
        ObjectMapperConfig.class,
        WebConfig.class,
        Xs2aEndpointPathConstant.class,
        Xs2aInterfaceConfig.class
    })
public class ConsentUpdateAuthorisationIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private static final String PSU_ID_1 = "PSU-1";
    private static final String PSU_ID_2 = "PSU-2";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String AUTH_REQ = "/json/payment/req/auth_request.json";
    private static final String PSU_CREDENTIALS_INVALID_RESP = "/json/payment/res/explicit/psu_credentials_invalid_response.json";
    private static final String FORMAT_ERROR_RESP = "/json/payment/res/explicit/format_error_response.json";
    private static final String CONSENT_PATH = "/json/consent/req/AisAccountConsent.json";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private TppService tppService;
    @MockBean private TppStopListService tppStopListService;
    @MockBean private AspspProfileService aspspProfileService;
    @MockBean private Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean private AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    @MockBean private AisConsentServiceEncrypted aisConsentService;

    @Before
    public void setUp() {
        given(tppService.getTppInfo()).willReturn(TPP_INFO);
        given(tppService.getTppId()).willReturn(TPP_INFO.getAuthorisationNumber());
        given(aspspProfileService.getAspspSettings()).willReturn(AspspSettingsBuilder.buildAspspSettings());
    }

    @Test
    public void updateConsentPsuData_failed_psu_authorisation_psu_request_are_different() throws Exception {
        //When
        ResultActions resultActions = updateConsentPsuDataAndGetResultActions(PSU_ID_1, PSU_ID_2);

        //Then
        resultActions
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(PSU_CREDENTIALS_INVALID_RESP, UTF_8)));
    }

    @Test
    public void updateConsentPsuData_failed_no_psu_authorisation_no_psu_request() throws Exception {
        //When
        ResultActions resultActions = updateConsentPsuDataAndGetResultActions(null, null);

        //Then
        resultActions
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(FORMAT_ERROR_RESP, UTF_8)));
    }

    private ResultActions updateConsentPsuDataAndGetResultActions(String psuIdAuthorisation, String psuIdHeader) throws Exception {
        //Given
        String request = IOUtils.resourceToString(AUTH_REQ, UTF_8);
        ScaApproach scaApproach = ScaApproach.EMBEDDED;

        PsuIdData psuIdDataAuthorisation = buildPsuIdDataAuthorisation(psuIdAuthorisation);
        HttpHeadersIT httpHeaders = buildHttpHeaders(psuIdHeader);

        AisAccountConsent aisAccountConsent = AisConsentBuilder.buildAisAccountConsent(CONSENT_PATH, scaApproach, CONSENT_ID, mapper);

        given(aisConsentService.getAisAccountConsentById(CONSENT_ID))
            .willReturn(Optional.of(aisAccountConsent));
        given(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(any(String.class), any(String.class)))
            .willReturn(Optional.of(AisConsentAuthorizationResponseBuilder.buildAisConsentAuthorizationResponse(scaApproach, psuIdDataAuthorisation)));
        given(aisConsentAuthorisationServiceEncrypted.getAuthorisationScaApproach(any(String.class)))
            .willReturn(Optional.of(new AuthorisationScaApproachResponse(scaApproach)));

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.buildConsentUpdateAuthorisationUrl(CONSENT_ID, AUTHORISATION_ID));
        requestBuilder.headers(httpHeaders);
        requestBuilder.content(request);

        return mockMvc.perform(requestBuilder);
    }

    private HttpHeadersIT buildHttpHeaders(String psuIdHeader) {
        HttpHeadersIT httpHeadersBase = HttpHeadersBuilder.buildHttpHeaders();
        return Optional.ofNullable(psuIdHeader)
                   .map(httpHeadersBase::addPsuIdHeader)
                   .orElse(httpHeadersBase);
    }

    private PsuIdData buildPsuIdDataAuthorisation(String psuIdAuthorisation) {
        return Optional.ofNullable(psuIdAuthorisation)
                   .map(PsuIdDataBuilder::buildPsuIdData)
                   .orElse(null);
    }
}
