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

package de.adorsys.psd2.xs2a.service.authorization.pis.stage.cancellation;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PisCancellationScaAuthenticatedStageTest {
    private static final String PAYMENT_PRODUCT = "Test payment product";
    private static final String PAYMENT_ID = "Test payment id";
    private static final String AUTHORISATION_ID = "Test authorisation id";
    private static final String AUTHENTICATION_METHOD_ID = "Test authentication method id";
    private static final String PSU_ID = "Test psuId";
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final TransactionStatus ACCP_TRANSACTION_STATUS = TransactionStatus.ACCP;
    private static final SpiResponseStatus FAILURE_RESPONSE_STATUS = SpiResponseStatus.LOGICAL_FAILURE;
    private static final ServiceType PIS_SERVICE_TYPE = ServiceType.PIS;
    private static final MessageErrorCode FORMAT_ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private static final MessageErrorCode SCA_METHOD_UNKNOWN = MessageErrorCode.SCA_METHOD_UNKNOWN;
    private static final ErrorType PIS_400_ERROR_TYPE = ErrorType.PIS_400;
    private static final ScaStatus FAILED_SCA_STATUS = ScaStatus.FAILED;
    private static final byte[] PAYMENT_DATA = "Test payment data".getBytes();
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(PSU_ID, null, null, null);
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo());
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final PisPaymentInfo PAYMENT_INFO = buildPisPaymentInfo();
    private static final SpiPaymentInfo SPI_PAYMENT_INFO = buildSpiPaymentInfo();

    @InjectMocks
    private PisCancellationScaAuthenticatedStage pisCancellationScaAuthenticatedStage;

    @Mock
    private PaymentCancellationSpi paymentCancellationSpi;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private PisCommonDecoupledService pisCommonDecoupledService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;

    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataRequest request;
    @Mock
    private GetPisAuthorisationResponse response;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse mockedExpectedResponse;
    @Mock
    private SpiAuthorizationCodeResult spiAuthorizationCodeResult;
    @Mock
    private SpiAuthenticationObject spiAuthenticationObject;
    @Mock
    private ChallengeData challengeData;
    @Mock
    private Xs2aAuthenticationObject xs2aAuthenticationObject;

    @Before
    public void setUp() {
        when(response.getPaymentType())
            .thenReturn(SINGLE_PAYMENT_TYPE);

        when(response.getPaymentProduct())
            .thenReturn(PAYMENT_PRODUCT);

        when(response.getPayments())
            .thenReturn(Collections.emptyList());

        when(response.getPaymentInfo())
            .thenReturn(PAYMENT_INFO);

        when(request.getAuthorisationId())
            .thenReturn(AUTHORISATION_ID);

        when(request.getAuthenticationMethodId())
            .thenReturn(AUTHENTICATION_METHOD_ID);
    }

    @Test
    public void apply_Success_decoupledApproach() {
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(true);

        doNothing()
            .when(scaApproachResolver).forceDecoupledScaApproach();

        when(pisCommonDecoupledService.proceedDecoupledCancellation(request, SPI_PAYMENT_INFO, AUTHENTICATION_METHOD_ID))
            .thenReturn(mockedExpectedResponse);

        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCancellationScaAuthenticatedStage.apply(request, response);

        assertThat(actualResponse).isNotNull();
        verify(scaApproachResolver).forceDecoupledScaApproach();
        verify(pisCommonDecoupledService).proceedDecoupledCancellation(request, SPI_PAYMENT_INFO, AUTHENTICATION_METHOD_ID);
    }

    @Test
    public void apply_Failure_embeddedApproach_spiResponseHasError() {
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(false);

        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);

        when(request.getPaymentId())
            .thenReturn(PAYMENT_ID);

        when(pisAspspDataService.getAspspConsentData(PAYMENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = buildErrorSpiResponse();

        when(paymentCancellationSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, SPI_PAYMENT_INFO, ASPSP_CONSENT_DATA))
            .thenReturn(spiResponse);

        doNothing()
            .when(pisAspspDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);

        when(spiErrorMapper.mapToErrorHolder(spiResponse, PIS_SERVICE_TYPE))
            .thenReturn(ErrorHolder.builder(FORMAT_ERROR_CODE).errorType(PIS_400_ERROR_TYPE).build());

        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCancellationScaAuthenticatedStage.apply(request, response);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getErrorHolder().getErrorType()).isEqualTo(PIS_400_ERROR_TYPE);
    }

    @Test
    public void apply_Failure_embeddedApproach_authorizationCodeResultIsEmpty() {
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(false);

        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);

        when(request.getPaymentId())
            .thenReturn(PAYMENT_ID);

        when(pisAspspDataService.getAspspConsentData(PAYMENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = buildSuccessSpiResponse();

        when(paymentCancellationSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, SPI_PAYMENT_INFO, ASPSP_CONSENT_DATA))
            .thenReturn(spiResponse);

        doNothing()
            .when(pisAspspDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);

        when(spiAuthorizationCodeResult.isEmpty())
            .thenReturn(true);

        when(spiErrorMapper.mapToErrorHolder(spiResponse, PIS_SERVICE_TYPE))
            .thenReturn(ErrorHolder.builder(FORMAT_ERROR_CODE).errorType(PIS_400_ERROR_TYPE).build());

        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCancellationScaAuthenticatedStage.apply(request, response);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getErrorHolder().getErrorCode()).isEqualTo(SCA_METHOD_UNKNOWN);
    }

    @Test
    public void apply_Success_embeddedApproach() {
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(false);

        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);

        when(request.getPaymentId())
            .thenReturn(PAYMENT_ID);

        when(pisAspspDataService.getAspspConsentData(PAYMENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = buildSuccessSpiResponse();

        when(paymentCancellationSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, SPI_PAYMENT_INFO, ASPSP_CONSENT_DATA))
            .thenReturn(spiResponse);

        doNothing()
            .when(pisAspspDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);

        when(spiAuthorizationCodeResult.isEmpty())
            .thenReturn(false);

        when(spiAuthorizationCodeResult.getSelectedScaMethod())
            .thenReturn(spiAuthenticationObject);

        when(spiAuthorizationCodeResult.getChallengeData())
            .thenReturn(challengeData);

        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(spiAuthenticationObject))
            .thenReturn(xs2aAuthenticationObject);


        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCancellationScaAuthenticatedStage.apply(request, response);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getChosenScaMethod()).isEqualTo(xs2aAuthenticationObject);
        assertThat(actualResponse.getChallengeData()).isEqualTo(challengeData);
    }

    private static PisPaymentInfo buildPisPaymentInfo() {
        PisPaymentInfo paymentInfo = new PisPaymentInfo();
        paymentInfo.setPaymentData(PAYMENT_DATA);
        paymentInfo.setPaymentId(PAYMENT_ID);
        paymentInfo.setPaymentProduct(PAYMENT_PRODUCT);
        paymentInfo.setPaymentType(SINGLE_PAYMENT_TYPE);
        paymentInfo.setTransactionStatus(ACCP_TRANSACTION_STATUS);
        return paymentInfo;
    }

    private static SpiPaymentInfo buildSpiPaymentInfo() {
        SpiPaymentInfo paymentInfo = new SpiPaymentInfo(PAYMENT_PRODUCT);
        paymentInfo.setPaymentData(PAYMENT_DATA);
        paymentInfo.setPaymentId(PAYMENT_ID);
        paymentInfo.setPaymentType(SINGLE_PAYMENT_TYPE);
        paymentInfo.setStatus(ACCP_TRANSACTION_STATUS);
        return paymentInfo;
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<SpiAuthorizationCodeResult> buildSuccessSpiResponse() {
        return SpiResponse.<SpiAuthorizationCodeResult>builder()
                   .payload(spiAuthorizationCodeResult)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse() {
        return SpiResponse.<T>builder()
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .fail(FAILURE_RESPONSE_STATUS);
    }
}
