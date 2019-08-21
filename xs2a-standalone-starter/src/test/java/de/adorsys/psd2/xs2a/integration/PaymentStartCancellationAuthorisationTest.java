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
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles({"integration-test", "mock-qwac"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = Xs2aStandaloneStarter.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    ObjectMapperConfig.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class
})
public class PaymentStartCancellationAuthorisationTest {
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final ScaStatus PIS_AUTHORISATION_SCA_STATUS = ScaStatus.PSUIDENTIFIED;
    private static final String PAYMENT_ID = "JKDQjM02y1a9G7_kLTgAy8HJCLIOYVWZ-fQThyI7gYhZvqcmJ6kZg7CmJFgTANLhcgftJbETkzvNvu5mZQqWcA==_=_psGLvQpt9Q";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private static final String PSU_ID = "PSU-123";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String AUTHORISATION_RESPONSE = "/json/payment/cancellation/res/explicit/cancellation_authorisation_response.json";
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

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
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @Captor
    private ArgumentCaptor<CreatePisAuthorisationRequest> createPisAuthorisationRequestCaptor;


    private HttpHeaders httpHeadersExplicit = new HttpHeaders();

    @Before
    public void setUp() {
        httpHeadersExplicit.add("Content-Type", "application/json");
        httpHeadersExplicit.add("tpp-qwac-certificate", "qwac certificate");
        httpHeadersExplicit.add("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        httpHeadersExplicit.add("PSU-ID", PSU_ID);
        httpHeadersExplicit.add("TPP-Redirect-URI", TPP_REDIRECT_URI);
        httpHeadersExplicit.add("TPP-Nok-Redirect-URI", TPP_NOK_REDIRECT_URI);

        given(tppService.getTppInfo()).willReturn(TPP_INFO);
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo())).willReturn(false);
        given(aspspProfileService.getAspspSettings()).willReturn(AspspSettingsBuilder.buildAspspSettings());
    }

    @Test
    public void startPaymentCancellationAuthorisation_success() throws Exception {
        //Given
        given(eventServiceEncrypted.recordEvent(any(EventBO.class))).willReturn(true);
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .willReturn(Optional.of(buildPisCommonPaymentResponse()));

        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(ScaApproach.EMBEDDED));
        given(pisCommonPaymentServiceEncrypted.createAuthorizationCancellation(eq(PAYMENT_ID), createPisAuthorisationRequestCaptor.capture()))
            .willReturn(Optional.of(new CreatePisAuthorisationResponse(AUTHORISATION_ID, PIS_AUTHORISATION_SCA_STATUS)));

        given(pisCommonPaymentServiceEncrypted.getPisCancellationAuthorisationById(AUTHORISATION_ID))
            .willReturn(Optional.of(buildGetPisAuthorisationResponse()));
        given(pisCommonPaymentServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CANCELLED))
            .willReturn(Optional.of(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)));

        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.buildPaymentStartCancellationAuthorisationUrl(
            SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, PAYMENT_ID));
        requestBuilder.headers(httpHeadersExplicit);

        CreatePisAuthorisationRequest expectedCreatePisAuthorisationRequest =
            new CreatePisAuthorisationRequest(PaymentAuthorisationType.CANCELLED,
                                              new PsuIdData(PSU_ID, null, null, null),
                                              ScaApproach.EMBEDDED, TPP_REDIRECT_URIs);

        //When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(AUTHORISATION_RESPONSE, UTF_8)));

        assertEquals(expectedCreatePisAuthorisationRequest, createPisAuthorisationRequestCaptor.getValue());
    }

    @NotNull
    private GetPisAuthorisationResponse buildGetPisAuthorisationResponse() {
        GetPisAuthorisationResponse getPisAuthorisationResponse = new GetPisAuthorisationResponse();
        getPisAuthorisationResponse.setScaStatus(PIS_AUTHORISATION_SCA_STATUS);
        getPisAuthorisationResponse.setPaymentType(SINGLE_PAYMENT_TYPE);
        getPisAuthorisationResponse.setPaymentProduct(SEPA_PAYMENT_PRODUCT);
        PisPaymentInfo paymentInfo = new PisPaymentInfo();
        paymentInfo.setPaymentId(PAYMENT_ID);
        getPisAuthorisationResponse.setPaymentInfo(paymentInfo);
        return getPisAuthorisationResponse;
    }

    @NotNull
    private PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(TPP_INFO);
        pisCommonPaymentResponse.setPaymentType(SINGLE_PAYMENT_TYPE);
        pisCommonPaymentResponse.setPaymentProduct(SEPA_PAYMENT_PRODUCT);
        return pisCommonPaymentResponse;
    }
}
