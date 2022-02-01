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
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.starter.Xs2aStandaloneStarter;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.SpiPaymentInitiationResponseBuilder;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
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
class InitiatePaymentsNotSuccessfulIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String SINGLE_PAYMENT_REQUEST_JSON_PATH = "/json/payment/req/SinglePaymentInitiate_request.json";
    private static final String PSU_IP_ADDRESS_MISSING_ERROR_MESSAGE_JSON_PATH = "/json/account/res/PsuIpAddressMissingErrorMessage.json";
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;

    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String INITIATE_SINGLE_PAYMENT_URL = UrlBuilder.buildInitiatePaymentUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT);

    private final HttpHeaders httpHeadersWithoutPsuIpAddress = new HttpHeaders();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @MockBean
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @MockBean
    private SinglePaymentSpi singlePaymentSpi;

    @BeforeEach
    void init() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("PSU-ID", "PSU-123");
        headerMap.put("PSU-ID-Type", "Some type");
        headerMap.put("PSU-Corporate-ID", "Some corporate id");
        headerMap.put("PSU-Corporate-ID-Type", "Some corporate id type");
        httpHeadersWithoutPsuIpAddress.setAll(headerMap);

        given(aspspProfileService.getAspspSettings(null))
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo(), null))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    @Test
    void initiateSinglePayment_withoutPsuIpAddress_notSuccessful() throws Exception {
        //Given
        makePreparations(ScaApproach.EMBEDDED);
        MockHttpServletRequestBuilder requestBuilder = makeRequestBuilder(INITIATE_SINGLE_PAYMENT_URL, httpHeadersWithoutPsuIpAddress, IOUtils.resourceToString(SINGLE_PAYMENT_REQUEST_JSON_PATH, UTF_8));
        //When
        ResultActions resultActions = mockMvc.perform(requestBuilder);
        //Then
        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(IOUtils.resourceToString(PSU_IP_ADDRESS_MISSING_ERROR_MESSAGE_JSON_PATH, UTF_8)));
    }

    private void makePreparations(ScaApproach scaApproach) {
        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(scaApproach));
        given(singlePaymentSpi.initiatePayment(any(SpiContextData.class), any(SpiSinglePayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiPaymentInitiationResponseBuilder.buildSinglePaymentResponse(false));
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(scaApproach))
                            .build());
    }

    private MockHttpServletRequestBuilder makeRequestBuilder(String url, HttpHeaders headers, String content) {
        return MockMvcRequestBuilders.post(url)
                   .headers(headers)
                   .content(content);
    }
}
