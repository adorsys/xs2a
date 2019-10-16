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


package de.adorsys.psd2.xs2a.service.authorization.pis.stage.initiation;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisDecoupledScaStartAuthorisationStageTest {
    private static final String PAYMENT_PRODUCT = "Test payment product";
    private static final String PAYMENT_ID = "Test payment id";
    private static final String PSU_ID = "Test psuId";
    private static final String PASSWORD = "Test password";
    private static final String AUTHORISATION = "Bearer 1111111";
    private static final String AUTHORISATION_ID = "Test authorisation";
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final ServiceType PIS_SERVICE_TYPE = ServiceType.PIS;
    private static final ErrorType PIS_400_ERROR_TYPE = ErrorType.PIS_400;
    private static final ScaStatus FAILED_SCA_STATUS = ScaStatus.FAILED;
    private static final TransactionStatus ACCP_TRANSACTION_STATUS = TransactionStatus.ACCP;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(PSU_ID, null, null, null, null);
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo(), UUID.randomUUID(), UUID.randomUUID(), AUTHORISATION);
    private static final byte[] PAYMENT_DATA = "Test payment data".getBytes();
    private static final PisPaymentInfo PAYMENT_INFO = buildPisPaymentInfo();
    private static final SpiPaymentInfo SPI_PAYMENT_INFO = buildSpiPaymentInfo();
    private static final GetPisAuthorisationResponse PIS_AUTHORISATION_RESPONSE = new GetPisAuthorisationResponse();

    @InjectMocks
    private PisDecoupledScaReceivedAuthorisationStage pisDecoupledScaStartAuthorisationStage;

    @Mock
    private PaymentAuthorisationSpi paymentAuthorisationSpi;
    @Mock
    private PisCommonDecoupledService pisCommonDecoupledService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataRequest request;
    @Mock
    private GetPisAuthorisationResponse response;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse mockedExpectedResponse;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @Before
    public void setUp() {
        PIS_AUTHORISATION_RESPONSE.setPsuIdData(PSU_ID_DATA);

        when(response.getPaymentType())
            .thenReturn(SINGLE_PAYMENT_TYPE);

        when(response.getPaymentProduct())
            .thenReturn(PAYMENT_PRODUCT);

        when(response.getPayments())
            .thenReturn(Collections.emptyList());

        when(response.getPaymentInfo())
            .thenReturn(PAYMENT_INFO);

        when(request.getPaymentId())
            .thenReturn(PAYMENT_ID);

        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuData(PSU_ID_DATA))
            .thenReturn(SPI_PSU_DATA);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);

        when(request.getPassword())
            .thenReturn(PASSWORD);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(Collections.singletonList(PSU_ID_DATA)))
            .thenReturn(Collections.singletonList(SPI_PSU_DATA));
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);
    }

    @Test
    public void apply_Failure_spiResponseHasError() {
        SpiResponse<SpiPsuAuthorisationResponse> expectedResponse = buildErrorSpiResponse();

        when(paymentAuthorisationSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        when(spiErrorMapper.mapToErrorHolder(expectedResponse, PIS_SERVICE_TYPE))
            .thenReturn(ErrorHolder
                            .builder(PIS_400_ERROR_TYPE)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                            .build());

        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisDecoupledScaStartAuthorisationStage.apply(request, response);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getErrorHolder().getErrorType()).isEqualTo(PIS_400_ERROR_TYPE);
    }

    @Test
    public void apply_Success() {
        SpiResponse<SpiPsuAuthorisationResponse> expectedResponse = buildSuccessSpiResponse();

        when(paymentAuthorisationSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        when(pisCommonDecoupledService.proceedDecoupledInitiation(request, SPI_PAYMENT_INFO))
            .thenReturn(mockedExpectedResponse);

        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisDecoupledScaStartAuthorisationStage.apply(request, response);

        assertThat(actualResponse).isNotNull();
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(request, SPI_PAYMENT_INFO);
    }

    @Test
    public void apply_Success_withoutHeaders() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setPaymentId(PAYMENT_ID);
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setPassword(PASSWORD);

        SpiResponse<SpiPsuAuthorisationResponse> expectedResponse = buildSuccessSpiResponse();

        when(paymentAuthorisationSpi.authorisePsu(SPI_CONTEXT_DATA, SPI_PSU_DATA, PASSWORD, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        when(pisCommonPaymentServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(PIS_AUTHORISATION_RESPONSE));

        when(pisCommonDecoupledService.proceedDecoupledInitiation(request, SPI_PAYMENT_INFO))
            .thenReturn(mockedExpectedResponse);

        ArgumentCaptor<Xs2aUpdatePisCommonPaymentPsuDataRequest> captor = ArgumentCaptor.forClass(Xs2aUpdatePisCommonPaymentPsuDataRequest.class);

        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisDecoupledScaStartAuthorisationStage.apply(request, response);

        assertThat(actualResponse).isNotNull();
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(captor.capture(), ArgumentMatchers.eq(SPI_PAYMENT_INFO));

        Xs2aUpdatePisCommonPaymentPsuDataRequest captured = captor.getValue();
        assertThat(captured).isNotNull();
        assertThat(captured.getPsuData().isEmpty()).isFalse();
        assertThat(captured.getPsuData()).isEqualTo(PSU_ID_DATA);
    }

    private static PisPaymentInfo buildPisPaymentInfo() {
        PisPaymentInfo paymentInfo = new PisPaymentInfo();
        paymentInfo.setPaymentData(PAYMENT_DATA);
        paymentInfo.setPaymentId(PAYMENT_ID);
        paymentInfo.setPaymentProduct(PAYMENT_PRODUCT);
        paymentInfo.setPaymentType(SINGLE_PAYMENT_TYPE);
        paymentInfo.setTransactionStatus(ACCP_TRANSACTION_STATUS);
        paymentInfo.setPsuDataList(Collections.singletonList(PSU_ID_DATA));
        return paymentInfo;
    }

    private static SpiPaymentInfo buildSpiPaymentInfo() {
        SpiPaymentInfo paymentInfo = new SpiPaymentInfo(PAYMENT_PRODUCT);
        paymentInfo.setPaymentData(PAYMENT_DATA);
        paymentInfo.setPaymentId(PAYMENT_ID);
        paymentInfo.setPaymentType(SINGLE_PAYMENT_TYPE);
        paymentInfo.setStatus(ACCP_TRANSACTION_STATUS);
        paymentInfo.setPsuDataList(Collections.singletonList(SPI_PSU_DATA));
        return paymentInfo;
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<SpiPsuAuthorisationResponse> buildSuccessSpiResponse() {
        return SpiResponse.<SpiPsuAuthorisationResponse>builder()
                   .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse() {
        return SpiResponse.<T>builder()
                   .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                   .build();
    }
}
