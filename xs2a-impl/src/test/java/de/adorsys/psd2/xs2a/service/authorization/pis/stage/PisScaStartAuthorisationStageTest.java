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

package de.adorsys.psd2.xs2a.service.authorization.pis.stage;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisScaStartAuthorisationStageTest {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";
    private final List<String> ERROR_MESSAGE_TEXT = Arrays.asList("message 1", "message 2", "message 3");
    private static final String AUTHENTICATION_METHOD_ID = "sms";
    private static final String PAYMENT_ID = "123456789";
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(TEST_ASPSP_DATA.getBytes(), "");
    private static final SpiContextData CONTEXT_DATA = new SpiContextData(new SpiPsuData(null, null, null, null), new TppInfo());
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final List<SpiAuthenticationObject> ONE_SPI_SCA_METHOD = Collections.singletonList(buildSpiSmsAuthenticationObject());
    private static final List<SpiAuthenticationObject> NONE_SPI_SCA_METHOD = Collections.emptyList();

    @InjectMocks
    private PisScaStartAuthorisationStage pisScaStartAuthorisationStage;

    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private PaymentAuthorisationSpi paymentAuthorisationSpi;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SinglePaymentSpi singlePaymentSpi;
    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;

    @Mock
    private CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    @Mock
    private ApplicationContext applicationContext;

    @Before
    public void setUp() {
        ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
                                      .errorType(PIS_400)
                                      .messages(ERROR_MESSAGE_TEXT)
                                      .build();

        when(spiErrorMapper.mapToErrorHolder(any(SpiResponse.class), eq(ServiceType.PIS)))
            .thenReturn(errorHolder);

        when(pisAspspDataService.getAspspConsentData(PAYMENT_ID)).thenReturn(ASPSP_CONSENT_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(any(PsuIdData.class))).thenReturn(CONTEXT_DATA);
    }

    @Test
    public void apply_paymentAuthorisationSpi_authorisePsu_fail() {
        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");
        SpiResponse<SpiAuthorisationStatus> spiErrorMessage = SpiResponse.<SpiAuthorisationStatus>builder()
                                                                  .message(ERROR_MESSAGE_TEXT)
                                                                  .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                  .fail(SpiResponseStatus.LOGICAL_FAILURE);

        // generate an error
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any()))
            .thenReturn(spiErrorMessage);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisScaStartAuthorisationStage.apply(buildRequest(AUTHENTICATION_METHOD_ID, PAYMENT_ID), buildResponse(PAYMENT_ID));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder().getErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        assertThat(actualResponse.getErrorHolder().getMessage()).isEqualTo(errorMessagesString);
    }

    @Test
    public void apply_paymentAuthorisationSpi_requestAvailableScaMethods_fail() {
        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");
        SpiResponse<SpiAuthorisationStatus> spiStatus = SpiResponse.<SpiAuthorisationStatus>builder()
                                                            .payload(SpiAuthorisationStatus.SUCCESS)
                                                            .aspspConsentData(ASPSP_CONSENT_DATA).success();

        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any()))
            .thenReturn(spiStatus);

        // generate an error
        SpiResponse<List<SpiAuthenticationObject>> spiErrorMessage = SpiResponse.<List<SpiAuthenticationObject>>builder()
                                                                         .message(ERROR_MESSAGE_TEXT)
                                                                         .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                         .fail(SpiResponseStatus.TECHNICAL_FAILURE);

        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any()))
            .thenReturn(spiErrorMessage);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisScaStartAuthorisationStage.apply(buildRequest(AUTHENTICATION_METHOD_ID, PAYMENT_ID), buildResponse(PAYMENT_ID));
        // Then

        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder().getErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        assertThat(actualResponse.getErrorHolder().getMessage()).isEqualTo(errorMessagesString);
    }

    @Test
    public void apply_paymentAuthorisationSpi_requestAuthorisationCode_fail() {
        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");
        SpiResponse<SpiAuthorisationStatus> spiStatus = SpiResponse.<SpiAuthorisationStatus>builder()
                                                            .payload(SpiAuthorisationStatus.SUCCESS)
                                                            .aspspConsentData(ASPSP_CONSENT_DATA).success();


        SpiResponse<List<SpiAuthenticationObject>> availableScaMethodsResponse = SpiResponse.<List<SpiAuthenticationObject>>builder()
                                                                                     .payload(ONE_SPI_SCA_METHOD)
                                                                                     .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                                     .success();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any()))
            .thenReturn(spiStatus);

        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any()))
            .thenReturn(availableScaMethodsResponse);

        // generate an error
        SpiResponse<SpiAuthorizationCodeResult> spiErrorMessage = SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                      .message(ERROR_MESSAGE_TEXT)
                                                                      .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                      .fail(SpiResponseStatus.TECHNICAL_FAILURE);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any()))
            .thenReturn(spiErrorMessage);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisScaStartAuthorisationStage.apply(buildRequest(AUTHENTICATION_METHOD_ID, PAYMENT_ID), buildResponse(PAYMENT_ID));
        // Then

        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder().getErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        assertThat(actualResponse.getErrorHolder().getMessage()).isEqualTo(errorMessagesString);
    }


    @Test
    public void apply_singlePaymentSpi_executePaymentWithoutSca_fail() {
        String errorMessagesString = ERROR_MESSAGE_TEXT.toString().replace("[", "").replace("]", "");
        SpiResponse<SpiAuthorisationStatus> spiStatus = SpiResponse.<SpiAuthorisationStatus>builder()
                                                            .payload(SpiAuthorisationStatus.SUCCESS)
                                                            .aspspConsentData(ASPSP_CONSENT_DATA).success();


        SpiResponse<List<SpiAuthenticationObject>> availableScaMethodsResponse = SpiResponse.<List<SpiAuthenticationObject>>builder()
                                                                                     .payload(NONE_SPI_SCA_METHOD)
                                                                                     .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                                     .success();

        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any()))
            .thenReturn(spiStatus);

        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any()))
            .thenReturn(availableScaMethodsResponse);

        when(applicationContext.getBean(SinglePaymentSpi.class))
            .thenReturn(singlePaymentSpi);

        when(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(any(), any()))
            .thenReturn(new SpiSinglePayment(PAYMENT_PRODUCT));


        // generate an error
        SpiResponse<SpiPaymentExecutionResponse> spiErrorMessage = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                       .message(ERROR_MESSAGE_TEXT)
                                                                       .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                       .fail(SpiResponseStatus.TECHNICAL_FAILURE);

        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any()))
            .thenReturn(spiErrorMessage);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisScaStartAuthorisationStage.apply(buildRequest(AUTHENTICATION_METHOD_ID, PAYMENT_ID), buildResponse(PAYMENT_ID));

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder().getErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        assertThat(actualResponse.getErrorHolder().getMessage()).isEqualTo(errorMessagesString);
    }


    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildRequest(String authenticationMethodId, String paymentId) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setAuthenticationMethodId(authenticationMethodId);
        request.setPaymentId(paymentId);
        request.setPsuData(buildPsuIdData());
        return request;
    }

    private GetPisAuthorisationResponse buildResponse(String paymentId) {
        GetPisAuthorisationResponse pisAuthorisationResponse = new GetPisAuthorisationResponse();
        pisAuthorisationResponse.setPaymentType(PaymentType.SINGLE);
        pisAuthorisationResponse.setPaymentProduct(PAYMENT_PRODUCT);
        PisPaymentInfo pisPaymentInfo = buildPisPaymentInfo(paymentId);
        pisAuthorisationResponse.setPaymentInfo(pisPaymentInfo);
        pisAuthorisationResponse.setPayments(getPisPayment());
        return pisAuthorisationResponse;
    }

    private PisPaymentInfo buildPisPaymentInfo(String paymentId) {
        PisPaymentInfo pisPaymentInfo = new PisPaymentInfo();
        pisPaymentInfo.setPaymentProduct(PAYMENT_PRODUCT);
        pisPaymentInfo.setPaymentType(PaymentType.SINGLE);
        pisPaymentInfo.setPaymentId(paymentId);
        return pisPaymentInfo;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("id", "type", "corporate ID", "corporate type");
    }

    private static SpiAuthenticationObject buildSpiSmsAuthenticationObject() {
        SpiAuthenticationObject spiAuthenticationObject = new SpiAuthenticationObject();
        spiAuthenticationObject.setAuthenticationMethodId("sms");
        spiAuthenticationObject.setAuthenticationType("SMS_OTP");
        return spiAuthenticationObject;
    }

    private List<PisPayment> getPisPayment() {
        PisPayment pisPayment = new PisPayment();
        pisPayment.setTransactionStatus(TransactionStatus.RCVD);
        return Collections.singletonList(pisPayment);
    }
}
