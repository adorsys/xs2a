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
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.PisCancellationAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
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
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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
class PaymentStartCancellationAuthorisationIT {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final ScaStatus PIS_AUTHORISATION_SCA_STATUS = ScaStatus.PSUIDENTIFIED;
    private static final String PAYMENT_ID = "JKDQjM02y1a9G7_kLTgAy8HJCLIOYVWZ-fQThyI7gYhZvqcmJ6kZg7CmJFgTANLhcgftJbETkzvNvu5mZQqWcA==_=_psGLvQpt9Q";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private static final String PSU_ID = "PSU-123";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String AUTHORISATION_RESPONSE = "/json/payment/cancellation/res/explicit/cancellation_authorisation_response.json";
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

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

    @Captor
    private ArgumentCaptor<CreateAuthorisationRequest> createAuthorisationRequestCaptor;
    @Captor
    private ArgumentCaptor<PisCancellationAuthorisationParentHolder> pisCancellationAuthorisationParentHolderCaptor;


    private final HttpHeaders httpHeadersExplicit = new HttpHeaders();
    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        httpHeadersExplicit.add("Content-Type", "application/json");
        httpHeadersExplicit.add("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        httpHeadersExplicit.add("PSU-ID", PSU_ID);
        httpHeadersExplicit.add("TPP-Redirect-URI", TPP_REDIRECT_URI);
        httpHeadersExplicit.add("TPP-Nok-Redirect-URI", TPP_NOK_REDIRECT_URI);

        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo(), null)).willReturn(CmsResponse.<Boolean>builder()
                                                                                                .payload(false)
                                                                                                .build());
        given(aspspProfileService.getAspspSettings(null)).willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    @Test
    void startPaymentCancellationAuthorisation_success() throws Exception {
        //Given
        given(eventServiceEncrypted.recordEvent(any(EventBO.class))).willReturn(true);
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(buildPisCommonPaymentResponse())
                            .build());

        given(aspspProfileService.getScaApproaches(null)).willReturn(Collections.singletonList(ScaApproach.EMBEDDED));
        given(authorisationServiceEncrypted.createAuthorisation(pisCancellationAuthorisationParentHolderCaptor.capture(), createAuthorisationRequestCaptor.capture()))
            .willReturn(CmsResponse.<CreateAuthorisationResponse>builder()
                            .payload(new CreateAuthorisationResponse(AUTHORISATION_ID, PIS_AUTHORISATION_SCA_STATUS, null, null, ScaApproach.EMBEDDED))
                            .build());

        given(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .willReturn(CmsResponse.<Authorisation>builder()
                            .payload(buildGetPisAuthorisationResponse())
                            .build());
        given(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .willReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED))
                            .build());

        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.buildPaymentStartCancellationAuthorisationUrl(
            SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, PAYMENT_ID));
        requestBuilder.headers(httpHeadersExplicit);

        CreateAuthorisationRequest expectedCreatePisAuthorisationRequest =
            new CreateAuthorisationRequest(AUTHORISATION_ID, new PsuIdData(PSU_ID, null, null, null, null),
                                           ScaApproach.EMBEDDED, ScaStatus.STARTED, TPP_REDIRECT_URIs);

        //When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(IOUtils.resourceToString(AUTHORISATION_RESPONSE, UTF_8)));

        CreateAuthorisationRequest actual = createAuthorisationRequestCaptor.getValue();
        expectedCreatePisAuthorisationRequest.setAuthorisationId(actual.getAuthorisationId());
        assertEquals(expectedCreatePisAuthorisationRequest, actual);
        assertEquals(PAYMENT_ID, pisCancellationAuthorisationParentHolderCaptor.getValue().getParentId());
    }

    @NotNull
    private Authorisation buildGetPisAuthorisationResponse() {
        Authorisation getPisAuthorisationResponse = new Authorisation();
        getPisAuthorisationResponse.setScaStatus(PIS_AUTHORISATION_SCA_STATUS);
        PisPaymentInfo paymentInfo = new PisPaymentInfo();
        paymentInfo.setPaymentId(PAYMENT_ID);
        return getPisAuthorisationResponse;
    }

    @NotNull
    private PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(TPP_INFO);
        pisCommonPaymentResponse.setPaymentType(SINGLE_PAYMENT_TYPE);
        pisCommonPaymentResponse.setPaymentProduct(SEPA_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setTransactionStatus(TransactionStatus.ACSP);
        pisCommonPaymentResponse.setInternalPaymentStatus(InternalPaymentStatus.CANCELLED_INITIATED);
        return pisCommonPaymentResponse;
    }
}
