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


import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.PisAuthorisationParentHolder;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreatePaymentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.PisCommonPaymentResponseBuilder;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
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
class InitiateCustomPaymentIT extends CustomPaymentTestParent {
    @BeforeEach
    protected void init() {
        super.init();
    }

    //Single
    @Test
    void initiateSinglePaymentCustom_explicit_embedded_successful() throws Exception {
        initiateSinglePaymentCustomSuccessful(httpHeadersJson, ScaApproach.EMBEDDED, SINGLE_PAYMENT_CUSTOM_REQUEST_JSON_PATH);
    }

    @Test
    void initiateSinglePaymentCustom_explicit_redirect_successful() throws Exception {
        initiateSinglePaymentCustomSuccessful(httpHeadersJson, ScaApproach.REDIRECT, SINGLE_PAYMENT_CUSTOM_REQUEST_JSON_PATH);
    }

    @Test
    void initiateSinglePaymentCustomXML_explicit_embedded_successful() throws Exception {
        initiateSinglePaymentCustomSuccessful(httpHeadersXml, ScaApproach.EMBEDDED, SINGLE_PAYMENT_CUSTOM_REQUEST_XML_PATH);
    }

    @Test
    void initiateSinglePaymentCustomXML_explicit_redirect_successful() throws Exception {
        initiateSinglePaymentCustomSuccessful(httpHeadersXml, ScaApproach.REDIRECT, SINGLE_PAYMENT_CUSTOM_REQUEST_XML_PATH);
    }

    //Periodic
    @Test
    void initiatePeriodicPaymentCustom_explicit_embedded_successful() throws Exception {
        initiatePeriodicPaymentCustomSuccessful(ScaApproach.EMBEDDED);
    }

    @Test
    void initiatePeriodicPaymentCustom_explicit_redirect_successful() throws Exception {
        initiatePeriodicPaymentCustomSuccessful(ScaApproach.REDIRECT);
    }

    //Bulk
    @Test
    void initiateBulkPaymentCustom_explicit_embedded_successful() throws Exception {
        initiateBulkPaymentCustomSuccessful(httpHeadersJson, ScaApproach.EMBEDDED, BULK_PAYMENT_CUSTOM_REQUEST_JSON_PATH);
    }

    @Test
    void initiateBulkPaymentCustom_explicit_redirect_successful() throws Exception {
        initiateBulkPaymentCustomSuccessful(httpHeadersJson, ScaApproach.REDIRECT, BULK_PAYMENT_CUSTOM_REQUEST_JSON_PATH);
    }

    @Test
    void initiateBulkPaymentCustomXML_explicit_embedded_successful() throws Exception {
        initiateBulkPaymentCustomSuccessful(httpHeadersXml, ScaApproach.EMBEDDED, BULK_PAYMENT_CUSTOM_REQUEST_XML_PATH);
    }

    @Test
    void initiateBulkPaymentCustomXML_explicit_redirect_successful() throws Exception {
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(filePath, UTF_8)));
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(filePath, UTF_8)));
    }

    private MultiValueMap<String, String> buildMultipartParams() throws IOException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("xml_sct", IOUtils.resourceToString(PERIODIC_PAYMENT_CUSTOM_REQUEST_XML_PATH, UTF_8));
        params.add("json_standingorderType", IOUtils.resourceToString(PERIODIC_PAYMENT_CUSTOM_REQUEST_JSON_PATH, UTF_8));
        return params;
    }

    private void makePreparation(ScaApproach scaApproach) {
        given(authorisationServiceEncrypted.createAuthorisation(eq(new PisAuthorisationParentHolder(ENCRYPT_PAYMENT_ID)),
                                                                any(CreateAuthorisationRequest.class)))
            .willReturn(CmsResponse.<CreateAuthorisationResponse>builder()
                            .payload(new CreateAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null, scaApproach))
                            .build());
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(scaApproach));
        given(commonPaymentSpi.initiatePayment(any(SpiContextData.class), any(SpiPaymentInfo.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(PisCommonPaymentResponseBuilder.buildSpiPaymentInitiationResponse());
        given(consentRestTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class), anyString()))
            .willReturn(ResponseEntity.ok(Boolean.TRUE));
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(scaApproach))
                            .build());
        CreatePaymentAuthorisationProcessorResponse processorResponse =
            new CreatePaymentAuthorisationProcessorResponse(ScaStatus.STARTED, scaApproach, null,
                                                            Collections.emptySet(), ENCRYPT_PAYMENT_ID, null);
        given(authorisationChainResponsibilityService.apply(any(PisAuthorisationProcessorRequest.class)))
            .willReturn(processorResponse);
    }
}
