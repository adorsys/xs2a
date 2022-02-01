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
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.PisAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.PisCancellationAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aScaStatusResponse;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.PisCommonPaymentResponseBuilder;
import de.adorsys.psd2.xs2a.service.PaymentServiceForAuthorisationImpl;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class PaymentControllerIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String ENCRYPT_PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String CANCELLATION_ID = "cancellationId";
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;

    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";

    private static final String CANCELLATION_AUTHORISATIONS_RESP = "/json/payment/res/explicit/SinglePaymentCancellationAuth_response.json";
    private static final String CANCELLATION_AUTHORISATIONS_REDIRECT_OAUTH_RESP = "/json/payment/res/explicit/SinglePaymentCancellationAuth_Redirect_OAuth_response.json";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    private final HttpHeaders httpHeadersExplicit = new HttpHeaders();

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
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @MockBean
    private SinglePaymentSpi singlePaymentSpi;
    @MockBean
    private AspspDataService aspspDataService;
    @MockBean
    @Qualifier("consentRestTemplate")
    private RestTemplate consentRestTemplate;
    @MockBean
    private PaymentServiceForAuthorisationImpl paymentServiceForAuthorisation;

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

        given(pisCommonPaymentServiceEncrypted.getPsuDataListByPaymentId(any()))
            .willReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(Collections.singletonList(getPsuIdData()))
                            .build());

        given(authorisationServiceEncrypted.createAuthorisation(any(), any()))
            .willReturn(CmsResponse.<CreateAuthorisationResponse>builder()
                            .payload(new CreateAuthorisationResponse(CANCELLATION_ID, SCA_STATUS, INTERNAL_REQUEST_ID, null, SCA_APPROACH))
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
        httpHeadersExplicit.add("TPP-Redirect-URI", TPP_REDIRECT_URI);
        httpHeadersExplicit.add("TPP-Nok-Redirect-URI", TPP_NOK_REDIRECT_URI);

        // when we use Explicit auth mode we need to set 'true' and value 'signingBasketSupported' in profile also should be 'true'
        httpHeadersExplicit.add("TPP-Explicit-Authorisation-Preferred", "true");
    }

    @Test
    void getPaymentInitiationScaStatus_successful() throws Exception {
        // Given
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(SCA_APPROACH));
        given(paymentServiceForAuthorisation.getAuthorisationScaStatus(ENCRYPT_PAYMENT_ID, AUTHORISATION_ID, SINGLE_PAYMENT_TYPE, SEPA_PAYMENT_PRODUCT))
            .willReturn(ResponseObject.<Xs2aScaStatusResponse>builder().body(new Xs2aScaStatusResponse(ScaStatus.RECEIVED, true, "psu message", null, null)).build());
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPT_PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponseWithAuthorisation(
                                new Authorisation(AUTHORISATION_ID, getPsuIdData(), ENCRYPT_PAYMENT_ID, AuthorisationType.PIS_CREATION, ScaStatus.RECEIVED)))
                            .build());
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(SCA_APPROACH))
                            .build());

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetPaymentInitiationScaStatusUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPT_PAYMENT_ID, AUTHORISATION_ID));
        requestBuilder.headers(httpHeadersExplicit);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json("{\"scaStatus\":\"received\"}"));
    }

    @Test
    void cancelPaymentAuthorisation_successful() throws Exception {
        // Given
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(SCA_APPROACH));
        given(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new PisAuthorisationParentHolder(ENCRYPT_PAYMENT_ID)))
            .willReturn(CmsResponse.<ScaStatus>builder()
                            .payload(ScaStatus.RECEIVED)
                            .build());
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPT_PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponse())
                            .build());
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(CANCELLATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(SCA_APPROACH))
                            .build());

        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.buildGetPaymentCancellationAuthorisationUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPT_PAYMENT_ID));
        requestBuilder.headers(httpHeadersExplicit);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(CANCELLATION_AUTHORISATIONS_RESP, UTF_8)));
    }

    @Test
    void cancelPaymentAuthorisation_Redirect_OAuth_successful() throws Exception {
        // Given
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithScaRedirectFlow(ScaRedirectFlow.OAUTH));
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(SCA_APPROACH));
        given(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new PisAuthorisationParentHolder(ENCRYPT_PAYMENT_ID)))
            .willReturn(CmsResponse.<ScaStatus>builder()
                            .payload(ScaStatus.RECEIVED)
                            .build());
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPT_PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponse())
                            .build());
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(anyString()))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(SCA_APPROACH))
                            .build());

        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.buildGetPaymentCancellationAuthorisationUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPT_PAYMENT_ID));
        requestBuilder.headers(httpHeadersExplicit);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(CANCELLATION_AUTHORISATIONS_REDIRECT_OAUTH_RESP, UTF_8)));
    }

    @Test
    void getPaymentInitiationCancellationAuthorisationInformation() throws Exception {
        // Given
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(SCA_APPROACH));
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPT_PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponse())
                            .build());

        List<String> authorisationIds = Arrays.asList("c0121ca2-ab3a-4564-b915-6e40e8b40f50", "743d0a45-7233-4fbf-9799-c657f327836c");
        given(authorisationServiceEncrypted.getAuthorisationsByParentId(new PisCancellationAuthorisationParentHolder(ENCRYPT_PAYMENT_ID)))
            .willReturn(CmsResponse.<List<String>>builder()
                            .payload(authorisationIds)
                            .build());

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetPaymentInitiationCancellationAuthorisationInformationUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPT_PAYMENT_ID));
        requestBuilder.headers(httpHeadersExplicit);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString("/json/payment/res/Cancellations.json", UTF_8)));
    }

    private PsuIdData getPsuIdData() {
        return new PsuIdData("PSU-123", "Some type", "Some corporate id", "Some corporate id type", "Some IP Address");
    }
}
