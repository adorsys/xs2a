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
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.PisAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.consent.CreatePaymentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.SpiPaymentInitiationResponseBuilder;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
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
class InitiatePaymentsSuccessfulIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static final String SINGLE_PAYMENT_REQUEST_JSON_PATH = "/json/payment/req/SinglePaymentInitiate_request.json";
    private static final String PERIODIC_PAYMENT_REQUEST_JSON_PATH = "/json/payment/req/PeriodicPaymentInitiate_request.json";
    private static final String BULK_PAYMENT_REQUEST_JSON_PATH = "/json/payment/req/BulkPaymentInitiate_request.json";

    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final PaymentType PERIODIC_PAYMENT_TYPE = PaymentType.PERIODIC;
    private static final PaymentType BULK_PAYMENT_TYPE = PaymentType.BULK;

    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String ENCRYPT_PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";

    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;

    private final HttpHeaders httpHeadersImplicit = new HttpHeaders();
    private final HttpHeaders httpHeadersImplicitNoPsuData = new HttpHeaders();
    private final HttpHeaders httpHeadersExplicit = new HttpHeaders();
    private final HttpHeaders httpHeadersExplicitNoPsuData = new HttpHeaders();
    private final MultiKeyMap responseMap = new MultiKeyMap();
    private final MultiKeyMap responseMapOauth = new MultiKeyMap();
    private final MultiKeyMap responseMapSigningBasketMode = new MultiKeyMap();

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
    private PeriodicPaymentSpi periodicPaymentSpi;
    @MockBean
    private BulkPaymentSpi bulkPaymentSpi;
    @MockBean
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;

    @MockBean
    @Qualifier("consentRestTemplate")
    private RestTemplate consentRestTemplate;

    @BeforeEach
    void init() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("PSU-ID", "PSU-123");
        headerMap.put("PSU-ID-Type", "Some type");
        headerMap.put("PSU-Corporate-ID", "Some corporate id");
        headerMap.put("PSU-Corporate-ID-Type", "Some corporate id type");
        headerMap.put("PSU-IP-Address", "1.1.1.1");
        headerMap.put("PSU-IP-Port", "1111");
        headerMap.put("PSU-User-Agent", "Some user agent");
        headerMap.put("PSU-Geo-Location", "Some geo location");
        headerMap.put("PSU-Accept", "Some accept");
        headerMap.put("PSU-Accept-Charset", "Some accept-charset");
        headerMap.put("PSU-Accept-Encoding", "Some accept-encoding");
        headerMap.put("PSU-Accept-Language", "Some accept-language");
        headerMap.put("PSU-Http-Method", "Some http method");
        headerMap.put("PSU-Device-ID", "d7d369a9-898d-4682-b586-0a63ffe43a2c");
        headerMap.put("TPP-Redirect-URI", TPP_REDIRECT_URI);
        headerMap.put("TPP-NOK-Redirect-URI", TPP_NOK_REDIRECT_URI);

        httpHeadersImplicit.setAll(headerMap);
        // when Implicit auth mode we need to set 'false'
        httpHeadersImplicit.add("TPP-Implicit-Authorisation-Preferred", "false");

        httpHeadersImplicitNoPsuData.putAll(httpHeadersImplicit);
        httpHeadersImplicitNoPsuData.remove("PSU-ID");
        httpHeadersImplicitNoPsuData.remove("PSU-ID-Type");
        httpHeadersImplicitNoPsuData.remove("PSU-Corporate-ID");
        httpHeadersImplicitNoPsuData.remove("PSU-Corporate-ID-Type");
        httpHeadersImplicitNoPsuData.remove("PSU-IP-Port");
        httpHeadersImplicitNoPsuData.remove("PSU-User-Agent");
        httpHeadersImplicitNoPsuData.remove("PSU-Geo-Location");
        httpHeadersImplicitNoPsuData.remove("PSU-Accept");
        httpHeadersImplicitNoPsuData.remove("PSU-Accept-Charset");
        httpHeadersImplicitNoPsuData.remove("PSU-Accept-Encoding");
        httpHeadersImplicitNoPsuData.remove("PSU-Accept-Language");
        httpHeadersImplicitNoPsuData.remove("PSU-Http-Method");
        httpHeadersImplicitNoPsuData.remove("PSU-Device-ID");

        httpHeadersExplicit.setAll(headerMap);
        // when we use Explicit auth mode we need to set 'true' and value 'signingBasketSupported' in profile also should be 'true'
        httpHeadersExplicit.add("TPP-Explicit-Authorisation-Preferred", "true");

        httpHeadersExplicitNoPsuData.putAll(httpHeadersExplicit);
        httpHeadersExplicitNoPsuData.remove("PSU-ID");
        httpHeadersExplicitNoPsuData.remove("PSU-ID-Type");
        httpHeadersExplicitNoPsuData.remove("PSU-Corporate-ID");
        httpHeadersExplicitNoPsuData.remove("PSU-Corporate-ID-Type");
        httpHeadersExplicitNoPsuData.remove("PSU-IP-Port");
        httpHeadersExplicitNoPsuData.remove("PSU-User-Agent");
        httpHeadersExplicitNoPsuData.remove("PSU-Geo-Location");
        httpHeadersExplicitNoPsuData.remove("PSU-Accept");
        httpHeadersExplicitNoPsuData.remove("PSU-Accept-Charset");
        httpHeadersExplicitNoPsuData.remove("PSU-Accept-Encoding");
        httpHeadersExplicitNoPsuData.remove("PSU-Accept-Language");
        httpHeadersExplicitNoPsuData.remove("PSU-Http-Method");
        httpHeadersExplicitNoPsuData.remove("PSU-Device-ID");

        responseMap.put(false, PaymentType.SINGLE, ScaApproach.REDIRECT, "", "", "/json/payment/res/implicit/SinglePaymentInitiate_redirect_implicit_response.json");
        responseMap.put(false, PaymentType.SINGLE, ScaApproach.REDIRECT, "", "psuIdDataIsEmpty", "/json/payment/res/implicit/SinglePaymentInitiate_redirect_implicit_psuIdDataIsEmpty_response.json");
        responseMap.put(false, PaymentType.SINGLE, ScaApproach.REDIRECT, "multilevelSca", "", "/json/payment/res/implicit/SinglePaymentInitiate_redirect_implicit_multilevelSca_response.json");
        responseMap.put(false, PaymentType.SINGLE, ScaApproach.REDIRECT, "multilevelSca", "psuIdDataIsEmpty", "/json/payment/res/implicit/SinglePaymentInitiate_redirect_implicit_multilevelSca_psuIdDataIsEmpty_response.json");
        responseMap.put(false, PaymentType.SINGLE, ScaApproach.EMBEDDED, "", "", "/json/payment/res/implicit/SinglePaymentInitiate_embedded_implicit_response.json");
        responseMap.put(false, PaymentType.SINGLE, ScaApproach.EMBEDDED, "", "psuIdDataIsEmpty", "/json/payment/res/implicit/SinglePaymentInitiate_embedded_implicit_psuIdDataIsEmpty_response.json");
        responseMap.put(false, PaymentType.SINGLE, ScaApproach.EMBEDDED, "multilevelSca", "", "/json/payment/res/implicit/SinglePaymentInitiate_embedded_implicit_multilevelSca_response.json");
        responseMap.put(false, PaymentType.SINGLE, ScaApproach.EMBEDDED, "multilevelSca", "psuIdDataIsEmpty", "/json/payment/res/implicit/SinglePaymentInitiate_embedded_implicit_multilevelSca_psuIdDataIsEmpty_response.json");
        responseMap.put(false, PaymentType.PERIODIC, ScaApproach.REDIRECT, "/json/payment/res/implicit/PeriodicPaymentInitiate_redirect_implicit_response.json");
        responseMap.put(false, PaymentType.PERIODIC, ScaApproach.EMBEDDED, "/json/payment/res/implicit/PeriodicPaymentInitiate_embedded_implicit_response.json");
        responseMap.put(false, PaymentType.BULK, ScaApproach.REDIRECT, "/json/payment/res/implicit/BulkPaymentInitiate_redirect_implicit_response.json");
        responseMap.put(false, PaymentType.BULK, ScaApproach.EMBEDDED, "/json/payment/res/implicit/BulkPaymentInitiate_embedded_implicit_response.json");

        responseMap.put(true, PaymentType.SINGLE, ScaApproach.REDIRECT, "", "", "/json/payment/res/explicit/SinglePaymentInitiate_redirect_explicit_response.json");
        responseMap.put(true, PaymentType.SINGLE, ScaApproach.REDIRECT, "", "psuIdDataIsEmpty", "/json/payment/res/explicit/SinglePaymentInitiate_redirect_explicit_psuIdDataIsEmpty_response.json");
        responseMap.put(true, PaymentType.SINGLE, ScaApproach.REDIRECT, "multilevelSca", "", "/json/payment/res/explicit/SinglePaymentInitiate_redirect_explicit_multilevelSca_response.json");
        responseMap.put(true, PaymentType.SINGLE, ScaApproach.REDIRECT, "multilevelSca", "psuIdDataIsEmpty", "/json/payment/res/explicit/SinglePaymentInitiate_redirect_explicit_multilevelSca_psuIdDataIsEmpty_response.json");
        responseMap.put(true, PaymentType.SINGLE, ScaApproach.EMBEDDED, "", "", "/json/payment/res/explicit/SinglePaymentInitiate_redirect_explicit_response.json");
        responseMap.put(true, PaymentType.SINGLE, ScaApproach.EMBEDDED, "", "psuIdDataIsEmpty", "/json/payment/res/explicit/SinglePaymentInitiate_redirect_explicit_psuIdDataIsEmpty_response.json");
        responseMap.put(true, PaymentType.SINGLE, ScaApproach.EMBEDDED, "multilevelSca", "", "/json/payment/res/explicit/SinglePaymentInitiate_embedded_explicit_multilevelSca_response.json");
        responseMap.put(true, PaymentType.SINGLE, ScaApproach.EMBEDDED, "multilevelSca", "psuIdDataIsEmpty", "/json/payment/res/explicit/SinglePaymentInitiate_embedded_explicit_multilevelSca_psuIdDataIsEmpty_response.json");
        responseMap.put(true, PaymentType.PERIODIC, ScaApproach.REDIRECT, "/json/payment/res/explicit/PeriodicPaymentInitiate_redirect_explicit_response.json");
        responseMap.put(true, PaymentType.PERIODIC, ScaApproach.EMBEDDED, "/json/payment/res/explicit/PeriodicPaymentInitiate_redirect_explicit_response.json");
        responseMap.put(true, PaymentType.BULK, ScaApproach.REDIRECT, "/json/payment/res/explicit/BulkPaymentInitiate_redirect_explicit_response.json");
        responseMap.put(true, PaymentType.BULK, ScaApproach.EMBEDDED, "/json/payment/res/explicit/BulkPaymentInitiate_redirect_explicit_response.json");

        responseMapOauth.put(httpHeadersImplicit, PaymentType.SINGLE, ScaApproach.REDIRECT, "/json/payment/res/implicit/SinglePaymentInitiate_redirect_oauth_implicit_response.json");
        responseMapSigningBasketMode.put(true, PaymentType.SINGLE, ScaApproach.EMBEDDED, "multilevelSca", "psuIdDataIsEmpty", "/json/payment/res/explicit/SinglePaymentInitiate_embedded_explicit_multilevelSca_psuIdDataIsEmpty_signingBasketActive_response.json");
        responseMapSigningBasketMode.put(true, PaymentType.SINGLE, ScaApproach.EMBEDDED, "multilevelSca", "", "/json/payment/res/explicit/SinglePaymentInitiate_embedded_explicit_multilevelSca_signingBasketActive_response.json");

        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo(), null))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(eventServiceEncrypted.recordEvent(any(EventBO.class)))
            .willReturn(true);
        given(consentRestTemplate.postForEntity(anyString(), any(EventBO.class), eq(Boolean.class)))
            .willReturn(new ResponseEntity<>(true, HttpStatus.OK));

        given(pisCommonPaymentServiceEncrypted.createCommonPayment(any(PisPaymentInfo.class)))
            .willReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder()
                            .payload(new CreatePisCommonPaymentResponse(ENCRYPT_PAYMENT_ID, null))
                            .build());
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    // =============== IMPLICIT MODE
    //
    @Test
    void initiateSinglePayment_implicit_embedded_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersImplicit, ScaApproach.EMBEDDED, false, false);
    }

    @Test
    void initiateSinglePayment_implicit_embedded_psuIdDataIsEmpty_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersImplicitNoPsuData, ScaApproach.EMBEDDED, false, true);
    }

    @Test
    void initiateSinglePayment_implicit_embedded_multilevelSca_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersImplicit, ScaApproach.EMBEDDED, true, false);
    }

    @Test
    void initiateSinglePayment_implicit_embedded_multilevelSca_psuIdDataIsEmpty_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersImplicitNoPsuData, ScaApproach.EMBEDDED, true, true);
    }

    @Test
    void initiateSinglePayment_implicit_redirect_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersImplicit, ScaApproach.REDIRECT, false, false);
    }

    @Test
    void initiateSinglePayment_implicit_redirect_psuIdDataIsEmpty_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersImplicitNoPsuData, ScaApproach.REDIRECT, false, true);
    }

    @Test
    void initiateSinglePayment_implicit_redirect_multilevelSca_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersImplicit, ScaApproach.REDIRECT, true, false);
    }

    @Test
    void initiateSinglePayment_implicit_redirect_multilevelSca_psuIdDataIsEmpty_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersImplicitNoPsuData, ScaApproach.REDIRECT, true, true);
    }

    @Test
    void initiateSinglePayment_implicit_redirect_oauth_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithScaRedirectFlow(ScaRedirectFlow.OAUTH));
        initiateSinglePaymentOauth_successful(httpHeadersImplicit, ScaApproach.REDIRECT);
    }

    @Test
    void initiatePeriodicPayment_implicit_embedded_successful() throws Exception {
        initiatePeriodicPayment_successful(httpHeadersImplicit, ScaApproach.EMBEDDED);
    }

    @Test
    void initiatePeriodicPayment_implicit_redirect_successful() throws Exception {
        initiatePeriodicPayment_successful(httpHeadersImplicit, ScaApproach.REDIRECT);
    }

    @Test
    void initiateBulkPayment_implicit_embedded_successful() throws Exception {
        initiateBulkPayment_successful(httpHeadersImplicit, ScaApproach.EMBEDDED);
    }

    @Test
    void initiateBulkPayment_implicit_redirect_successful() throws Exception {
        initiateBulkPayment_successful(httpHeadersImplicit, ScaApproach.REDIRECT);
    }

    // =============== EXPLICIT MODE
    //

    @Test
    void initiateSinglePayment_explicit_embedded_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersExplicit, ScaApproach.EMBEDDED, false, false);
    }

    @Test
    void initiateSinglePayment_explicit_embedded_psuIdDataIsEmpty_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersExplicitNoPsuData, ScaApproach.EMBEDDED, false, true);
    }

    @Test
    void initiateSinglePayment_explicit_embedded_multilevelSca_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithSigningBasketSupported(true));
        initiateSinglePayment_successful(httpHeadersExplicit, ScaApproach.EMBEDDED, true, false);
    }

    @Test
    void initiateSinglePayment_explicit_embedded_multilevelSca_psuIdDataIsEmpty_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersExplicitNoPsuData, ScaApproach.EMBEDDED, true, true);
    }

    @Test
    void initiateSinglePayment_explicit_embedded_multilevelSca_psuIdDataIsEmpty_signingBasketActive_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithSigningBasketSupported(true));
        initiateSinglePayment_successful(httpHeadersExplicitNoPsuData, ScaApproach.EMBEDDED, true, true);
    }

    @Test
    void initiateSinglePayment_explicit_embedded_multilevelSca_signingBasketActive_successful() throws Exception {
        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettingsWithSigningBasketSupported(true));
        initiateSinglePayment_successful(httpHeadersExplicit, ScaApproach.EMBEDDED, true, false);
    }

    @Test
    void initiateSinglePayment_explicit_redirect_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersExplicit, ScaApproach.REDIRECT, false, false);
    }

    @Test
    void initiateSinglePayment_explicit_redirect_psuIdDataIsEmpty_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersExplicitNoPsuData, ScaApproach.REDIRECT, false, true);
    }

    @Test
    void initiateSinglePayment_explicit_redirect_multilevelSca_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersExplicit, ScaApproach.REDIRECT, true, false);
    }

    @Test
    void initiateSinglePayment_explicit_redirect_multilevelSca_psuIdDataIsEmpty_successful() throws Exception {
        initiateSinglePayment_successful(httpHeadersExplicitNoPsuData, ScaApproach.REDIRECT, true, true);
    }

    @Test
    void initiatePeriodicPayment_explicit_embedded_successful() throws Exception {
        initiatePeriodicPayment_successful(httpHeadersExplicit, ScaApproach.EMBEDDED);
    }

    @Test
    void initiatePeriodicPayment_explicit_redirect_successful() throws Exception {
        initiatePeriodicPayment_successful(httpHeadersExplicit, ScaApproach.REDIRECT);
    }

    @Test
    void initiateBulkPayment_explicit_embedded_successful() throws Exception {
        initiateBulkPayment_successful(httpHeadersExplicit, ScaApproach.EMBEDDED);
    }

    @Test
    void initiateBulkPayment_explicit_redirect_successful() throws Exception {
        initiateBulkPayment_successful(httpHeadersExplicit, ScaApproach.REDIRECT);
    }

    private void initiateSinglePayment_successful(HttpHeaders headers, ScaApproach scaApproach, boolean multilevelSca, boolean isPsuIdDataEmpty) throws Exception {
        // Given
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(scaApproach));

        given(singlePaymentSpi.initiatePayment(any(SpiContextData.class), any(SpiSinglePayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiPaymentInitiationResponseBuilder.buildSinglePaymentResponse(multilevelSca));
        given(consentRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(), any(Class.class), any(String.class)))
            .willReturn(ResponseEntity.ok(Boolean.TRUE));
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(scaApproach))
                            .build());
        given(authorisationServiceEncrypted.createAuthorisation(eq(new PisAuthorisationParentHolder(ENCRYPT_PAYMENT_ID)), any()))
            .willReturn(getCmsReponse(SCA_STATUS));
        CreatePaymentAuthorisationProcessorResponse processorResponse =
            new CreatePaymentAuthorisationProcessorResponse(ScaStatus.STARTED, scaApproach, null,
                                                            Collections.emptySet(), ENCRYPT_PAYMENT_ID, null);
        given(authorisationChainResponsibilityService.apply(any(PisAuthorisationProcessorRequest.class)))
            .willReturn(processorResponse);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(UrlBuilder.buildInitiatePaymentUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT));
        requestBuilder.headers(headers);
        requestBuilder.content(IOUtils.resourceToString(SINGLE_PAYMENT_REQUEST_JSON_PATH, UTF_8));

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);


        String filePath = isSigningBasketModeActive(headers)
                              ? (String) responseMapSigningBasketMode.get(isExplicitMethod(headers, multilevelSca), PaymentType.SINGLE, scaApproach, multilevelScaKey(multilevelSca), psuIdDataEmptyKey(isPsuIdDataEmpty))
                              : (String) responseMap.get(isExplicitMethod(headers, multilevelSca), PaymentType.SINGLE, scaApproach, multilevelScaKey(multilevelSca), psuIdDataEmptyKey(isPsuIdDataEmpty));

        System.out.println(content());
        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(filePath, UTF_8)));
    }

    private void initiateSinglePaymentOauth_successful(HttpHeaders headers, ScaApproach scaApproach) throws Exception {
        // Given
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(scaApproach));

        given(singlePaymentSpi.initiatePayment(any(SpiContextData.class), any(SpiSinglePayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiPaymentInitiationResponseBuilder.buildSinglePaymentResponse(false));

        given(consentRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(), any(Class.class), any(String.class)))
            .willReturn(ResponseEntity.ok(Boolean.TRUE));
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(scaApproach))
                            .build());
        given(authorisationServiceEncrypted.createAuthorisation(eq(new PisAuthorisationParentHolder(ENCRYPT_PAYMENT_ID)), any()))
            .willReturn(getCmsReponse(SCA_STATUS));
        CreatePaymentAuthorisationProcessorResponse processorResponse =
            new CreatePaymentAuthorisationProcessorResponse(ScaStatus.STARTED, scaApproach, null,
                                                            Collections.emptySet(), ENCRYPT_PAYMENT_ID, null);
        given(authorisationChainResponsibilityService.apply(any(PisAuthorisationProcessorRequest.class)))
            .willReturn(processorResponse);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(UrlBuilder.buildInitiatePaymentUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT));
        requestBuilder.headers(headers);
        requestBuilder.content(IOUtils.resourceToString(SINGLE_PAYMENT_REQUEST_JSON_PATH, UTF_8));

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString((String) responseMapOauth.get(headers, PaymentType.SINGLE, scaApproach), UTF_8)));
    }

    private void initiatePeriodicPayment_successful(HttpHeaders headers, ScaApproach scaApproach) throws Exception {
        // Given
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(scaApproach));
        given(periodicPaymentSpi.initiatePayment(any(SpiContextData.class), any(SpiPeriodicPayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiPaymentInitiationResponseBuilder.buildPeriodicPaymentResponse());

        given(consentRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(), any(Class.class), any(String.class)))
            .willReturn(ResponseEntity.ok(Boolean.TRUE));
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(scaApproach))
                            .build());
        given(authorisationServiceEncrypted.createAuthorisation(eq(new PisAuthorisationParentHolder(ENCRYPT_PAYMENT_ID)), any()))
            .willReturn(getCmsReponse(SCA_STATUS));
        CreatePaymentAuthorisationProcessorResponse processorResponse =
            new CreatePaymentAuthorisationProcessorResponse(ScaStatus.STARTED, scaApproach, null,
                                                            Collections.emptySet(), ENCRYPT_PAYMENT_ID, null);
        given(authorisationChainResponsibilityService.apply(any(PisAuthorisationProcessorRequest.class)))
            .willReturn(processorResponse);
        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.buildInitiatePaymentUrl(PERIODIC_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT));
        requestBuilder.headers(headers);
        requestBuilder.content(IOUtils.resourceToString(PERIODIC_PAYMENT_REQUEST_JSON_PATH, UTF_8));

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString((String) responseMap.get(isExplicitMethod(headers, false), PaymentType.PERIODIC, scaApproach), UTF_8)));
    }

    private void initiateBulkPayment_successful(HttpHeaders headers, ScaApproach scaApproach) throws Exception {
        // Given
        Map<String, BigDecimal> amountMap = new HashMap<>();
        amountMap.put("DE21500105176194357737", new BigDecimal("666"));
        amountMap.put("DE54500105173424724776", new BigDecimal("888"));
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(scaApproach));

        given(bulkPaymentSpi.initiatePayment(any(SpiContextData.class), any(SpiBulkPayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiPaymentInitiationResponseBuilder.buildBulkPaymentResponse());

        given(consentRestTemplate.exchange(any(String.class), any(HttpMethod.class), any(), any(Class.class), any(String.class)))
            .willReturn(ResponseEntity.ok(Boolean.TRUE));
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(scaApproach))
                            .build());
        given(authorisationServiceEncrypted.createAuthorisation(eq(new PisAuthorisationParentHolder(ENCRYPT_PAYMENT_ID)), any()))
            .willReturn(getCmsReponse(SCA_STATUS));
        CreatePaymentAuthorisationProcessorResponse processorResponse =
            new CreatePaymentAuthorisationProcessorResponse(ScaStatus.STARTED, scaApproach, null,
                                                            Collections.emptySet(), ENCRYPT_PAYMENT_ID, null);
        given(authorisationChainResponsibilityService.apply(any(PisAuthorisationProcessorRequest.class)))
            .willReturn(processorResponse);

        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.buildInitiatePaymentUrl(BULK_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT));
        requestBuilder.headers(headers);
        requestBuilder.content(IOUtils.resourceToString(BULK_PAYMENT_REQUEST_JSON_PATH, UTF_8));

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString((String) responseMap.get(isExplicitMethod(headers, false), PaymentType.BULK, scaApproach), UTF_8)));
    }

    private String multilevelScaKey(boolean multilevelSca) {
        return multilevelSca ? "multilevelSca" : "";
    }

    private String psuIdDataEmptyKey(boolean isPsuIdDataEmpty) {
        return isPsuIdDataEmpty ? "psuIdDataIsEmpty" : "";
    }

    private boolean isExplicitMethod(HttpHeaders headers, boolean multilevelScaRequired) {
        StartAuthorisationMode startAuthorisationMode = aspspProfileService.getAspspSettings(null).getCommon().getStartAuthorisationMode();

        if (StartAuthorisationMode.AUTO.equals(startAuthorisationMode)) {
            return multilevelScaRequired || isSigningBasketModeActive(headers);
        }
        return StartAuthorisationMode.EXPLICIT.equals(startAuthorisationMode);
    }

    private boolean isSigningBasketModeActive(HttpHeaders headers) {
        boolean tppExplicitAuthorisationPreferred = Boolean.parseBoolean(headers.toSingleValueMap().get("TPP-Explicit-Authorisation-Preferred"));
        return tppExplicitAuthorisationPreferred && aspspProfileService.getAspspSettings(null).getSb().isSigningBasketSupported();
    }

    private CmsResponse<CreateAuthorisationResponse> getCmsReponse(ScaStatus scaStatus) {
        return CmsResponse.<CreateAuthorisationResponse>builder()
                   .payload(buildCreateAuthorisationResponse(scaStatus))
                   .build();
    }

    private CreateAuthorisationResponse buildCreateAuthorisationResponse(ScaStatus scaStatus) {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, scaStatus, null, null, ScaApproach.EMBEDDED);
    }
}
