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
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.*;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles({"integration-test", "mock-qwac"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(
    classes = {
        CorsConfigurationProperties.class,
        WebConfig.class,
        Xs2aEndpointPathConstant.class,
        Xs2aInterfaceConfig.class
    })
class ConsentUpdateAuthorisationIT {
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

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;

    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @MockBean
    private ConsentServiceEncrypted aisConsentService;

    @BeforeEach
    void setUp() {
        given(aspspProfileService.getAspspSettings(null)).willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppStopListService.checkIfTppBlocked(TPP_INFO.getAuthorisationNumber(), null))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(aspspProfileService.getScaApproaches(null))
            .willReturn(Collections.singletonList(ScaApproach.REDIRECT));
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    @Test
    void updateConsentPsuData_failed_psu_authorisation_psu_request_are_different() throws Exception {
        //When
        ResultActions resultActions = updateConsentPsuDataAndGetResultActions(PSU_ID_1, PSU_ID_2);

        //Then
        resultActions
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(PSU_CREDENTIALS_INVALID_RESP, UTF_8)));
    }

    @Test
    void updateConsentPsuData_failed_no_psu_authorisation_no_psu_request() throws Exception {
        //When
        ResultActions resultActions = updateConsentPsuDataAndGetResultActions(null, null);

        //Then
        resultActions
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(FORMAT_ERROR_RESP, UTF_8)));
    }

    private ResultActions updateConsentPsuDataAndGetResultActions(String psuIdAuthorisation, String psuIdHeader) throws Exception {
        //Given
        String request = IOUtils.resourceToString(AUTH_REQ, UTF_8);
        ScaApproach scaApproach = ScaApproach.EMBEDDED;

        PsuIdData psuIdDataAuthorisation = buildPsuIdDataAuthorisation(psuIdAuthorisation);
        HttpHeadersMock httpHeaders = buildHttpHeaders(psuIdHeader);

        Authorisation authorisation = new Authorisation(AUTHORISATION_ID, psuIdDataAuthorisation, CONSENT_ID, AuthorisationType.CONSENT, ScaStatus.RECEIVED);
        CmsConsent cmsConsent = CmsConsentBuilder.buildCmsConsent(CONSENT_PATH, scaApproach, CONSENT_ID, xs2aObjectMapper, authorisation);


        given(aisConsentService.getConsentById(CONSENT_ID))
            .willReturn(CmsResponse.<CmsConsent>builder()
                            .payload(cmsConsent)
                            .build());
        given(authorisationServiceEncrypted.getAuthorisationById(any(String.class)))
            .willReturn(CmsResponse.<Authorisation>builder()
                            .payload(AisConsentAuthorizationResponseBuilder.buildAisConsentAuthorizationResponse(scaApproach, psuIdDataAuthorisation))
                            .build());
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(any(String.class)))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(scaApproach))
                            .build());

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.buildConsentUpdateAuthorisationUrl(CONSENT_ID, AUTHORISATION_ID));
        requestBuilder.headers(httpHeaders);
        requestBuilder.content(request);

        return mockMvc.perform(requestBuilder);
    }

    private HttpHeadersMock buildHttpHeaders(String psuIdHeader) {
        HttpHeadersMock httpHeadersBase = HttpHeadersBuilder.buildHttpHeaders();
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
