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
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.*;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
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
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles({"integration-test", "mock-qwac"})
@RunWith(SpringRunner.class)
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
public class PaymentStartAuthorisationIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final String PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private static final String PSU_ID = "PSU-123";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String AUTHORISATION_METHOD_ID = "3r356ea7-8e3e-474f-b5ea-2b89346cbd56";
    private static final String PSU_PASS = "09876";
    private static final String AUTH_REQ = "/json/payment/req/auth_request.json";
    private static final String AUTH_RESP = "/json/payment/res/explicit/auth_response.json";

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
    @MockBean
    private PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;
    @MockBean
    private CommonPaymentSpi commonPaymentSpi;
    @MockBean
    private UpdatePaymentAfterSpiServiceEncrypted updatePaymentAfterSpiService;
    @MockBean
    private PaymentAuthorisationSpi paymentAuthorisationSpi;
    @MockBean
    @Qualifier("consentRestTemplate")
    private RestTemplate consentRestTemplate;

    @Captor
    private ArgumentCaptor<CreatePisAuthorisationRequest> createPisAuthorisationRequestCaptor;


    private HttpHeaders httpHeadersExplicit = new HttpHeaders();

    @Before
    public void setUp() {
        httpHeadersExplicit.add("Content-Type", "application/json");
        httpHeadersExplicit.add("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        httpHeadersExplicit.add("PSU-ID", PSU_ID);

        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo()))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(aspspProfileService.getAspspSettings()).willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    @Test
    public void startPaymentAuthorisation_success() throws Exception {
        //Given
        String request = IOUtils.resourceToString(AUTH_REQ, UTF_8);

        given(eventServiceEncrypted.recordEvent(any(EventBO.class))).willReturn(true);
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(buildPisCommonPaymentResponse())
                            .build());

        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(ScaApproach.EMBEDDED));
        given(pisAuthorisationServiceEncrypted.createAuthorization(eq(PAYMENT_ID), createPisAuthorisationRequestCaptor.capture()))
            .willReturn(CmsResponse.<CreatePisAuthorisationResponse>builder()
                            .payload(new CreatePisAuthorisationResponse(AUTHORISATION_ID, ScaStatus.PSUIDENTIFIED, null, null, buildPsuIdData()))
                            .build());

        given(pisAuthorisationServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID))
            .willReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                            .payload(buildGetPisAuthorisationResponse(ScaStatus.PSUIDENTIFIED))
                            .build());
        given(pisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED))
                            .build());
        given(paymentAuthorisationSpi.authorisePsu(any(SpiContextData.class), any(SpiPsuData.class), eq(PSU_PASS), any(SpiPayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                            .build());
        SpiAuthenticationObject authenticationObject = new SpiAuthenticationObject();
        authenticationObject.setAuthenticationMethodId(AUTHORISATION_METHOD_ID);
        given(paymentAuthorisationSpi.requestAvailableScaMethods(any(SpiContextData.class), any(SpiPayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(new SpiAvailableScaMethodsResponse(Collections.singletonList(authenticationObject)))
                            .build());
        given(pisAuthorisationServiceEncrypted.saveAuthenticationMethods(eq(AUTHORISATION_ID), any()))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        given(paymentAuthorisationSpi.requestAuthorisationCode(any(SpiContextData.class), eq(AUTHORISATION_METHOD_ID), any(SpiPayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(new SpiAuthorizationCodeResult())
                            .build());
        given(aspspDataService.updateAspspConsentData(any(AspspConsentData.class))).willReturn(true);

        given(commonPaymentSpi.executePaymentWithoutSca(any(SpiContextData.class), any(SpiPaymentInfo.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                            .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                            .build());
        given(updatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TransactionStatus.ACCP))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        given(consentRestTemplate.postForEntity(anyString(), any(EventBO.class), eq(Boolean.class)))
            .willReturn(new ResponseEntity<>(true, HttpStatus.OK));

        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.buildPaymentStartAuthorisationUrl(
            SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, PAYMENT_ID));
        requestBuilder.headers(httpHeadersExplicit);
        requestBuilder.content(request);

        //When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(AUTH_RESP, UTF_8)));

        assertEquals(PaymentAuthorisationType.CREATED, createPisAuthorisationRequestCaptor.getValue().getAuthorizationType());
        assertEquals(PSU_ID, createPisAuthorisationRequestCaptor.getValue().getPsuData().getPsuId());
        assertEquals(ScaApproach.EMBEDDED, createPisAuthorisationRequestCaptor.getValue().getScaApproach());
    }

    @NotNull
    private GetPisAuthorisationResponse buildGetPisAuthorisationResponse(ScaStatus scaStatus) {
        GetPisAuthorisationResponse getPisAuthorisationResponse = new GetPisAuthorisationResponse();
        getPisAuthorisationResponse.setScaStatus(scaStatus);
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
        pisCommonPaymentResponse.setAuthorisations(Collections.singletonList(buildAuthorisation()));
        pisCommonPaymentResponse.setTransactionStatus(TransactionStatus.ACSP);
        return pisCommonPaymentResponse;
    }

    private Authorisation buildAuthorisation() {
        return new Authorisation(AUTHORISATION_ID, ScaStatus.RECEIVED, new PsuIdData(PSU_ID, null, null, null), PaymentAuthorisationType.CREATED);
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, null, null, null);
    }
}
