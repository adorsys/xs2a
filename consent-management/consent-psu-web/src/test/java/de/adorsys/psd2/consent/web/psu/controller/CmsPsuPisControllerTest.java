/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.api.pis.UpdatePaymentRequest;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.consent.web.psu.mapper.PaymentModelMapperCmsPsu;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsPsuPisControllerTest {
    private static final String PAYMENT_ID = "RLJWGWtrRDwj5fDvVlLK0k";
    private static final String AUTHORISATION_ID = "564b5851-8ec3-4199-9313-5a7be8f95929";
    private static final String PSU_ID = "psu id";
    private static final String PSU_ID_TYPE = "psu id type";
    private static final String PSU_CORPORATE_ID = "psu corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "psu corporate id type";
    private static final String INSTANCE_ID = "instance id";
    private static final String SCA_STATUS_RECEIVED = "RECEIVED";
    private static final String TPP_NOK_REDIRECT_URI = "https://everything_is_bad.html";
    private static final String REDIRECT_ID = "redirect_id";
    private static final String METHOD_ID = "SMS";
    private static final String AUTHENTICATION_DATA = "123456";
    private static final AuthenticationDataHolder AUTHENTICATION_DATA_HOLDER = new AuthenticationDataHolder(METHOD_ID, AUTHENTICATION_DATA);

    private static final String INSTANCE_ID_HEADER_NAME = "instance-id";
    private static final String PSU_ID_HEADER_NAME = "psu-id";
    private static final String PSU_ID_TYPE_HEADER_NAME = "psu-id-type";
    private static final String PSU_CORPORATE_ID_HEADER_NAME = "psu-corporate-id";
    private static final String PSU_CORPORATE_ID_TYPE_HEADER_NAME = "psu-corporate-id-type";
    private static final HttpHeaders PSU_HEADERS = buildPsuHeaders();
    private static final HttpHeaders INSTANCE_ID_HEADERS = buildInstanceIdHeaders();
    private static final byte[] EMPTY_BODY = new byte[0];
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_TYPE = "payments";

    private final JsonReader jsonReader = new JsonReader();
    private MockMvc mockMvc;
    private final PsuIdData psuIdData = buildPsuIdData();

    @Mock
    private CmsPsuPisService cmsPsuPisService;
    @Mock
    private PaymentModelMapperCmsPsu paymentModelMapperCms;

    @InjectMocks
    private CmsPsuPisController cmsPsuPisController;

    @BeforeEach
    void setUp() {
        Xs2aObjectMapper xs2aObjectMapper = new ObjectMapperConfig().xs2aObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(cmsPsuPisController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(xs2aObjectMapper))
                      .build();
    }

    @Test
    void updatePayment() throws Exception {
        String payment = jsonReader.getStringFromFile("json/pis/request/update-payment.json");

        when(paymentModelMapperCms.mapToXs2aPayment()).thenReturn(payment.getBytes());
        UpdatePaymentRequest updatePaymentRequest = new UpdatePaymentRequest(payment.getBytes(), INSTANCE_ID, PAYMENT_ID, PAYMENT_PRODUCT, PAYMENT_TYPE);
        when(cmsPsuPisService.updatePayment(updatePaymentRequest)).thenReturn(true);

        mockMvc.perform(put("/psu-api/v1/payment/{payment-service}/{payment-product}/{payment-id}", PAYMENT_TYPE, PAYMENT_PRODUCT, PAYMENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payment))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));
    }

    @Test
    void updatePayment_error() throws Exception {
        String payment = jsonReader.getStringFromFile("json/pis/request/update-payment.json");

        when(paymentModelMapperCms.mapToXs2aPayment()).thenReturn(payment.getBytes());
        UpdatePaymentRequest updatePaymentRequest = new UpdatePaymentRequest(payment.getBytes(), INSTANCE_ID, PAYMENT_ID, PAYMENT_PRODUCT, PAYMENT_TYPE);
        when(cmsPsuPisService.updatePayment(updatePaymentRequest)).thenReturn(false);

        mockMvc.perform(put("/psu-api/v1/payment/{payment-service}/{payment-product}/{payment-id}", PAYMENT_TYPE, PAYMENT_PRODUCT, PAYMENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payment))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));
    }

    @Test
    void updatePsuInPayment_withValidRequest_shouldReturnOk() throws Exception {
        String psuIdDataJson = jsonReader.getStringFromFile("json/psu-id-data.json");
        when(cmsPsuPisService.updatePsuInPayment(psuIdData, AUTHORISATION_ID, INSTANCE_ID)).thenReturn(true);

        mockMvc.perform(put("/psu-api/v1/payment/authorisation/{authorisation-id}/psu-data", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(psuIdDataJson))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).updatePsuInPayment(psuIdData, AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void updatePsuInPayment_withFalseServiceResponse_shouldReturnBadRequest() throws Exception {
        String psuIdDataJson = jsonReader.getStringFromFile("json/psu-id-data.json");
        when(cmsPsuPisService.updatePsuInPayment(psuIdData, AUTHORISATION_ID, INSTANCE_ID)).thenReturn(false);

        mockMvc.perform(put("/psu-api/v1/payment/authorisation/{authorisation-id}/psu-data", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(psuIdDataJson))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).updatePsuInPayment(psuIdData, AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void updatePsuInPayment_onExpiredAuthorisationException_shouldReturnNokLink() throws Exception {
        String psuIdDataJson = jsonReader.getStringFromFile("json/psu-id-data.json");
        String cmsPaymentResponseJson = jsonReader.getStringFromFile("json/pis/response/cms-payment-response-timeout.json");
        when(cmsPsuPisService.updatePsuInPayment(psuIdData, AUTHORISATION_ID, INSTANCE_ID)).thenThrow(new AuthorisationIsExpiredException(TPP_NOK_REDIRECT_URI));

        mockMvc.perform(put("/psu-api/v1/payment/authorisation/{authorisation-id}/psu-data", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(psuIdDataJson))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(cmsPaymentResponseJson));

        verify(cmsPsuPisService).updatePsuInPayment(psuIdData, AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void getPaymentIdByRedirectId_withValidRequest_shouldReturnPaymentResponse() throws Exception {
        String cmsPaymentResponseJson = jsonReader.getStringFromFile("json/pis/response/cms-payment-response.json");
        CmsPaymentResponse cmsPaymentResponse = buildCmsPaymentResponse();
        when(cmsPsuPisService.checkRedirectAndGetPayment(REDIRECT_ID, INSTANCE_ID)).thenReturn(Optional.of(cmsPaymentResponse));

        mockMvc.perform(get("/psu-api/v1/payment/redirect/{redirect-id}", REDIRECT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsPaymentResponseJson));
    }

    @Test
    void getPaymentIdByRedirectId_withEmptyServiceResponse_shouldReturnNotFound() throws Exception {
        when(cmsPsuPisService.checkRedirectAndGetPayment(REDIRECT_ID, INSTANCE_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/psu-api/v1/payment/redirect/{redirect-id}", REDIRECT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).checkRedirectAndGetPayment(REDIRECT_ID, INSTANCE_ID);
    }

    @Test
    void getPaymentIdByRedirectId_onExpiredRedirectUrlException_shouldReturnNokLink() throws Exception {
        String cmsPaymentResponseJson = jsonReader.getStringFromFile("json/pis/response/cms-payment-response-timeout.json");
        when(cmsPsuPisService.checkRedirectAndGetPayment(REDIRECT_ID, INSTANCE_ID)).thenThrow(new RedirectUrlIsExpiredException(TPP_NOK_REDIRECT_URI));

        mockMvc.perform(get("/psu-api/v1/payment/redirect/{redirect-id}", REDIRECT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(cmsPaymentResponseJson));

        verify(cmsPsuPisService).checkRedirectAndGetPayment(REDIRECT_ID, INSTANCE_ID);
    }

    @Test
    void getPaymentByPaymentId_withValidRequest_shouldReturnPayment() throws Exception {
        String cmsPaymentJson = jsonReader.getStringFromFile("json/pis/response/cms-payment.json");
        CmsBasePaymentResponse cmsPayment = buildCmsPayment();
        when(cmsPsuPisService.getPayment(psuIdData, PAYMENT_ID, INSTANCE_ID)).thenReturn(Optional.of(cmsPayment));

        mockMvc.perform(get("/psu-api/v1/payment/{payment-id}", PAYMENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsPaymentJson));
    }

    @Test
    void getPaymentByPaymentId_withEmptyServiceResponse_shouldReturnBadRequest() throws Exception {
        when(cmsPsuPisService.getPayment(psuIdData, PAYMENT_ID, INSTANCE_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/psu-api/v1/payment/{payment-id}", PAYMENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).getPayment(psuIdData, PAYMENT_ID, INSTANCE_ID);

    }

    @Test
    void getPaymentIdByRedirectIdForCancellation_withValidRequest_shouldReturnPaymentResponse() throws Exception {
        String cmsPaymentResponseJson = jsonReader.getStringFromFile("json/pis/response/cms-payment-response.json");
        CmsPaymentResponse cmsPaymentResponse = buildCmsPaymentResponse();
        when(cmsPsuPisService.checkRedirectAndGetPaymentForCancellation(REDIRECT_ID, INSTANCE_ID)).thenReturn(Optional.of(cmsPaymentResponse));

        mockMvc.perform(get("/psu-api/v1/payment/cancellation/redirect/{redirect-id}", REDIRECT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsPaymentResponseJson));
    }

    @Test
    void getPaymentIdByRedirectIdForCancellation_withEmptyServiceResponse_shouldReturnNotFound() throws Exception {
        when(cmsPsuPisService.checkRedirectAndGetPaymentForCancellation(REDIRECT_ID, INSTANCE_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/psu-api/v1/payment/cancellation/redirect/{redirect-id}", REDIRECT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).checkRedirectAndGetPaymentForCancellation(REDIRECT_ID, INSTANCE_ID);
    }

    @Test
    void getPaymentIdByRedirectIdForCancellation_onExpiredRedirectUrlException_shouldReturnNokLink() throws Exception {
        String cmsPaymentResponseJson = jsonReader.getStringFromFile("json/pis/response/cms-payment-response-timeout.json");
        when(cmsPsuPisService.checkRedirectAndGetPaymentForCancellation(REDIRECT_ID, INSTANCE_ID)).thenThrow(new RedirectUrlIsExpiredException(TPP_NOK_REDIRECT_URI));

        mockMvc.perform(get("/psu-api/v1/payment/cancellation/redirect/{redirect-id}", REDIRECT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(cmsPaymentResponseJson));

        verify(cmsPsuPisService).checkRedirectAndGetPaymentForCancellation(REDIRECT_ID, INSTANCE_ID);
    }

    @Test
    void getPaymentByPaymentIdForCancellation_withValidRequest_shouldReturnPayment() throws Exception {
        String cmsPaymentJson = jsonReader.getStringFromFile("json/pis/response/cms-payment.json");
        CmsBasePaymentResponse cmsPayment = buildCmsPayment();
        when(cmsPsuPisService.getPayment(psuIdData, PAYMENT_ID, INSTANCE_ID)).thenReturn(Optional.of(cmsPayment));

        mockMvc.perform(get("/psu-api/v1/payment/cancellation/{payment-id}", PAYMENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsPaymentJson));
    }

    @Test
    void getPaymentByPaymentIdForCancellation_withEmptyServiceResponse_shouldReturnBadRequest() throws Exception {
        when(cmsPsuPisService.getPayment(psuIdData, PAYMENT_ID, INSTANCE_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/psu-api/v1/payment/cancellation/{payment-id}", PAYMENT_ID)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).getPayment(psuIdData, PAYMENT_ID, INSTANCE_ID);
    }

    @Test
    void getAuthorisationByAuthorisationId_withValidRequest_shouldReturnAuthorisation() throws Exception {
        String cmsPsuAuthorisationJson = jsonReader.getStringFromFile("json/pis/response/cms-psu-authorisation.json");
        CmsPsuAuthorisation cmsPsuAuthorisation = jsonReader.getObjectFromString(cmsPsuAuthorisationJson, CmsPsuAuthorisation.class);
        when(cmsPsuPisService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(cmsPsuAuthorisation));

        mockMvc.perform(get("/psu-api/v1/payment/authorisation/{authorisation-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsPsuAuthorisationJson));
    }

    @Test
    void getAuthorisationByAuthorisationId_withEmptyServiceResponse_shouldReturnBadRequest() throws Exception {
        when(cmsPsuPisService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/psu-api/v1/payment/authorisation/{authorisation-id}", AUTHORISATION_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_withValidRequest_shouldReturnOk() throws Exception {
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER))
            .thenReturn(true);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/pis/request/authentication-data-holder.json");

        mockMvc.perform(put("/psu-api/v1/payment/{payment-id}/authorisation/{authorisation-id}/status/{status}", PAYMENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER);
    }

    @Test
    void updateAuthorisationStatus_withValidRequestAndLowercaseScaStatus_shouldReturnOk() throws Exception {
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER))
            .thenReturn(true);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/pis/request/authentication-data-holder.json");
        String lowercaseScaStatus = SCA_STATUS_RECEIVED.toLowerCase();

        mockMvc.perform(put("/psu-api/v1/payment/{payment-id}/authorisation/{authorisation-id}/status/{status}", PAYMENT_ID, AUTHORISATION_ID, lowercaseScaStatus)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER);
    }

    @Test
    void updateAuthorisationStatus_withFalseServiceResponse_shouldReturnBadRequest() throws Exception {
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER))
            .thenReturn(false);
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/pis/request/authentication-data-holder.json");

        mockMvc.perform(put("/psu-api/v1/payment/{payment-id}/authorisation/{authorisation-id}/status/{status}", PAYMENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER);
    }

    @Test
    void updateAuthorisationStatus_withInvalidScaStatus_shouldReturnBadRequest() throws Exception {
        String invalidScaStatus = "invalid SCA status";
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/pis/request/authentication-data-holder.json");

        mockMvc.perform(put("/psu-api/v1/payment/{payment-id}/authorisation/{authorisation-id}/status/{status}", PAYMENT_ID, AUTHORISATION_ID, invalidScaStatus)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService, never()).updateAuthorisationStatus(any(), anyString(), anyString(), any(), anyString(), any());
    }

    @Test
    void updateAuthorisationStatus_onExpiredAuthorisationException_shouldReturnNokLink() throws Exception {
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER))
            .thenThrow(new AuthorisationIsExpiredException(TPP_NOK_REDIRECT_URI));
        String authenticationDataHolderContent = jsonReader.getStringFromFile("json/pis/request/authentication-data-holder.json");
        String cmsPaymentTimeoutResponse = jsonReader.getStringFromFile("json/pis/response/cms-payment-response-timeout.json");

        mockMvc.perform(put("/psu-api/v1/payment/{payment-id}/authorisation/{authorisation-id}/status/{status}", PAYMENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED)
                            .headers(INSTANCE_ID_HEADERS)
                            .headers(PSU_HEADERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(authenticationDataHolderContent))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().json(cmsPaymentTimeoutResponse));

        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, AUTHENTICATION_DATA_HOLDER);
    }

    @Test
    void updatePaymentStatus_withValidRequest_shouldReturnOk() throws Exception {
        TransactionStatus transactionStatus = TransactionStatus.ACCP;
        when(cmsPsuPisService.updatePaymentStatus(PAYMENT_ID, transactionStatus, INSTANCE_ID))
            .thenReturn(true);
        String transactionStatusString = transactionStatus.name();

        mockMvc.perform(put("/psu-api/v1/payment/{payment-id}/status/{status}", PAYMENT_ID, transactionStatusString)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).updatePaymentStatus(PAYMENT_ID, transactionStatus, INSTANCE_ID);
    }

    @Test
    void updatePaymentStatus_withFalseServiceResponse_shouldReturnBadRequest() throws Exception {
        TransactionStatus transactionStatus = TransactionStatus.ACCP;
        when(cmsPsuPisService.updatePaymentStatus(PAYMENT_ID, transactionStatus, INSTANCE_ID))
            .thenReturn(false);
        String transactionStatusString = transactionStatus.name();

        mockMvc.perform(put("/psu-api/v1/payment/{payment-id}/status/{status}", PAYMENT_ID, transactionStatusString)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isBadRequest())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).updatePaymentStatus(PAYMENT_ID, transactionStatus, INSTANCE_ID);
    }

    @Test
    void psuAuthorisationStatuses_withValidRequest_shouldReturnOk() throws Exception {
        String cmsPisPsuDataAuthorisationListJson = jsonReader.getStringFromFile("json/pis/response/cms-pis-psu-data-authorisation-list.json");
        CmsPisPsuDataAuthorisation cmsPisPsuDataAuthorisation = new CmsPisPsuDataAuthorisation(psuIdData, AUTHORISATION_ID, ScaStatus.RECEIVED, AuthorisationType.PIS_CREATION);
        when(cmsPsuPisService.getPsuDataAuthorisations(PAYMENT_ID, INSTANCE_ID, null, null))
            .thenReturn(Optional.of(Collections.singletonList(cmsPisPsuDataAuthorisation)));

        mockMvc.perform(get("/psu-api/v1/payment/{payment-id}/authorisation/psus", PAYMENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isOk())
            .andExpect(content().json(cmsPisPsuDataAuthorisationListJson));
    }

    @Test
    void psuAuthorisationStatuses_withEmptyServiceResponse_shouldReturnNotFound() throws Exception {
        when(cmsPsuPisService.getPsuDataAuthorisations(PAYMENT_ID, INSTANCE_ID, null, null))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/psu-api/v1/payment/{payment-id}/authorisation/psus", PAYMENT_ID)
                            .headers(INSTANCE_ID_HEADERS))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(EMPTY_BODY));

        verify(cmsPsuPisService).getPsuDataAuthorisations(PAYMENT_ID, INSTANCE_ID, null, null);
    }

    private CmsPaymentResponse buildCmsPaymentResponse() {
        CmsPaymentResponse cmsPaymentResponse = jsonReader.getObjectFromFile("json/pis/response/cms-payment-response-no-payment.json", CmsPaymentResponse.class);
        CmsBasePaymentResponse cmsPayment = buildCmsPayment();
        cmsPaymentResponse.setPayment(cmsPayment);
        return cmsPaymentResponse;
    }

    private PsuIdData buildPsuIdData() {
        return jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
    }

    private CmsBasePaymentResponse buildCmsPayment() {
        CmsSinglePayment cmsPayment = new CmsSinglePayment(PAYMENT_PRODUCT);
        cmsPayment.setPaymentId(PAYMENT_ID);
        return cmsPayment;
    }

    private static HttpHeaders buildInstanceIdHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(INSTANCE_ID_HEADER_NAME, INSTANCE_ID);
        return httpHeaders;
    }

    private static HttpHeaders buildPsuHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(PSU_ID_HEADER_NAME, PSU_ID);
        httpHeaders.add(PSU_ID_TYPE_HEADER_NAME, PSU_ID_TYPE);
        httpHeaders.add(PSU_CORPORATE_ID_HEADER_NAME, PSU_CORPORATE_ID);
        httpHeaders.add(PSU_CORPORATE_ID_TYPE_HEADER_NAME, PSU_CORPORATE_ID_TYPE);
        return httpHeaders;
    }
}
