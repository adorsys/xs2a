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


import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.starter.config.validation.PaymentValidationConfigImpl;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.PsuIdDataBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.SpiPaymentInitiationResponseBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
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
    Xs2aInterfaceConfig.class,
    PaymentValidationConfigImpl.class
})
public class InitiateCustomPayment_IT {
    private static final Charset UTF_8 = Charset.forName("utf-8");

    private static final String SINGLE_PAYMENT_CUSTOM_REQUEST_JSON_PATH = "/json/payment/req/SinglePaymentCustomInitiate_request.json";

    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;

    private static final String CUSTOM_PAYMENT_PRODUCT = "custom-payment";
    private static final String ENCRYPT_PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();

    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;

    private HttpHeaders httpHeadersExplicit = new HttpHeaders();
    private MultiKeyMap responseMap = new MultiKeyMap();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @MockBean
    private SinglePaymentSpi singlePaymentSpi;

    @MockBean
    @Qualifier("consentRestTemplate")
    private RestTemplate consentRestTemplate;

    @Before
    public void init() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("tpp-qwac-certificate", "qwac certificate");
        headerMap.put("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("PSU-ID", "PSU-123");
        headerMap.put("PSU-ID-Type", "Some type");
        headerMap.put("PSU-Corporate-ID", "Some corporate id");
        headerMap.put("PSU-Corporate-ID-Type", "Some corporate id type");
        headerMap.put("PSU-IP-Address", "1.1.1.1");
        headerMap.put("TPP-Redirect-URI", TPP_REDIRECT_URI);
        headerMap.put("TPP-NOK-Redirect-URI", TPP_NOK_REDIRECT_URI);

        httpHeadersExplicit.setAll(headerMap);
        // when we use Explicit auth mode we need to set 'true' and value 'signingBasketSupported' in profile also should be 'true'
        httpHeadersExplicit.add("TPP-Explicit-Authorisation-Preferred", "true");

        responseMap.put(PaymentType.SINGLE, ScaApproach.EMBEDDED, "/json/payment/res/explicit/SinglePaymentCustomInitiate_embedded_explicit_response.json");
        responseMap.put(PaymentType.SINGLE, ScaApproach.REDIRECT, "/json/payment/res/explicit/SinglePaymentCustomInitiate_redirect_explicit_response.json");

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

        given(pisCommonPaymentServiceEncrypted.createCommonPayment(any(PisPaymentInfo.class)))
            .willReturn(Optional.of(new CreatePisCommonPaymentResponse(ENCRYPT_PAYMENT_ID)));
    }

    @Test
    public void initiateSinglePaymentCustom_explicit_embedded_successful() throws Exception {
        given(pisCommonPaymentServiceEncrypted.createAuthorization(ENCRYPT_PAYMENT_ID, getPisAuthorisationRequest(ScaApproach.EMBEDDED)))
            .willReturn(Optional.of(new CreatePisAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS)));
        initiateSinglePaymentCustom_successful(httpHeadersExplicit, ScaApproach.EMBEDDED);
    }

    @Test
    public void initiateSinglePaymentCustom_explicit_redirect_successful() throws Exception {
        given(pisCommonPaymentServiceEncrypted.createAuthorization(ENCRYPT_PAYMENT_ID, getPisAuthorisationRequest(ScaApproach.REDIRECT)))
            .willReturn(Optional.of(new CreatePisAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS)));
        initiateSinglePaymentCustom_successful(httpHeadersExplicit, ScaApproach.REDIRECT);
    }

    private CreatePisAuthorisationRequest getPisAuthorisationRequest(ScaApproach scaApproach) {
        return new CreatePisAuthorisationRequest(PaymentAuthorisationType.CREATED, PsuIdDataBuilder.buildPsuIdData(), scaApproach, TPP_REDIRECT_URIs);
    }

    private void initiateSinglePaymentCustom_successful(HttpHeaders headers, ScaApproach scaApproach) throws Exception {
        // Given
        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(scaApproach));

        given(singlePaymentSpi.initiatePayment(any(SpiContextData.class), any(SpiSinglePayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiPaymentInitiationResponseBuilder.buildSinglePaymentResponse(false));
        given(consentRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(), any(Class.class), any(String.class)))
            .willReturn(ResponseEntity.ok(Boolean.TRUE));
        given(pisCommonPaymentServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .willReturn(Optional.of(new AuthorisationScaApproachResponse(scaApproach)));

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(UrlBuilder.buildInitiatePaymentUrl(SINGLE_PAYMENT_TYPE.getValue(), CUSTOM_PAYMENT_PRODUCT));
        requestBuilder.headers(headers);
        requestBuilder.content(IOUtils.resourceToString(SINGLE_PAYMENT_CUSTOM_REQUEST_JSON_PATH, UTF_8));

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        String filePath = (String) responseMap.get(PaymentType.SINGLE, scaApproach);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(filePath, UTF_8)));
    }
}
