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

package de.adorsys.psd2.xs2a.integration.consent;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import de.adorsys.xs2a.reader.JsonReader;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration-test", "mock-qwac"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class
})
public class UpdatePsuDataForConsentIT {

    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String WRONG_AUTHORISATION_ID = "q3356ea7-8e3e-474f-b5ea-2b89346cb6jk";
    private static final String ENCRYPTED_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String PSU_ID = "PSU-123";
    private static final String PSU_PASS = "12345";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @MockBean
    private AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    @MockBean
    private AisConsentSpi aisConsentSpi;

    private JsonReader jsonReader = new JsonReader();

    private HttpHeaders httpHeaders = new HttpHeaders();

    @Before
    public void setUp() {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        httpHeaders.add("PSU-ID", PSU_ID);

        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo()))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(aspspProfileService.getAspspSettings()).willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(aspspProfileService.getScaApproaches())
            .willReturn(Collections.singletonList(ScaApproach.REDIRECT));
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    @Test
    public void updatePsuData_success() throws Exception {
        given(eventServiceEncrypted.recordEvent(any(EventBO.class))).willReturn(true);
        AisConsentAuthorizationResponse authorizationResponse = new AisConsentAuthorizationResponse();
        authorizationResponse.setScaStatus(ScaStatus.PSUIDENTIFIED);
        authorizationResponse.setChosenScaApproach(ScaApproach.EMBEDDED);
        given(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, ENCRYPTED_CONSENT_ID))
            .willReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(authorizationResponse)
                            .build());
        given(aisConsentServiceEncrypted.getAisAccountConsentById(ENCRYPTED_CONSENT_ID))
            .willReturn(CmsResponse.<AisAccountConsent>builder()
                            .payload(buildAisAccountConsent())
                            .build());

        given(aisConsentAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED))
                            .build());
        given(aisConsentSpi.authorisePsu(any(SpiContextData.class), any(SpiPsuData.class), eq(PSU_PASS), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                            .build());
        given(aisConsentSpi.requestAvailableScaMethods(any(SpiContextData.class), any(SpiAccountConsent.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(new SpiAvailableScaMethodsResponse(Collections.singletonList(new SpiAuthenticationObject())))
                            .build());
        given(aisConsentSpi.requestAuthorisationCode(any(SpiContextData.class), isNull(), any(), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(new SpiAuthorizationCodeResult())
                            .build());
        given(aisConsentAuthorisationServiceEncrypted.updateConsentAuthorization(eq(AUTHORISATION_ID), any(AisConsentAuthorizationRequest.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        given(aisConsentAuthorisationServiceEncrypted.saveAuthenticationMethods(eq(AUTHORISATION_ID), anyList()))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.buildConsentUpdateAuthorisationUrl(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID));
        requestBuilder.content(jsonReader.getStringFromFile("json/auth/req/update_password.json"));
        requestBuilder.headers(httpHeaders);

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/res/update_psu_data_consent_resp.json")));
    }

    @Test
    public void updatePsuData_wrongAuthorisationId() throws Exception {
        given(eventServiceEncrypted.recordEvent(any(EventBO.class))).willReturn(true);
        given(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(WRONG_AUTHORISATION_ID, ENCRYPTED_CONSENT_ID))
            .willReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(new AisConsentAuthorizationResponse())
                            .build());
        given(aisConsentServiceEncrypted.getAisAccountConsentById(ENCRYPTED_CONSENT_ID))
            .willReturn(CmsResponse.<AisAccountConsent>builder()
                            .payload(buildAisAccountConsent())
                            .build());

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.buildConsentUpdateAuthorisationUrl(ENCRYPTED_CONSENT_ID, WRONG_AUTHORISATION_ID));
        requestBuilder.content(jsonReader.getStringFromFile("json/auth/req/update_password.json"));
        requestBuilder.headers(httpHeaders);

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(jsonReader.getStringFromFile("json/auth/res/403_resource_unknown.json")));
    }

    @NotNull
    private AisAccountConsent buildAisAccountConsent() {
        AisAccountConsent aisAccountConsent = new AisAccountConsent();
        aisAccountConsent.setTppAccess(new AisAccountAccess(Collections.emptyList(),
                                                            Collections.emptyList(),
                                                            Collections.emptyList(),
                                                            null, null, null, null));
        aisAccountConsent.setAspspAccess(new AisAccountAccess(Collections.emptyList(),
                                                              Collections.emptyList(),
                                                              Collections.emptyList(),
                                                              null, null, null, null));
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null);
        aisAccountConsent.setAccountConsentAuthorizations(Collections.singletonList(
            new AisAccountConsentAuthorisation(AUTHORISATION_ID, psuIdData, ScaStatus.PSUIDENTIFIED)));
        aisAccountConsent.setTppInfo(TPP_INFO);
        aisAccountConsent.setConsentStatus(ConsentStatus.VALID);
        return aisAccountConsent;
    }
}
