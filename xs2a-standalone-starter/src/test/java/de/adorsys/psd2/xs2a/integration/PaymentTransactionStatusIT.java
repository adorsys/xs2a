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
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
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
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.PisCommonPaymentResponseBuilder;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class PaymentTransactionStatusIT {
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String XML_CONTENT_TYPE = "application/xml";
    private static final String ACCEPT_HEADER = "accept";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final UUID X_REQUEST_ID = UUID.randomUUID();
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String ENCRYPTED_PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData("data".getBytes(), ENCRYPTED_PAYMENT_ID);

    private static final String TRANSACTION_STATUS_RESPONSE_PATH = "/json/payment/res/status/payment_initiation_status_resp.json";
    private static final String TRANSACTION_STATUS_SPI_XML_PATH = "/xml/payment/spi/res/payment_initiation_status_raw.xml";

    private HttpHeaders httpHeaders = new HttpHeaders();

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
    private SinglePaymentSpi singlePaymentSpi;
    @MockBean
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @MockBean
    private AspspDataService aspspDataService;
    @MockBean
    private UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiServiceEncrypted;

    @Before
    public void init() {
        httpHeaders.add("x-request-id", X_REQUEST_ID.toString());
        httpHeaders.add("PSU-ID", "PSU-123");
        httpHeaders.add("PSU-ID-Type", "Some type");
        httpHeaders.add("PSU-Corporate-ID", "Some corporate id");
        httpHeaders.add("PSU-Corporate-ID-Type", "Some corporate id type");

        when(aspspProfileService.getAspspSettings())
            .thenReturn(AspspSettingsBuilder.buildAspspSettings());
        when(aspspProfileService.getScaApproaches())
            .thenReturn(Collections.singletonList(ScaApproach.REDIRECT));
        when(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        when(eventServiceEncrypted.recordEvent(any(EventBO.class)))
            .thenReturn(true);
        when(updatePaymentStatusAfterSpiServiceEncrypted.updatePaymentStatus(eq(ENCRYPTED_PAYMENT_ID), any(TransactionStatus.class)))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        when(aspspDataService.readAspspConsentData(ENCRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(ASPSP_CONSENT_DATA));

        PisCommonPaymentResponse pisCommonPaymentResponse = PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponseWithPayment();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPTED_PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(pisCommonPaymentResponse)
                            .build());
        when(tppService.updateTppInfo(any(TppInfo.class)))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    @Test
    public void getTransactionStatus() throws Exception {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetTransactionStatusUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPTED_PAYMENT_ID));
        httpHeaders.add(ACCEPT_HEADER, JSON_CONTENT_TYPE);
        requestBuilder.headers(httpHeaders);
        when(singlePaymentSpi.getPaymentStatusById(any(), eq(JSON_CONTENT_TYPE), any(), any()))
            .thenReturn(SpiResponse.<SpiGetPaymentStatusResponse>builder().payload(new SpiGetPaymentStatusResponse(TransactionStatus.ACSP, null, SpiGetPaymentStatusResponse.RESPONSE_TYPE_JSON, null)).build());

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(IOUtils.resourceToString(TRANSACTION_STATUS_RESPONSE_PATH, UTF_8)));
    }

    @Test
    public void getTransactionStatus_raw() throws Exception {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetTransactionStatusUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPTED_PAYMENT_ID));
        httpHeaders.add(ACCEPT_HEADER, XML_CONTENT_TYPE);
        requestBuilder.headers(httpHeaders);
        byte[] paymentStatusRaw = IOUtils.resourceToByteArray(TRANSACTION_STATUS_SPI_XML_PATH);
        when(singlePaymentSpi.getPaymentStatusById(any(), eq(XML_CONTENT_TYPE), any(), any()))
            .thenReturn(SpiResponse.<SpiGetPaymentStatusResponse>builder().payload(new SpiGetPaymentStatusResponse(TransactionStatus.ACSP, null, SpiGetPaymentStatusResponse.RESPONSE_TYPE_XML, paymentStatusRaw)).build());

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_XML))
            .andExpect(content().string(IOUtils.resourceToString(TRANSACTION_STATUS_SPI_XML_PATH, UTF_8)));
    }

    @Test
    public void getTransactionStatusNoContentType() throws Exception {
        // Given
        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetTransactionStatusUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPTED_PAYMENT_ID));
        requestBuilder.headers(httpHeaders);
        when(singlePaymentSpi.getPaymentStatusById(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiGetPaymentStatusResponse>builder().payload(new SpiGetPaymentStatusResponse(TransactionStatus.ACSP, null, SpiGetPaymentStatusResponse.RESPONSE_TYPE_JSON, null)).build());

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(IOUtils.resourceToString(TRANSACTION_STATUS_RESPONSE_PATH, UTF_8)));
    }
}
