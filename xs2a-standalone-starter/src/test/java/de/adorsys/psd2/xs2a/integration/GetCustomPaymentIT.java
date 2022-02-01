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
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.PisCommonPaymentResponseBuilder;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
class GetCustomPaymentIT extends CustomPaymentTestParent {

    @BeforeEach
    protected void init() {
        super.init();
    }

    //Single
    @Test
    void single_Xml() throws Exception {
        getPayment(PaymentType.SINGLE, SINGLE_PAYMENT_CUSTOM_REQUEST_XML_PATH, MediaType.APPLICATION_XML);
    }

    @Test
    void single_Json() throws Exception {
        getPayment(PaymentType.SINGLE, SINGLE_PAYMENT_CUSTOM_REQUEST_JSON_PATH, MediaType.APPLICATION_JSON);
    }

    //Periodic
    @Test
    void periodic_Xml() throws Exception {
        getPayment(PaymentType.PERIODIC, PERIODIC_PAYMENT_CUSTOM_REQUEST_XML_PATH, MediaType.APPLICATION_XML);
    }

    @Test
    void periodic_Json() throws Exception {
        getPayment(PaymentType.PERIODIC, PERIODIC_PAYMENT_CUSTOM_REQUEST_JSON_PATH, MediaType.APPLICATION_JSON);
    }

    //Bulk
    @Test
    void bulk_Xml() throws Exception {
        getPayment(PaymentType.BULK, BULK_PAYMENT_CUSTOM_REQUEST_XML_PATH, MediaType.APPLICATION_XML);
    }

    @Test
    void bulk_Json() throws Exception {
        getPayment(PaymentType.BULK, BULK_PAYMENT_CUSTOM_REQUEST_JSON_PATH, MediaType.APPLICATION_JSON);
    }

    private void getPayment(PaymentType paymentType, String requestContentPath, MediaType mediaType) throws Exception {
        // Given
        HttpHeaders headers = mediaType == MediaType.APPLICATION_XML ? updateHeadersWithAcceptTypeXml(httpHeadersXml) : httpHeadersXml;

        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setPaymentType(paymentType);
        response.setPaymentProduct(CUSTOM_PAYMENT_PRODUCT);
        response.setTppInfo(TPP_INFO);

        SpiPaymentInfo spiPaymentInfo = new SpiPaymentInfo(CUSTOM_PAYMENT_PRODUCT);

        byte[] data = IOUtils.resourceToByteArray(requestContentPath);
        response.setPaymentData(data);
        spiPaymentInfo.setPaymentData(data);
        spiPaymentInfo.setStatus(TransactionStatus.ACSP);

        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(ScaApproach.EMBEDDED));

        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPT_PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder().payload(response).build());
        given(commonPaymentSpi.getPaymentById(any(SpiContextData.class), anyString(), any(SpiPaymentInfo.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(PisCommonPaymentResponseBuilder.buildGetPaymentResponse(spiPaymentInfo));

        String content = IOUtils.resourceToString(requestContentPath, UTF_8);
        String paymentUrl = UrlBuilder.buildGetPaymentUrl(paymentType.getValue(), CUSTOM_PAYMENT_PRODUCT, ENCRYPT_PAYMENT_ID);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(paymentUrl)
                                                           .headers(headers)
                                                           .content(content);
        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        ResultMatcher resultMatcher = mediaType == MediaType.APPLICATION_XML ? content().string(content) : content().json(content);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(mediaType))
            .andExpect(resultMatcher);
    }
}
