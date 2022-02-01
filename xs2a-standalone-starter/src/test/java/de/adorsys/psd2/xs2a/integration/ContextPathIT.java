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


package de.adorsys.psd2.xs2a.integration;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.authorisation.AuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.*;
import de.adorsys.psd2.core.data.AccountAccess;
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
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentAuthorizationResponseBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.CmsConsentBuilder;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AisAuthorisationProcessorRequest;
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
import java.util.Collections;
import java.util.Optional;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
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
    private static final String TEST_PSU_MESSAGE = "psu message";

    private final HttpHeaders httpHeadersImplicit = new HttpHeaders();

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
    private ConsentServiceEncrypted consentServiceEncrypted;
    @MockBean
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;
    @MockBean
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @MockBean
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;

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

    @Test
    void createConsent_withCustomContextPath_shouldCreateConsent() throws Exception {
        createConsent_withCustomContextPath(httpHeadersImplicit, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_CONTEXT_PATH);
    }

    @Test
    void createConsent_withCustomContextPathAndForcedXs2aBaseUrl_shouldCreateConsentWithForcedLinks() throws Exception {
        when(aspspProfileService.getAspspSettings(null))
            .thenReturn(AspspSettingsBuilder.buildAspspSettingsWithForcedXs2aBaseUrl("http://myhost.com/mypath/"));

        createConsent_withCustomContextPath(httpHeadersImplicit, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_FORCED_PATH);
    }

    private void createConsent_withCustomContextPath(HttpHeaders headers, String responseJsonPath) throws Exception {
        // Given
        CmsConsent cmsConsent = CmsConsentBuilder.buildCmsConsent(CREATE_CONSENT_REQUEST_JSON_PATH, SCA_APPROACH, ENCRYPT_CONSENT_ID, xs2aObjectMapper);

        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(SCA_APPROACH));
        given(authorisationServiceEncrypted.createAuthorisation(any(AuthorisationParentHolder.class), any(CreateAuthorisationRequest.class)))
            .willReturn(CmsResponse.<CreateAuthorisationResponse>builder()
                            .payload(new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null, ScaApproach.EMBEDDED))
                            .build());
        given(consentServiceEncrypted.createConsent(any(CmsConsent.class)))
            .willReturn(CmsResponse.<CmsCreateConsentResponse>builder()
                            .payload(new CmsCreateConsentResponse(ENCRYPT_CONSENT_ID, cmsConsent))
                            .build());
        given(consentServiceEncrypted.getConsentById(ENCRYPT_CONSENT_ID))
            .willReturn(CmsResponse.<CmsConsent>builder()
                            .payload(cmsConsent)
                            .build());
        given(aisConsentServiceEncrypted.updateAspspAccountAccess(eq(ENCRYPT_CONSENT_ID), any(AccountAccess.class)))
            .willReturn(CmsResponse.<CmsConsent>builder()
                            .payload(cmsConsent)
                            .build());
        given(authorisationServiceEncrypted.getAuthorisationById(any(String.class)))
            .willReturn(CmsResponse.<Authorisation>builder()
                            .payload(AisConsentAuthorizationResponseBuilder.buildAisConsentAuthorizationResponse(SCA_APPROACH))
                            .build());
        given(aspspDataService.readAspspConsentData(any(String.class)))
            .willReturn(Optional.of(new AspspConsentData(null, ENCRYPT_CONSENT_ID)));
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(SCA_APPROACH))
                            .build());
        CreateConsentAuthorisationProcessorResponse processorResponse =
            new CreateConsentAuthorisationProcessorResponse(ScaStatus.STARTED, SCA_APPROACH, TEST_PSU_MESSAGE,
                                                            Collections.emptySet(), ENCRYPT_CONSENT_ID, null);
        given(authorisationChainResponsibilityService.apply(any(AisAuthorisationProcessorRequest.class)))
            .willReturn(processorResponse);

        String requestUrl = CONTEXT_PATH + UrlBuilder.buildConsentCreation();
        MockHttpServletRequestBuilder requestBuilder = post(requestUrl);
        requestBuilder.headers(headers);
        requestBuilder.content(resourceToString(CREATE_CONSENT_REQUEST_JSON_PATH, UTF_8));
        requestBuilder.contextPath(CONTEXT_PATH);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(responseJsonPath, UTF_8)));
    }
}
