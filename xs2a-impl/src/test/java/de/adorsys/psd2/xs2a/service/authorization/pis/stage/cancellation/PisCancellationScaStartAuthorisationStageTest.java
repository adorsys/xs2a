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

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
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

import javax.swing.plaf.metal.OceanTheme;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.FINALISED;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.PSUAUTHENTICATED;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisCancellationScaStartAuthorisationStageTest {
    private static final String PSU_ID = "Test psuId";
    private static final String PAYMENT_ID = "SeviKzWmPUDncnNz4F-f5gIUdnn78_IqZdZQvWhGeVlzr95yG8yF319Fm7h0bDeW_=_bS6p6XvTWI";
    private static final String PASSWORD = "Test password";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfer";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Test paymentId");
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final PsuIdData PSU_ID_DATA_WRONG = new PsuIdData("Wrong PSU", null, null, null);
    private static final TransactionStatus PAYMENT_STATUS = TransactionStatus.RCVD;
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(PSU_ID, null, null, null);
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo());
    private static final SpiResponseStatus FAILURE_STATUS = SpiResponseStatus.UNAUTHORIZED_FAILURE;
    private static final List<SpiAuthenticationObject> MULTIPLE_SPI_SCA_METHODS = Arrays.asList(buildSpiSmsAuthenticationObject(), buildSpiPhotoAuthenticationObject());
    private static final List<SpiAuthenticationObject> ONE_SPI_SCA_METHOD = Collections.singletonList(buildSpiSmsAuthenticationObject());
    private static final List<SpiAuthenticationObject> NONE_SPI_SCA_METHOD = Collections.emptyList();
    private static final List<Xs2aAuthenticationObject> MULTIPLE_XS2A_SCA_METHODS = Arrays.asList(buildXs2aSmsAuthenticationObject(), buildXs2aPhotoAuthenticationObject());
    private static final List<Xs2aAuthenticationObject> ONE_XS2A_SCA_METHOD = Collections.singletonList(buildXs2aSmsAuthenticationObject());
    private static final List<Xs2aAuthenticationObject> NONE_XS2A_SCA_METHOD = Collections.emptyList();
    private static final GetPisAuthorisationResponse GET_PIS_CANCELLATION_AUTHORISATION_BY_ID = buildGetPisAuthorisationResponse();
    private static final PisPayment PIS_PAYMENT = new PisPayment();
    private static final SinglePayment XS2A_PAYMENT = new SinglePayment();

    @InjectMocks
    private PisCancellationScaStartAuthorisationStage pisCancellationScaStartAuthorisationStage;
    @Mock
    private PaymentCancellationSpi paymentCancellationSpi;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataRequest xs2aUpdatePisCommonPaymentPsuDataRequest;
    @Mock
    private GetPisAuthorisationResponse getPisAuthorisationResponse;
    @Mock
    private PisPsuDataService pisPsuDataService;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @Mock
    private CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;

    @Before
    public void setUp() {
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.getPaymentId()).thenReturn(PAYMENT_ID);
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.getAuthorisationId()).thenReturn(AUTHORISATION_ID);
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.getPassword()).thenReturn(PASSWORD);
        when(getPisAuthorisationResponse.getPaymentProduct()).thenReturn(PAYMENT_PRODUCT);
        when(getPisAuthorisationResponse.getPaymentType()).thenReturn(PAYMENT_TYPE);
        when(pisAspspDataService.getAspspConsentData(PAYMENT_ID)).thenReturn(ASPSP_CONSENT_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA)).thenReturn(SPI_PSU_DATA);
        doNothing().when(pisAspspDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);
        when(xs2aPisCommonPaymentService.saveAuthenticationMethods(any(), anyListOf(Xs2aAuthenticationObject.class))).thenReturn(true);
        when(pisCommonPaymentServiceEncrypted.getPisCancellationAuthorisationById(AUTHORISATION_ID)).thenReturn(Optional.of(buildGetPisAuthorisationResponse()));
        when(getPisAuthorisationResponse.getPayments()).thenReturn(Collections.singletonList(PIS_PAYMENT));
        when(cmsToXs2aPaymentMapper.mapToSinglePayment(PIS_PAYMENT)).thenReturn(XS2A_PAYMENT);
        when(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(XS2A_PAYMENT, PAYMENT_PRODUCT)).thenReturn(buildSpiPayment());
        when(pisCommonPaymentServiceEncrypted.updateCommonPaymentStatusById(PAYMENT_ID, TransactionStatus.RJCT)).thenReturn(Optional.of(true));
    }

    @Test
    public void apply_Identification_Success() {
        //Given
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.isUpdatePsuIdentification()).thenReturn(true);
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.getPsuData()).thenReturn(PSU_ID_DATA);
        when(pisPsuDataService.getPsuDataByPaymentId(PAYMENT_ID)).thenReturn(Collections.singletonList(PSU_ID_DATA));
        //When
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisCancellationScaStartAuthorisationStage.apply(xs2aUpdatePisCommonPaymentPsuDataRequest, getPisAuthorisationResponse);
        //Then
        assertThat(response.getScaStatus()).isEqualTo(ScaStatus.PSUIDENTIFIED);
        assertThat(response.getPsuId()).isEqualTo(PSU_ID);
    }

    @Test
    public void apply_Identification_NoPsu_Failure() {
        //Given
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.isUpdatePsuIdentification()).thenReturn(true);
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.getPsuData()).thenReturn(null);
        //When
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisCancellationScaStartAuthorisationStage.apply(xs2aUpdatePisCommonPaymentPsuDataRequest, getPisAuthorisationResponse);
        //Then
        assertThat(response.getScaStatus()).isEqualTo(ScaStatus.FAILED);
        assertThat(response.getErrorHolder().getErrorType()).isEqualTo(ErrorType.PIS_400);
        assertThat(response.getErrorHolder().getErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }

    @Test
    public void apply_Identification_WrongPsu_Failure() {
        //Given
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.isUpdatePsuIdentification()).thenReturn(true);
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.getPsuData()).thenReturn(PSU_ID_DATA_WRONG);
        //When
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisCancellationScaStartAuthorisationStage.apply(xs2aUpdatePisCommonPaymentPsuDataRequest, getPisAuthorisationResponse);
        //Then
        assertThat(response.getScaStatus()).isEqualTo(ScaStatus.FAILED);
        assertThat(response.getErrorHolder().getErrorType()).isEqualTo(ErrorType.PIS_401);
        assertThat(response.getErrorHolder().getErrorCode()).isEqualTo(MessageErrorCode.PSU_CREDENTIALS_INVALID);
    }

    @Test
    public void apply_Authorisation_NoAvailableScaMethod_success() {
        //Given
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.isUpdatePsuIdentification()).thenReturn(false);
        when(paymentCancellationSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, buildSpiPayment(), ASPSP_CONSENT_DATA)).thenReturn(buildSuccessfulSpiResponse(SpiAuthorisationStatus.SUCCESS));
        when(paymentCancellationSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, buildSpiPayment(), ASPSP_CONSENT_DATA)).thenReturn(buildSuccessfulSpiResponse(NONE_SPI_SCA_METHOD));
        when(paymentCancellationSpi.cancelPaymentWithoutSca(SPI_CONTEXT_DATA, buildSpiPayment(), ASPSP_CONSENT_DATA)).thenReturn(buildSuccessfulSpiResponse(SpiResponse.voidResponse()));

        //When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCancellationScaStartAuthorisationStage.apply(xs2aUpdatePisCommonPaymentPsuDataRequest, getPisAuthorisationResponse);

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getScaStatus()).isEqualTo(FINALISED);
    }

    @Test
    public void apply_Authorisation_SingleAvailableScaMethod_success() {
        //Given
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.isUpdatePsuIdentification()).thenReturn(false);
        when(paymentCancellationSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, buildSpiPayment(), ASPSP_CONSENT_DATA)).thenReturn(buildSuccessfulSpiResponse(SpiAuthorisationStatus.SUCCESS));
        when(paymentCancellationSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, buildSpiPayment(), ASPSP_CONSENT_DATA)).thenReturn(buildSuccessfulSpiResponse(ONE_SPI_SCA_METHOD));
        when(paymentCancellationSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, buildSpiSmsAuthenticationObject().getAuthenticationMethodId(), buildSpiPayment(), ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessfulSpiResponse(new SpiAuthorizationCodeResult()));
        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(buildSpiSmsAuthenticationObject())).thenReturn(buildXs2aSmsAuthenticationObject());

        //When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCancellationScaStartAuthorisationStage.apply(xs2aUpdatePisCommonPaymentPsuDataRequest, getPisAuthorisationResponse);

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getChosenScaMethod()).isEqualTo(buildXs2aSmsAuthenticationObject());
        assertThat(actualResponse.getScaStatus()).isEqualTo(SCAMETHODSELECTED);
    }

    @Test
    public void apply_Authorisation_MultipleAvailableScaMethod_success() {
        //Given
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.isUpdatePsuIdentification()).thenReturn(false);
        when(paymentCancellationSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, buildSpiPayment(), ASPSP_CONSENT_DATA)).thenReturn(buildSuccessfulSpiResponse(SpiAuthorisationStatus.SUCCESS));
        when(paymentCancellationSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, buildSpiPayment(), ASPSP_CONSENT_DATA)).thenReturn(buildSuccessfulSpiResponse(MULTIPLE_SPI_SCA_METHODS));
        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(MULTIPLE_SPI_SCA_METHODS)).thenReturn(MULTIPLE_XS2A_SCA_METHODS);

        //When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCancellationScaStartAuthorisationStage.apply(xs2aUpdatePisCommonPaymentPsuDataRequest, getPisAuthorisationResponse);

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuId()).isEqualTo(PSU_ID);
        assertThat(actualResponse.getAvailableScaMethods()).isEqualTo(MULTIPLE_XS2A_SCA_METHODS);
        assertThat(actualResponse.getScaStatus()).isEqualTo(PSUAUTHENTICATED);
    }

    private <T> SpiResponse<T> buildSuccessfulSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }

    private <T> SpiResponse<T> buildFailureSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .fail(FAILURE_STATUS);
    }

    private SpiSinglePayment buildSpiPayment() {
        SpiSinglePayment payment = new SpiSinglePayment(PAYMENT_PRODUCT);
        payment.setPaymentId(PAYMENT_ID);
        payment.setPaymentStatus(PAYMENT_STATUS);
        return payment;
    }

    private static SpiAuthenticationObject buildSpiSmsAuthenticationObject() {
        SpiAuthenticationObject spiAuthenticationObject = new SpiAuthenticationObject();
        spiAuthenticationObject.setAuthenticationMethodId("sms");
        spiAuthenticationObject.setAuthenticationType("SMS_OTP");
        return spiAuthenticationObject;
    }

    private static SpiAuthenticationObject buildSpiPhotoAuthenticationObject() {
        SpiAuthenticationObject spiAuthenticationObject = new SpiAuthenticationObject();
        spiAuthenticationObject.setAuthenticationMethodId("photo");
        spiAuthenticationObject.setAuthenticationType("PHOTO_OTP");
        return spiAuthenticationObject;
    }

    private static Xs2aAuthenticationObject buildXs2aSmsAuthenticationObject() {
        Xs2aAuthenticationObject xs2aAuthenticationObject = new Xs2aAuthenticationObject();
        xs2aAuthenticationObject.setAuthenticationMethodId("sms");
        xs2aAuthenticationObject.setAuthenticationType("SMS_OTP");
        return xs2aAuthenticationObject;
    }

    private static Xs2aAuthenticationObject buildXs2aPhotoAuthenticationObject() {
        Xs2aAuthenticationObject xs2aAuthenticationObject = new Xs2aAuthenticationObject();
        xs2aAuthenticationObject.setAuthenticationMethodId("photo");
        xs2aAuthenticationObject.setAuthenticationType("PHOTO_OTP");
        return xs2aAuthenticationObject;
    }

    private static GetPisAuthorisationResponse buildGetPisAuthorisationResponse() {
        GetPisAuthorisationResponse response = new GetPisAuthorisationResponse();
        response.setPsuId(PSU_ID);
        return response;
    }
}
