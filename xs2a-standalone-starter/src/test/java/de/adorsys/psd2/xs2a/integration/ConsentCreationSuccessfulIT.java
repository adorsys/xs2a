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
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.AisConsentAuthorizationResponseBuilder;
import de.adorsys.psd2.xs2a.integration.builder.ais.CmsConsentBuilder;
import de.adorsys.xs2a.reader.JsonReader;
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
import java.util.HashMap;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode.EXPLICIT;
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
class ConsentCreationSuccessfulIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String DEDICATED_CONSENT_REQUEST_JSON_PATH = "/json/account/req/DedicatedConsent.json";
    private static final String BANK_OFFERED_CONSENT_REQUEST_JSON_PATH = "/json/account/req/BankOfferedConsent.json";
    private static final String GLOBAL_CONSENT_REQUEST_JSON_PATH = "/json/account/req/GlobalConsent.json";
    private static final String ALL_AVAILABLE_ACCOUNT_CONSENT_REQUEST_JSON_PATH = "/json/account/req/AllAvailableAccountConsent.json";
    private static final String ALL_AVAILABLE_WITH_BALANCES_ACCOUNT_CONSENT_REQUEST_JSON_PATH = "/json/account/req/AllAvailableWithBalancesAccountConsent.json";
    private static final String CREATE_CONSENT_IMPLICIT_EMBEDDED_RESPONSE_PATH = "/json/account/res/CreateAisConsent_implicit_embedded_response.json";
    private static final String CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_PATH = "/json/account/res/CreateAisConsent_implicit_redirect_response.json";
    private static final String CREATE_CONSENT_IMPLICIT_REDIRECT_OAUTH_RESPONSE_PATH = "/json/account/res/CreateAisConsent_implicit_redirect_oauth_response.json";
    private static final String CREATE_CONSENT_EXPLICIT_EMBEDDED_RESPONSE_PATH = "/json/account/res/CreateAisConsent_explicit_embedded_response.json";
    private static final String CREATE_CONSENT_EXPLICIT_REDIRECT_RESPONSE_PATH = "/json/account/res/CreateAisConsent_explicit_redirect_response.json";
    private static final String CREATE_CONSENT_EXPLICIT_EMBEDDED_SIGNING_BASKET_RESPONSE_PATH = "/json/account/res/CreateAisConsent_explicit_redirect_response.json";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    private static final String ENCRYPT_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";

    private final HttpHeaders httpHeadersImplicit = new HttpHeaders();
    private final HttpHeaders httpHeadersExplicit = new HttpHeaders();

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

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void init() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("PSU-ID", "PSU-123");
        headerMap.put("PSU-ID-Type", "Some type");
        headerMap.put("PSU-Corporate-ID", "Some corporate id");
        headerMap.put("PSU-Corporate-ID-Type", "Some corporate id type");
        headerMap.put("PSU-IP-Address", "1.1.1.1");
        headerMap.put("TPP-Redirect-URI", "ok.uri");

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
    void creation_dedicated_consent_implicit_embedded_successful() throws Exception {
        consentCreation_successful(httpHeadersImplicit, ScaApproach.EMBEDDED, DEDICATED_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_IMPLICIT_EMBEDDED_RESPONSE_PATH);
    }

    @Test
    void creation_dedicated_consent_implicit_redirect_successful() throws Exception {
        consentCreation_successful(httpHeadersImplicit, ScaApproach.REDIRECT, DEDICATED_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_PATH);
    }

    @Test
    void creation_dedicated_consent_implicit_redirect_oauth_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithScaRedirectFlow(ScaRedirectFlow.OAUTH));
        consentCreation_successful(httpHeadersImplicit, ScaApproach.REDIRECT, DEDICATED_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_IMPLICIT_REDIRECT_OAUTH_RESPONSE_PATH);
    }

    @Test
    void creation_global_consent_implicit_embedded_successful() throws Exception {
        consentCreation_successful(httpHeadersImplicit, ScaApproach.EMBEDDED, GLOBAL_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_IMPLICIT_EMBEDDED_RESPONSE_PATH);
    }

    @Test
    void creation_global_consent_implicit_redirect_successful() throws Exception {
        consentCreation_successful(httpHeadersImplicit, ScaApproach.REDIRECT, GLOBAL_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_PATH);
    }

    @Test
    void creation_bank_offered_consent_implicit_redirect_successful() throws Exception {
        consentCreation_successful(httpHeadersImplicit, ScaApproach.REDIRECT, BANK_OFFERED_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_PATH);
    }

    @Test
    void creation_all_available_account_consent_implicit_embedded_successful() throws Exception {
        consentCreation_successful(httpHeadersImplicit, ScaApproach.EMBEDDED, ALL_AVAILABLE_ACCOUNT_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_IMPLICIT_EMBEDDED_RESPONSE_PATH);
    }

    @Test
    void creation_all_available_account_consent_implicit_redirect_successful() throws Exception {
        consentCreation_successful(httpHeadersImplicit, ScaApproach.REDIRECT, ALL_AVAILABLE_ACCOUNT_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_PATH);
    }

    @Test
    void creation_all_available_with_balances_account_consent_implicit_redirect_successful() throws Exception {
        consentCreation_successful(httpHeadersImplicit, ScaApproach.REDIRECT, ALL_AVAILABLE_WITH_BALANCES_ACCOUNT_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_IMPLICIT_REDIRECT_RESPONSE_PATH);
    }

    // =============== EXPLICIT MODE
    //
    @Test
    void creation_dedicated_consent_explicit_embedded_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationMode(EXPLICIT));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.EMBEDDED, DEDICATED_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_EMBEDDED_RESPONSE_PATH);
    }

    @Test
    void creation_dedicated_consent_explicit_embedded_signingBasket_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationModeAndSigningBasketSupported(EXPLICIT, true));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.EMBEDDED, DEDICATED_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_EMBEDDED_SIGNING_BASKET_RESPONSE_PATH);
    }

    @Test
    void creation_dedicated_consent_explicit_redirect_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationMode(EXPLICIT));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.REDIRECT, DEDICATED_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_REDIRECT_RESPONSE_PATH);
    }

    @Test
    void creation_global_consent_explicit_embedded_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationMode(EXPLICIT));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.EMBEDDED, GLOBAL_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_EMBEDDED_RESPONSE_PATH);
    }

    @Test
    void creation_global_consent_explicit_embedded_signingBasket_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationModeAndSigningBasketSupported(EXPLICIT, true));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.EMBEDDED, GLOBAL_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_EMBEDDED_SIGNING_BASKET_RESPONSE_PATH);
    }

    @Test
    void creation_global_consent_explicit_redirect_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationMode(EXPLICIT));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.REDIRECT, GLOBAL_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_REDIRECT_RESPONSE_PATH);
    }

    @Test
    void creation_bank_offered_consent_explicit_redirect_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationMode(EXPLICIT));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.REDIRECT, BANK_OFFERED_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_REDIRECT_RESPONSE_PATH);
    }

    @Test
    void creation_all_available_account_consent_explicit_embedded_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationMode(EXPLICIT));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.EMBEDDED, ALL_AVAILABLE_ACCOUNT_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_EMBEDDED_RESPONSE_PATH);
    }

    @Test
    void creation_all_available_account_consent_explicit_embedded_signingBasket_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationModeAndSigningBasketSupported(EXPLICIT, true));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.EMBEDDED, ALL_AVAILABLE_ACCOUNT_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_EMBEDDED_SIGNING_BASKET_RESPONSE_PATH);
    }

    @Test
    void creation_all_available_account_consent_explicit_redirect_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationMode(EXPLICIT));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.REDIRECT, ALL_AVAILABLE_ACCOUNT_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_REDIRECT_RESPONSE_PATH);
    }

    @Test
    void creation_all_available_with_balances_account_consent_explicit_redirect_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithStartAuthorisationMode(EXPLICIT));
        consentCreation_successful(httpHeadersExplicit, ScaApproach.REDIRECT, ALL_AVAILABLE_WITH_BALANCES_ACCOUNT_CONSENT_REQUEST_JSON_PATH, CREATE_CONSENT_EXPLICIT_REDIRECT_RESPONSE_PATH);
    }

    private void consentCreation_successful(HttpHeaders headers, ScaApproach scaApproach, String requestJsonPath, String responseJsonPath) throws Exception {
        // Given
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(scaApproach));
        given(authorisationServiceEncrypted.createAuthorisation(any(AuthorisationParentHolder.class), any(CreateAuthorisationRequest.class)))
            .willReturn(CmsResponse.<CreateAuthorisationResponse>builder()
                            .payload(buildCreateAisConsentAuthorizationResponse())
                            .build());
        CmsConsent cmsConsent = CmsConsentBuilder.buildCmsConsent(requestJsonPath, scaApproach, ENCRYPT_CONSENT_ID, xs2aObjectMapper);
        given(consentServiceEncrypted.createConsent(any(CmsConsent.class)))
            .willReturn(CmsResponse.<CmsCreateConsentResponse>builder()
                            .payload(new CmsCreateConsentResponse(ENCRYPT_CONSENT_ID, cmsConsent))
                            .build());
        given(aisConsentServiceEncrypted.updateAspspAccountAccess(eq(ENCRYPT_CONSENT_ID), any(AccountAccess.class)))
            .willReturn(CmsResponse.<CmsConsent>builder()
                            .payload(cmsConsent)
                            .build());
        given(consentServiceEncrypted.getConsentById(any(String.class)))
            .willReturn(CmsResponse.<CmsConsent>builder()
                            .payload(cmsConsent)
                            .build());
        given(authorisationServiceEncrypted.getAuthorisationById(any(String.class)))
            .willReturn(CmsResponse.<Authorisation>builder()
                            .payload(AisConsentAuthorizationResponseBuilder.buildAisConsentAuthorizationResponse(scaApproach))
                            .build());
        given(aspspDataService.readAspspConsentData(any(String.class)))
            .willReturn(Optional.of(new AspspConsentData(null, ENCRYPT_CONSENT_ID)));
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(scaApproach))
                            .build());

        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.buildConsentCreation());
        requestBuilder.headers(headers);
        requestBuilder.content(resourceToString(requestJsonPath, UTF_8));

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(responseJsonPath, UTF_8)));
    }

    private CreateAuthorisationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, INTERNAL_REQUEST_ID, null, ScaApproach.EMBEDDED);
    }
}
