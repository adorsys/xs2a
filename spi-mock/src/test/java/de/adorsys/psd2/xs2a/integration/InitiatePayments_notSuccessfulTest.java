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
import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.SpiPaymentInitiationResponseBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
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
public class InitiatePayments_notSuccessfulTest {
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String SINGLE_PAYMENT_REQUEST_JSON_PATH = "/json/payment/req/SinglePaymentInitiate_request.json";
    private static final String PSU_IP_ADDRESS_MISSING_ERROR_MESSAGE_JSON_PATH = "/json/account/res/PsuIpAddressMissingErrorMessage.json";
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;

    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private static final String INITIATE_SINGLE_PAYMENT_URL = UrlBuilder.buildInitiatePaymentUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT);

    private HttpHeaders httpHeadersWithoutPsuIpAddress = new HttpHeaders();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
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
        httpHeadersWithoutPsuIpAddress.setAll(headerMap);

        given(aspspProfileService.getAspspSettings())
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppService.getTppInfo())
            .willReturn(TPP_INFO);
        given(tppService.getTppId())
            .willReturn(TPP_INFO.getAuthorisationNumber());
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.buildTppUniqueParamsHolder()))
            .willReturn(false);
    }

    @Test
    public void initiateSinglePayment_withoutPsuIpAddress_notSuccessful() throws Exception {
        //Given
        makePreparations(ScaApproach.EMBEDDED);
        MockHttpServletRequestBuilder requestBuilder = makeRequestBuilder(INITIATE_SINGLE_PAYMENT_URL, httpHeadersWithoutPsuIpAddress, IOUtils.resourceToString(SINGLE_PAYMENT_REQUEST_JSON_PATH, UTF_8));
        //When
        ResultActions resultActions = mockMvc.perform(requestBuilder);
        //Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(IOUtils.resourceToString(PSU_IP_ADDRESS_MISSING_ERROR_MESSAGE_JSON_PATH, UTF_8)));
    }

    private void makePreparations(ScaApproach scaApproach) {
        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(scaApproach));
        given(singlePaymentSpi.initiatePayment(any(SpiContextData.class), any(SpiSinglePayment.class), any(AspspConsentData.class)))
            .willReturn(SpiPaymentInitiationResponseBuilder.buildSinglePaymentResponse());
        given(consentRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class), any(String.class)))
            .willReturn(ResponseEntity.ok(Void.class));
        given(pisCommonPaymentServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .willReturn(Optional.of(new AuthorisationScaApproachResponse(scaApproach)));
    }

    private MockHttpServletRequestBuilder makeRequestBuilder(String url, HttpHeaders headers, String content) {
        return MockMvcRequestBuilders.post(url)
                   .headers(headers)
                   .content(content);
    }
}

