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
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
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
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
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
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.jetbrains.annotations.NotNull;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class UpdatePsuDataForPaymentCancellationIT {

    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String ENCRYPT_PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String WRONG_AUTHORISATION_ID = "q3356ea7-8e3e-474f-b5ea-2b89346cb6jk";
    private static final String PSU_ID = "PSU-123";
    private static final String PSU_PASS = "12345";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();

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
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @MockBean
    private PaymentCancellationSpi paymentCancellationSpi;
    @MockBean
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @MockBean
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    private final JsonReader jsonReader = new JsonReader();

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @BeforeEach
    void setUp() {
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        httpHeaders.add("PSU-ID", PSU_ID);

        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo(), null))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(aspspProfileService.getAspspSettings(null)).willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(aspspProfileService.getScaApproaches(null))
            .willReturn(Collections.singletonList(ScaApproach.REDIRECT));
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        given(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(ENCRYPT_PAYMENT_ID)).willReturn(spiAspspConsentDataProvider);
        given(spiAspspConsentDataProvider.loadAspspConsentData()).willReturn("data".getBytes());
    }

    @Test
    void updatePsuData_success() throws Exception {
        given(eventServiceEncrypted.recordEvent(any(EventBO.class))).willReturn(true);
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPT_PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(buildPisCommonPaymentResponse(AUTHORISATION_ID))
                            .build());

        given(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED))
                            .build());
        given(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .willReturn(CmsResponse.<Authorisation>builder()
                            .payload(buildGetPisAuthorisationResponse(ScaStatus.PSUIDENTIFIED))
                            .build());

        given(paymentCancellationSpi.authorisePsu(any(SpiContextData.class), anyString(), any(SpiPsuData.class), eq(PSU_PASS), any(SpiPayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                            .build());

        AuthenticationObject sms = new AuthenticationObject();
        sms.setAuthenticationType("SMS_OTP");
        sms.setAuthenticationMethodId("sms");
        sms.setName("some-sms-name");

        given(paymentCancellationSpi.requestAvailableScaMethods(any(SpiContextData.class), any(SpiPayment.class), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(new SpiAvailableScaMethodsResponse(Collections.singletonList(sms)))
                            .build());
        given(authorisationServiceEncrypted.saveAuthenticationMethods(eq(AUTHORISATION_ID), any()))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        spiAuthorizationCodeResult.setSelectedScaMethod(sms);
        spiAuthorizationCodeResult.setScaStatus(ScaStatus.SCAMETHODSELECTED);

        given(paymentCancellationSpi.requestAuthorisationCode(any(SpiContextData.class), anyString(), any(), any(SpiAspspConsentDataProvider.class)))
            .willReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                            .payload(spiAuthorizationCodeResult)
                            .build());
        given(authorisationServiceEncrypted.updateAuthorisation(eq(AUTHORISATION_ID), any(UpdateAuthorisationRequest.class)))
            .willReturn(CmsResponse.<Authorisation>builder()
                            .payload(new Authorisation())
                            .build());

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.buildPaymentCancellationUpdateAuthorisationUrl(PaymentType.SINGLE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPT_PAYMENT_ID, AUTHORISATION_ID));
        requestBuilder.content(jsonReader.getStringFromFile("json/auth/req/update_password.json"));
        requestBuilder.headers(httpHeaders);

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/payment/res/update_psu_data_payment_cancellation_resp.json")));
    }

    @Test
    void updatePsuData_wrongAuthorisationId() throws Exception {
        given(eventServiceEncrypted.recordEvent(any(EventBO.class))).willReturn(true);

        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPT_PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(buildPisCommonPaymentResponse(AUTHORISATION_ID))
                            .build());

        MockHttpServletRequestBuilder requestBuilder = put(UrlBuilder.buildPaymentCancellationUpdateAuthorisationUrl(PaymentType.SINGLE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPT_PAYMENT_ID, WRONG_AUTHORISATION_ID));
        requestBuilder.content(jsonReader.getStringFromFile("json/auth/req/update_password.json"));
        requestBuilder.headers(httpHeaders);

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/auth/res/403_resource_unknown.json")));
    }

    @NotNull
    private PisCommonPaymentResponse buildPisCommonPaymentResponse(String authorisationId) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentResponse.setPaymentProduct(SEPA_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setTppInfo(TPP_INFO);
        pisCommonPaymentResponse.setAuthorisations(Collections.singletonList(new Authorisation(authorisationId,
                                                                                               new PsuIdData(PSU_ID, null, null, null, null),
                                                                                               ENCRYPT_PAYMENT_ID,
                                                                                               AuthorisationType.PIS_CANCELLATION,
                                                                                               ScaStatus.PSUIDENTIFIED)));
        pisCommonPaymentResponse.setTransactionStatus(TransactionStatus.ACSP);
        return pisCommonPaymentResponse;
    }

    private Authorisation buildGetPisAuthorisationResponse(ScaStatus scaStatus) {
        Authorisation getPisAuthorisationResponse = new Authorisation();
        getPisAuthorisationResponse.setScaStatus(scaStatus);
        getPisAuthorisationResponse.setParentId(ENCRYPT_PAYMENT_ID);
        getPisAuthorisationResponse.setAuthorisationType(AuthorisationType.PIS_CANCELLATION);
        return getPisAuthorisationResponse;
    }
}
