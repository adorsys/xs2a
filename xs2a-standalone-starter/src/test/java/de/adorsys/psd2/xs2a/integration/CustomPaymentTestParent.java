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
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import org.apache.commons.collections.map.MultiKeyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

public abstract class CustomPaymentTestParent {
    protected static final Charset UTF_8 = StandardCharsets.UTF_8;
    protected static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    protected static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();

    static final String TPP_REDIRECT_URI = "request/redirect_uri";
    static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    static final String ENCRYPT_PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    static final String CUSTOM_PAYMENT_PRODUCT = "custom-payment";
    static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);
    static final String SINGLE_PAYMENT_CUSTOM_REQUEST_JSON_PATH = "/json/payment/req/SinglePaymentCustomInitiate_request.json";
    static final String SINGLE_PAYMENT_CUSTOM_REQUEST_XML_PATH = "/xml/payment/spi/res/SinglePaymentCustomInitiate_request.xml";
    static final String PERIODIC_PAYMENT_CUSTOM_REQUEST_XML_PATH = "/xml/payment/spi/res/PeriodicPaymentCustomInitiate_request.xml";
    static final String PERIODIC_PAYMENT_CUSTOM_REQUEST_JSON_PATH = "/json/payment/req/PeriodicPaymentCustomInitiate_request.json";
    static final String BULK_PAYMENT_CUSTOM_REQUEST_XML_PATH = "/xml/payment/spi/res/BulkPaymentCustomInitiate_request.xml";
    static final String BULK_PAYMENT_CUSTOM_REQUEST_JSON_PATH = "/json/payment/req/BulkPaymentCustomInitiate_request.json";
    static final String PAYMENT_CUSTOM_STATUS_RESPONSE_JSON_PATH = "/json/payment/req/SinglePaymentCustomStatus_response.json";
    static final String PAYMENT_CUSTOM_STATUS_RESPONSE_XML_PATH = "/xml/payment/spi/res/SinglePaymentCustomStatus_response.xml";

    HttpHeaders httpHeadersJson = new HttpHeaders();
    HttpHeaders httpHeadersXml = new HttpHeaders();
    MultiKeyMap responseMap = new MultiKeyMap();

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected AspspProfileService aspspProfileService;
    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean
    protected AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @MockBean
    protected  PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @MockBean
    protected CommonPaymentSpi commonPaymentSpi;
    @MockBean
    @Qualifier("consentRestTemplate")
    protected RestTemplate consentRestTemplate;
    @MockBean
    protected AuthorisationChainResponsibilityService authorisationChainResponsibilityService;

    protected void init() {
        HashMap<String, String> headerMap = new HashMap<>();
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

        httpHeadersJson.setAll(headerMap);
        // when we use Explicit auth mode we need to set 'true' and value 'signingBasketSupported' in profile also should be 'true'
        httpHeadersJson.add("TPP-Explicit-Authorisation-Preferred", "true");
        httpHeadersJson.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        httpHeadersXml.setAll(headerMap);
        httpHeadersXml.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);


        responseMap.put(PaymentType.SINGLE, ScaApproach.EMBEDDED, "/json/payment/res/explicit/SinglePaymentCustomInitiate_embedded_explicit_response.json");
        responseMap.put(PaymentType.SINGLE, ScaApproach.REDIRECT, "/json/payment/res/explicit/SinglePaymentCustomInitiate_redirect_explicit_response.json");
        responseMap.put(PaymentType.BULK, ScaApproach.EMBEDDED, "/json/payment/res/explicit/BulkPaymentCustomInitiate_embedded_explicit_response.json");
        responseMap.put(PaymentType.BULK, ScaApproach.REDIRECT, "/json/payment/res/explicit/BulkPaymentCustomInitiate_redirect_explicit_response.json");
        responseMap.put(PaymentType.PERIODIC, ScaApproach.EMBEDDED, "/json/payment/res/explicit/PeriodicPaymentCustomInitiate_embedded_explicit_response.json");
        responseMap.put(PaymentType.PERIODIC, ScaApproach.REDIRECT, "/json/payment/res/explicit/PeriodicPaymentCustomInitiate_redirect_explicit_response.json");

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

    protected HttpHeaders updateHeadersWithAcceptTypeXml(HttpHeaders httpHeaders) {
        httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);
        return httpHeaders;
    }
}
