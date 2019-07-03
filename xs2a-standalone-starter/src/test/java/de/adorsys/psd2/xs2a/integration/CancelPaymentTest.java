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
import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.service.EventServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.PisCommonPaymentResponseBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.SpiPaymentCancellationResponseBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import org.apache.commons.io.IOUtils;
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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    Xs2aInterfaceConfig.class
})
public class CancelPaymentTest {
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String ENCRYPTED_PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private static final ScaApproach REDIRECT_SCA_APPROACH = ScaApproach.REDIRECT;
    private static final ScaApproach EMBEDDED_SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData("data".getBytes(), ENCRYPTED_PAYMENT_ID);
    private HttpHeaders httpHeadersExplicit = new HttpHeaders();

    private static final String REDIRECT_EXPLICIT_CANCELLATION_RESP = "/json/payment/res/explicit/SinglePaymentCancellation_redirect_explicit_response.json";
    private static final String EMBEDDED_EXPLICIT_CANCELLATION_RESP = "/json/payment/res/explicit/SinglePaymentCancellation_embedded_explicit_response.json";

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
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;
    @MockBean
    private PaymentCancellationSpi paymentCancellationSpi;
    @MockBean
    private UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiServiceEncrypted;

    @Before
    public void init() {
        // common actions for all tests
        given(aspspProfileService.getAspspSettings())
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppService.getTppInfo()).willReturn(TPP_INFO);
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.buildTppUniqueParamsHolder()))
            .willReturn(false);
        given(eventServiceEncrypted.recordEvent(any(Event.class)))
            .willReturn(true);
        given(updatePaymentStatusAfterSpiServiceEncrypted.updatePaymentStatus(eq(ENCRYPTED_PAYMENT_ID), any(TransactionStatus.class)))
            .willReturn(true);
        given(aspspDataService.readAspspConsentData(ENCRYPTED_PAYMENT_ID)).willReturn(Optional.of(ASPSP_CONSENT_DATA));

        PisCommonPaymentResponse pisCommonPaymentResponse = PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponseWithPayment();
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPTED_PAYMENT_ID))
            .willReturn(Optional.of(pisCommonPaymentResponse));

        httpHeadersExplicit.add("Content-Type", "application/json");
        httpHeadersExplicit.add("tpp-qwac-certificate", "qwac certificate");
        httpHeadersExplicit.add("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        httpHeadersExplicit.add("PSU-ID", "PSU-123");
        httpHeadersExplicit.add("PSU-ID-Type", "Some type");
        httpHeadersExplicit.add("PSU-Corporate-ID", "Some corporate id");
        httpHeadersExplicit.add("PSU-Corporate-ID-Type", "Some corporate id type");
        httpHeadersExplicit.add("PSU-IP-Address", "1.1.1.1");
        httpHeadersExplicit.add("TPP-Redirect-URI", "ok.uri");
        httpHeadersExplicit.add("TPP-Nok-Redirect-URI", "nok.uri");

        // when we use Explicit auth mode we need to set 'true' and value 'signingBasketSupported' in profile also should be 'true'
        httpHeadersExplicit.add("TPP-Explicit-Authorisation-Preferred", "true");
    }

    @Test
    public void cancelPayment_explicit_redirect_withMandatedAuthorisation_successful() throws Exception {
        // Given
        aspspProfileService.getAspspSettings().setSigningBasketSupported(true);
        cancelPayment_withMandatedAuthorisation_successful(httpHeadersExplicit, REDIRECT_SCA_APPROACH, REDIRECT_EXPLICIT_CANCELLATION_RESP);
    }

    @Test
    public void cancelPayment_explicit_embedded_withMandatedAuthorisation_successful() throws Exception {
        // Given
        cancelPayment_withNotMandatedAuthorisation_successful(httpHeadersExplicit, EMBEDDED_SCA_APPROACH);
    }

    @Test
    public void cancelPayment_explicit_redirect_withNotMandatedAuthorisation_successful() throws Exception {
        // Given
        cancelPayment_withNotMandatedAuthorisation_successful(httpHeadersExplicit, REDIRECT_SCA_APPROACH);
    }

    @Test
    public void cancelPayment_explicit_embedded_withNotMandatedAuthorisation_successful() throws Exception {
        // Given
        aspspProfileService.getAspspSettings().setSigningBasketSupported(true);
        cancelPayment_withMandatedAuthorisation_successful(httpHeadersExplicit, EMBEDDED_SCA_APPROACH, EMBEDDED_EXPLICIT_CANCELLATION_RESP);
    }

    private void cancelPayment_withMandatedAuthorisation_successful(HttpHeaders httpHeaders, ScaApproach scaApproach, String responseJsonPath) throws Exception {
        // Given
        SpiPaymentCancellationResponse spiPaymentCancellationResponse = SpiPaymentCancellationResponseBuilder.buildSpiPaymentCancellationResponse(true);
        SpiResponse<SpiPaymentCancellationResponse> spiResponse = SpiResponse.<SpiPaymentCancellationResponse>builder()
                                                                      .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                      .payload(spiPaymentCancellationResponse)
                                                                      .success();
        given(paymentCancellationSpi.initiatePaymentCancellation(any(), any(), any()))
            .willReturn(spiResponse);

        given(aspspProfileService.getScaApproaches())
            .willReturn(Collections.singletonList(scaApproach));

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.buildCancellationPaymentUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPTED_PAYMENT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isAccepted())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(responseJsonPath, UTF_8)));
    }

    public void cancelPayment_withNotMandatedAuthorisation_successful(HttpHeaders httpHeaders, ScaApproach scaApproach) throws Exception {
        // Given
        given(aspspDataService.readAspspConsentData(ENCRYPTED_PAYMENT_ID)).willReturn(Optional.of(ASPSP_CONSENT_DATA));

        SpiPaymentCancellationResponse spiPaymentCancellationResponse = SpiPaymentCancellationResponseBuilder.buildSpiPaymentCancellationResponse(false);
        SpiResponse<SpiPaymentCancellationResponse> spiInitiatePaymentCancellationResponse = SpiResponse.<SpiPaymentCancellationResponse>builder()
                                                                                                 .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                                                 .payload(spiPaymentCancellationResponse)
                                                                                                 .success();
        given(paymentCancellationSpi.initiatePaymentCancellation(any(), any(), any())).willReturn(spiInitiatePaymentCancellationResponse);

        SpiResponse<SpiResponse.VoidResponse> spiCancelPaymentResponse = SpiResponse.<SpiResponse.VoidResponse>builder()
                                                                             .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                             .payload(SpiResponse.voidResponse())
                                                                             .success();

        given(paymentCancellationSpi.cancelPaymentWithoutSca(any(), any(), any())).willReturn(spiCancelPaymentResponse);

        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(scaApproach));

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.buildCancellationPaymentUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPTED_PAYMENT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isNoContent())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
    }
}
