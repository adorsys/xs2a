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
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.PisCommonPaymentResponseBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.SpiPaymentCancellationResponseBuilder;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class CancelPaymentIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String ENCRYPTED_PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final ScaApproach REDIRECT_SCA_APPROACH = ScaApproach.REDIRECT;
    private static final ScaApproach EMBEDDED_SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData("data".getBytes(), ENCRYPTED_PAYMENT_ID);
    private final HttpHeaders httpHeadersExplicit = new HttpHeaders();

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
    private Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;
    @MockBean
    private PaymentCancellationSpi paymentCancellationSpi;
    @MockBean
    private UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiServiceEncrypted;
    @MockBean
    @Qualifier("consentRestTemplate")
    private RestTemplate consentRestTemplate;

    @BeforeEach
    void init() {
        // common actions for all tests
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo(), null))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(eventServiceEncrypted.recordEvent(any(EventBO.class)))
            .willReturn(true);
        given(updatePaymentStatusAfterSpiServiceEncrypted.updatePaymentStatus(eq(ENCRYPTED_PAYMENT_ID), any(TransactionStatus.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        given(updatePaymentStatusAfterSpiServiceEncrypted.updateInternalPaymentStatus(eq(ENCRYPTED_PAYMENT_ID), any(InternalPaymentStatus.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        given(updatePaymentStatusAfterSpiServiceEncrypted.updatePaymentCancellationTppRedirectUri(eq(ENCRYPTED_PAYMENT_ID), any(TppRedirectUri.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        given(updatePaymentStatusAfterSpiServiceEncrypted.updatePaymentCancellationInternalRequestId(eq(ENCRYPTED_PAYMENT_ID), anyString()))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(aspspDataService.readAspspConsentData(ENCRYPTED_PAYMENT_ID)).willReturn(Optional.of(ASPSP_CONSENT_DATA));

        PisCommonPaymentResponse pisCommonPaymentResponse = PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponseWithPayment();
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPTED_PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(pisCommonPaymentResponse)
                            .build());
        given(consentRestTemplate.postForEntity(anyString(), any(EventBO.class), eq(Boolean.class)))
            .willReturn(new ResponseEntity<>(true, HttpStatus.OK));
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        httpHeadersExplicit.add("Content-Type", "application/json");
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
    void cancelPayment_explicit_redirect_withMandatedAuthorisation_successful() throws Exception {
        // Given
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithSigningBasketSupported(true));
        cancelPayment_withMandatedAuthorisation_successful(httpHeadersExplicit, REDIRECT_SCA_APPROACH, REDIRECT_EXPLICIT_CANCELLATION_RESP);
    }

    @Test
    void cancelPayment_explicit_embedded_withMandatedAuthorisation_successful() throws Exception {
        // Given
        cancelPayment_withNotMandatedAuthorisation_successful(httpHeadersExplicit, EMBEDDED_SCA_APPROACH);
    }

    @Test
    void cancelPayment_explicit_redirect_withNotMandatedAuthorisation_successful() throws Exception {
        // Given
        cancelPayment_withNotMandatedAuthorisation_successful(httpHeadersExplicit, REDIRECT_SCA_APPROACH);
    }

    @Test
    void cancelPayment_explicit_embedded_withNotMandatedAuthorisation_successful() throws Exception {
        // Given
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithSigningBasketSupported(true));
        cancelPayment_withMandatedAuthorisation_successful(httpHeadersExplicit, EMBEDDED_SCA_APPROACH, EMBEDDED_EXPLICIT_CANCELLATION_RESP);
    }

    private void cancelPayment_withMandatedAuthorisation_successful(HttpHeaders httpHeaders, ScaApproach scaApproach, String responseJsonPath) throws Exception {
        // Given
        SpiPaymentCancellationResponse spiPaymentCancellationResponse = SpiPaymentCancellationResponseBuilder.buildSpiPaymentCancellationResponse(true);
        SpiResponse<SpiPaymentCancellationResponse> spiResponse = SpiResponse.<SpiPaymentCancellationResponse>builder()
                                                                      .payload(spiPaymentCancellationResponse)
                                                                      .build();
        given(paymentCancellationSpi.initiatePaymentCancellation(any(), any(), any()))
            .willReturn(spiResponse);

        given(aspspProfileService.getScaApproaches(null))
            .willReturn(Collections.singletonList(scaApproach));

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.buildCancellationPaymentUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPTED_PAYMENT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isAccepted())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(responseJsonPath, UTF_8)));
    }

    void cancelPayment_withNotMandatedAuthorisation_successful(HttpHeaders httpHeaders, ScaApproach scaApproach) throws Exception {
        // Given
        given(aspspDataService.readAspspConsentData(ENCRYPTED_PAYMENT_ID)).willReturn(Optional.of(ASPSP_CONSENT_DATA));

        SpiPaymentCancellationResponse spiPaymentCancellationResponse = SpiPaymentCancellationResponseBuilder.buildSpiPaymentCancellationResponse(false);
        SpiResponse<SpiPaymentCancellationResponse> spiInitiatePaymentCancellationResponse = SpiResponse.<SpiPaymentCancellationResponse>builder()
                                                                                                 .payload(spiPaymentCancellationResponse)
                                                                                                 .build();
        given(paymentCancellationSpi.initiatePaymentCancellation(any(), any(), any())).willReturn(spiInitiatePaymentCancellationResponse);

        SpiResponse<SpiResponse.VoidResponse> spiCancelPaymentResponse = SpiResponse.<SpiResponse.VoidResponse>builder()
                                                                             .payload(SpiResponse.voidResponse())
                                                                             .build();

        given(paymentCancellationSpi.cancelPaymentWithoutSca(any(), any(), any())).willReturn(spiCancelPaymentResponse);

        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(scaApproach));

        MockHttpServletRequestBuilder requestBuilder = delete(UrlBuilder.buildCancellationPaymentUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPTED_PAYMENT_ID));
        requestBuilder.headers(httpHeaders);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isNoContent())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
