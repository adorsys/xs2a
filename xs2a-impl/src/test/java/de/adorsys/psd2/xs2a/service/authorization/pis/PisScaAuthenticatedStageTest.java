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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.PisScaAuthenticatedStage;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisScaAuthenticatedStageTest {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";
    private static final String AUTHENTICATION_METHOD_ID = "sms";
    private static final String AUTHENTICATION_METHOD_ID_UNKNOWN = "unknown";
    private static final String PAYMENT_ID = "123456789";
    private final AspspConsentData aspspConsentData = new AspspConsentData(TEST_ASPSP_DATA.getBytes(), "");
    private final SpiContextData contextData = new SpiContextData(new SpiPsuData(null, null, null, null), new TppInfo());

    @InjectMocks
    private PisScaAuthenticatedStage pisScaAuthenticatedStage;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private PaymentAuthorisationSpi paymentAuthorisationSpi;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;

    @Before
    public void setUp() {
        when(pisAspspDataService.getAspspConsentData(PAYMENT_ID)).thenReturn(aspspConsentData);
        when(spiContextDataProvider.provideWithPsuIdData(any(PsuIdData.class))).thenReturn(contextData);
        doNothing().when(pisAspspDataService).updateAspspConsentData(any(AspspConsentData.class));
    }

    @Test
    public void requestAuthorisationCode_Success() {
        //Given
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        ChallengeData challengeData = new ChallengeData(null, "some data", "some link", 100, null, "info");
        spiAuthorizationCodeResult.setChallengeData(challengeData);

        when(paymentAuthorisationSpi.requestAuthorisationCode(contextData, AUTHENTICATION_METHOD_ID, buildSpiPaymentInfo(PAYMENT_ID), aspspConsentData))
            .thenReturn(buildSpiResponse(spiAuthorizationCodeResult));
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisScaAuthenticatedStage.apply(buildRequest(AUTHENTICATION_METHOD_ID, PAYMENT_ID), buildResponse(PAYMENT_ID));
        // Then
        assertEquals(response.getScaStatus(), ScaStatus.SCAMETHODSELECTED);
    }

    @Test
    public void requestAuthorisationCode_Failure_ScaMethodUnknown() {
        //Given
        SpiAuthorizationCodeResult spiAuthorizationCodeResultScaMethodUnknown = new SpiAuthorizationCodeResult();
        spiAuthorizationCodeResultScaMethodUnknown.setChallengeData(new ChallengeData());

        when(paymentAuthorisationSpi.requestAuthorisationCode(contextData, AUTHENTICATION_METHOD_ID_UNKNOWN, buildSpiPaymentInfo(PAYMENT_ID), aspspConsentData))
            .thenReturn(buildSpiResponse(spiAuthorizationCodeResultScaMethodUnknown));
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisScaAuthenticatedStage.apply(buildRequest(AUTHENTICATION_METHOD_ID_UNKNOWN, PAYMENT_ID), buildResponse(PAYMENT_ID));
        // Then
        assertEquals(response.getScaStatus(), ScaStatus.FAILED);
        assertEquals(response.getErrorHolder().getErrorCode(), MessageErrorCode.SCA_METHOD_UNKNOWN);
    }

    private SpiResponse<SpiAuthorizationCodeResult> buildSpiResponse(SpiAuthorizationCodeResult spiAuthorizationCodeResult) {
        return SpiResponse.<SpiAuthorizationCodeResult>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(spiAuthorizationCodeResult)
                   .success();
    }

    private SpiPaymentInfo buildSpiPaymentInfo(String paymentId) {
        SpiPaymentInfo spiPayment = new SpiPaymentInfo("sepa-credit-transfers");
        spiPayment.setPaymentId(paymentId);
        spiPayment.setPaymentType(PaymentType.SINGLE);
        spiPayment.setStatus(null);
        spiPayment.setPaymentData(null);
        return spiPayment;
    }

    private GetPisAuthorisationResponse buildResponse(String paymentId) {
        GetPisAuthorisationResponse pisAuthorisationResponse = new GetPisAuthorisationResponse();
        pisAuthorisationResponse.setPaymentType(PaymentType.SINGLE);
        pisAuthorisationResponse.setPaymentProduct("sepa-credit-transfers");
        PisPaymentInfo pisPaymentInfo = buildPisPaymentInfo(paymentId);
        pisAuthorisationResponse.setPaymentInfo(pisPaymentInfo);
        return pisAuthorisationResponse;
    }

    private PisPaymentInfo buildPisPaymentInfo(String paymentId) {
        PisPaymentInfo pisPaymentInfo = new PisPaymentInfo();
        pisPaymentInfo.setPaymentProduct("sepa-credit-transfers");
        pisPaymentInfo.setPaymentType(PaymentType.SINGLE);
        pisPaymentInfo.setPaymentId(paymentId);
        return pisPaymentInfo;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildRequest(String authenticationMethodId, String paymentId) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setAuthenticationMethodId(authenticationMethodId);
        request.setPaymentId(paymentId);
        request.setPsuData(buildPsuIdData());
        return request;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("id", "type", "corporate ID", "corporate type");
    }
}
