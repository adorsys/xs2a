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
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.HttpHeadersBuilder;
import de.adorsys.psd2.xs2a.integration.builder.PsuIdDataBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.integration.builder.payment.PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public abstract class PaymentUpdateAuthorisationBase {
    protected static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    protected static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    protected static final String PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    protected static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    protected static final String PSU_ID_1 = "PSU-1";
    protected static final String PSU_ID_2 = "PSU-2";

    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String AUTH_REQ = "/json/payment/req/auth_request.json";
    private static final String PSU_CREDENTIALS_INVALID_RESP = "/json/payment/res/explicit/psu_credentials_invalid_response.json";
    private static final String FORMAT_ERROR_RESP = "/json/payment/res/explicit/format_error_response.json";

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected TppService tppService;
    @MockBean
    protected TppStopListService tppStopListService;
    @MockBean
    protected AspspProfileService aspspProfileService;
    @MockBean
    protected Xs2aEventServiceEncrypted eventServiceEncrypted;
    @MockBean
    protected PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @MockBean
    protected PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted;

    public void before() {
        given(aspspProfileService.getAspspSettings()).willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(aspspProfileService.getScaApproaches())
            .willReturn(Collections.singletonList(ScaApproach.REDIRECT));
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.getTppInfo()))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());
        given(tppService.updateTppInfo(any(TppInfo.class)))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
    }

    void updatePaymentPsuDataAndCheckForPsuCredentialsInvalidResponse(String psuIdAuthorisation, String psuIdHeader) throws Exception {
        //When
        ResultActions resultActions = updatePaymentPsuDataAndGetResultActions(psuIdAuthorisation, psuIdHeader);

        //Then
        resultActions
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(PSU_CREDENTIALS_INVALID_RESP, UTF_8)));
    }

    void updatePaymentPsuDataAndCheckForFormatErrorResponse(String psuIdAuthorisation, String psuIdHeader) throws Exception {
        //When
        ResultActions resultActions = updatePaymentPsuDataAndGetResultActions(psuIdAuthorisation, psuIdHeader);

        //Then
        resultActions
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json(IOUtils.resourceToString(FORMAT_ERROR_RESP, UTF_8)));
    }

    private ResultActions updatePaymentPsuDataAndGetResultActions(String psuIdAuthorisation, String psuIdHeader) throws Exception {
        //Given
        String request = IOUtils.resourceToString(AUTH_REQ, UTF_8);
        PsuIdData psuIdDataAuthorisation = buildPsuIdDataAuthorisation(psuIdAuthorisation);
        HttpHeadersIT httpHeaders = buildHttpHeaders(psuIdHeader);

        List<Authorisation> authorisationList = Collections.singletonList(buildAuthorisation(psuIdDataAuthorisation));
        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse(authorisationList);
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .willReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(pisCommonPaymentResponse)
                            .build());
        given(pisAuthorisationServiceEncrypted.updatePisAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FAILED))
            .willReturn(CmsResponse.<Boolean>builder()
                            .payload(Boolean.TRUE)
                            .build());
        MockHttpServletRequestBuilder requestBuilder = put(buildRequestUrl());
        requestBuilder.headers(httpHeaders);
        requestBuilder.content(request);

        return mockMvc.perform(requestBuilder);
    }

    abstract String buildRequestUrl();

    private HttpHeadersIT buildHttpHeaders(String psuIdHeader) {
        HttpHeadersIT httpHeadersBase = HttpHeadersBuilder.buildHttpHeaders();
        return Optional.ofNullable(psuIdHeader)
                   .map(httpHeadersBase::addPsuIdHeader)
                   .orElse(httpHeadersBase);
    }

    private PsuIdData buildPsuIdDataAuthorisation(String psuIdAuthorisation) {
        return Optional.ofNullable(psuIdAuthorisation)
                   .map(PsuIdDataBuilder::buildPsuIdData)
                   .orElse(null);
    }

    private Authorisation buildAuthorisation(PsuIdData psuIdData) {
        return new Authorisation(AUTHORISATION_ID, ScaStatus.RECEIVED, psuIdData, PaymentAuthorisationType.CREATED);
    }
}
