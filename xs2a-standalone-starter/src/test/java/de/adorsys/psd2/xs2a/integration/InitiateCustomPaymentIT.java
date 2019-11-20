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


import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.integration.builder.PsuIdDataBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.PisCommonPaymentResponseBuilder;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
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
    Xs2aInterfaceConfig.class
})
public class InitiateCustomPaymentIT extends CustomPaymentTestParent {
    @Before
    public void init() {
        super.init();
    }

    //Single
    @Test
    public void initiateSinglePaymentCustom_explicit_embedded_successful() throws Exception {
        initiateSinglePaymentCustomSuccessful(httpHeadersJson, ScaApproach.EMBEDDED, SINGLE_PAYMENT_CUSTOM_REQUEST_JSON_PATH);
    }

    @Test
    public void initiateSinglePaymentCustom_explicit_redirect_successful() throws Exception {
        initiateSinglePaymentCustomSuccessful(httpHeadersJson, ScaApproach.REDIRECT, SINGLE_PAYMENT_CUSTOM_REQUEST_JSON_PATH);
    }

    @Test
    public void initiateSinglePaymentCustomXML_explicit_embedded_successful() throws Exception {
        initiateSinglePaymentCustomSuccessful(httpHeadersXml, ScaApproach.EMBEDDED, SINGLE_PAYMENT_CUSTOM_REQUEST_XML_PATH);
    }

    @Test
    public void initiateSinglePaymentCustomXML_explicit_redirect_successful() throws Exception {
        initiateSinglePaymentCustomSuccessful(httpHeadersXml, ScaApproach.REDIRECT, SINGLE_PAYMENT_CUSTOM_REQUEST_XML_PATH);
    }

    //Periodic
    @Test
    public void initiatePeriodicPaymentCustom_explicit_embedded_successful() throws Exception {
        initiatePeriodicPaymentCustomSuccessful(ScaApproach.EMBEDDED);
    }

    @Test
    public void initiatePeriodicPaymentCustom_explicit_redirect_successful() throws Exception {
        initiatePeriodicPaymentCustomSuccessful(ScaApproach.REDIRECT);
    }

    //Bulk
    @Test
    public void initiateBulkPaymentCustom_explicit_embedded_successful() throws Exception {
        initiateBulkPaymentCustomSuccessful(httpHeadersJson, ScaApproach.EMBEDDED, BULK_PAYMENT_CUSTOM_REQUEST_JSON_PATH);
    }

    @Test
    public void initiateBulkPaymentCustom_explicit_redirect_successful() throws Exception {
        initiateBulkPaymentCustomSuccessful(httpHeadersJson, ScaApproach.REDIRECT, BULK_PAYMENT_CUSTOM_REQUEST_JSON_PATH);
    }

    @Test
    public void initiateBulkPaymentCustomXML_explicit_embedded_successful() throws Exception {
        initiateBulkPaymentCustomSuccessful(httpHeadersXml, ScaApproach.EMBEDDED, BULK_PAYMENT_CUSTOM_REQUEST_XML_PATH);
    }

    @Test
    public void initiateBulkPaymentCustomXML_explicit_redirect_successful() throws Exception {
        initiateBulkPaymentCustomSuccessful(httpHeadersXml, ScaApproach.REDIRECT, BULK_PAYMENT_CUSTOM_REQUEST_XML_PATH);
    }

    private void initiateBulkPaymentCustomSuccessful(HttpHeaders headers, ScaApproach scaApproach, String requestContentPath) throws Exception {
        makePreparation(scaApproach);

        String paymentUrl = UrlBuilder.buildInitiatePaymentUrl(PaymentType.BULK.getValue(), CUSTOM_PAYMENT_PRODUCT);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(paymentUrl)
                                                           .headers(headers)
                                                           .content(IOUtils.resourceToString(requestContentPath, UTF_8));
        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        String filePath = (String) responseMap.get(PaymentType.BULK, scaApproach);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(filePath, UTF_8)));
    }

    private CreatePisAuthorisationRequest getPisAuthorisationRequest(ScaApproach scaApproach) {
        return new CreatePisAuthorisationRequest(PaymentAuthorisationType.CREATED, PsuIdDataBuilder.buildPsuIdData(), scaApproach, TPP_REDIRECT_URIs);
    }

    private void initiateSinglePaymentCustomSuccessful(HttpHeaders headers, ScaApproach scaApproach, String requestContentPath) throws Exception {
        // Given
        makePreparation(scaApproach);

        String paymentUrl = UrlBuilder.buildInitiatePaymentUrl(PaymentType.SINGLE.getValue(), CUSTOM_PAYMENT_PRODUCT);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(paymentUrl)
                                                           .headers(headers)
                                                           .content(IOUtils.resourceToString(requestContentPath, UTF_8));
        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        String filePath = (String) responseMap.get(PaymentType.SINGLE, scaApproach);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(filePath, UTF_8)));
    }

    private void initiatePeriodicPaymentCustomSuccessful(ScaApproach scaApproach) throws Exception {
        makePreparation(scaApproach);

        String paymentUrl = UrlBuilder.buildInitiatePaymentUrl(PaymentType.PERIODIC.getValue(), CUSTOM_PAYMENT_PRODUCT);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                                                           .multipart(paymentUrl)
                                                           .params(buildMultipartParams())
                                                           .headers(httpHeadersXml);
        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        String filePath = (String) responseMap.get(PaymentType.PERIODIC, scaApproach);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(filePath, UTF_8)));
    }

    private MultiValueMap<String, String> buildMultipartParams() throws IOException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("xml_sct", IOUtils.resourceToString(PERIODIC_PAYMENT_CUSTOM_REQUEST_XML_PATH, UTF_8));
        params.add("json_standingorderType", IOUtils.resourceToString(PERIODIC_PAYMENT_CUSTOM_REQUEST_JSON_PATH, UTF_8));
        return params;
    }

    private void makePreparation(ScaApproach scaApproach) {
        given(pisAuthorisationServiceEncrypted.createAuthorization(ENCRYPT_PAYMENT_ID, getPisAuthorisationRequest(scaApproach)))
            .willReturn(CmsResponse.<CreatePisAuthorisationResponse>builder()
                            .payload(new CreatePisAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null, null))
                            .build());
        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(scaApproach));
        given(commonPaymentSpi.initiatePayment(any(SpiContextData.class), any(SpiPaymentInfo.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(PisCommonPaymentResponseBuilder.buildSpiPaymentInitiationResponse());
        given(consentRestTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class), anyString()))
            .willReturn(ResponseEntity.ok(Boolean.TRUE));
        given(pisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(scaApproach))
                            .build());
    }
}
