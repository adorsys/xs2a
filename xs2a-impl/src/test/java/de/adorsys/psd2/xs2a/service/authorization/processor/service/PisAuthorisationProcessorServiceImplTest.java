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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.pis.DecoupledPisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.EmbeddedPisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.spi.payment.SpiPaymentServiceResolver;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_401;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisAuthorisationProcessorServiceImplTest {
    private static final String TEST_PAYMENT_ID = "12345676";
    private static final String TEST_AUTHORISATION_ID = "assddsff";
    private static final PsuIdData TEST_PSU_DATA = new PsuIdData("test-user", null, null, null, null);
    private static final ScaApproach TEST_SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final ScaStatus TEST_SCA_STATUS = ScaStatus.RECEIVED;
    private static final String TEST_PAYMENT_PRODUCT = "sepa- credit-transfers";
    private static final SpiSinglePayment TEST_SPI_SINGLE_PAYMENT = new SpiSinglePayment(TEST_PAYMENT_PRODUCT);
    private static final String TEST_AUTHENTICATION_METHOD_ID = "sms";
    private static final String TEST_AUTHENTICATION_TYPE = "SMS_OTP";
    private static final ErrorType TEST_ERROR_TYPE_400 = PIS_400;
    private static final TransactionStatus TEST_TRANSACTION_STATUS_SUCCESS = TransactionStatus.ACSC;
    private static final TransactionStatus TEST_TRANSACTION_STATUS_MULTILEVEL_SCA = TransactionStatus.PATC;

    @InjectMocks
    private PisAuthorisationProcessorServiceImpl pisAuthorisationProcessorService;
    @Mock
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    @Mock
    private SpiPaymentServiceResolver spiPaymentServiceResolver;
    @Mock
    private SinglePaymentSpi singlePaymentSpi;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private PisCommonDecoupledService pisCommonDecoupledService;
    @Mock
    private PaymentAuthorisationSpi paymentAuthorisationSpi;
    @Mock
    private SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    @Mock
    private List<PisScaAuthorisationService> services;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    @Test
    public void updateAuthorisation_success() {
        // Given
        EmbeddedPisScaAuthorisationService embeddedPisScaAuthorisationService = Mockito.mock(EmbeddedPisScaAuthorisationService.class);
        DecoupledPisScaAuthorisationService decoupledPisScaAuthorisationService = Mockito.mock(DecoupledPisScaAuthorisationService.class);
        services = Arrays.asList(embeddedPisScaAuthorisationService, decoupledPisScaAuthorisationService);

        when(embeddedPisScaAuthorisationService.getScaApproachServiceType()).thenReturn(ScaApproach.EMBEDDED);

        PisAuthorisationProcessorServiceImpl pisAuthorisationProcessorService = new PisAuthorisationProcessorServiceImpl(null, services, null, null, null, null, null, null, null, null, null, null, null, null, null);

        //When
        pisAuthorisationProcessorService.updateAuthorisation(buildAuthorisationProcessorRequest(), buildAuthorisationProcessorResponse());

        //Then
        verify(embeddedPisScaAuthorisationService).updateAuthorisation(any(), eq(buildAuthorisationProcessorResponse()));
    }

    @Test
    public void doScaReceived_authorisation_no_sca_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(new SpiAvailableScaMethodsResponse(false, Collections.emptyList()))
                                                                                                     .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                                            .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaReceived_authorisation_one_sca_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(buildSpiAuthorizationCodeResult())
                                                                                                          .build());
        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(buildSpiAuthenticationObjectSingleValueList().get(0))).thenReturn(buildXs2aAuthenticationObject());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.SCAMETHODSELECTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        assertThat(actual.getChosenScaMethod()).isEqualTo(buildXs2aAuthenticationObject());
        assertThat(actual.getChallengeData()).isEqualTo(buildChallengeData());
        verify(xs2aPisCommonPaymentService).saveAuthenticationMethods(eq(TEST_AUTHORISATION_ID), any());
    }

    @Test
    public void doScaReceived_authorisation_multiple_sca_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildMultipleScaMethodsResponse())
                                                                                                     .build());
        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(buildSpiAuthenticationObjectList())).thenReturn(buildXs2aAuthenticationObjectList());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.PSUAUTHENTICATED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        assertThat(actual.getAvailableScaMethods()).isEqualTo(buildXs2aAuthenticationObjectList());
        verify(xs2aPisCommonPaymentService).saveAuthenticationMethods(eq(TEST_AUTHORISATION_ID), any());
    }

    @Test
    public void doScaReceived_authorisation_authorise_Psu_with_error_failure() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaReceived_authorisation_authorise_Psu_incorrect_credentials_failure() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(spiResponse);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual);
        ErrorHolder errorHolder = actual.getErrorHolder();
        TppMessageInformation tppMessageInformation = errorHolder.getTppMessageInformationList().get(0);
        assertNotNull(errorHolder);
        assertThat(errorHolder.getErrorType()).isEqualTo(PIS_401);
        assertThat(tppMessageInformation.getCategory()).isEqualTo(MessageCategory.ERROR);
        assertThat(tppMessageInformation.getMessageErrorCode()).isEqualTo(MessageErrorCode.PSU_CREDENTIALS_INVALID);
    }

    @Test
    public void doScaReceived_authorisation_authorise_Psu_exemption_success() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(true, SpiAuthorisationStatus.SUCCESS))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                                            .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaReceived_authorisation_authorise_Psu_sca_exemption_payment_execution_failure() {

        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(true, SpiAuthorisationStatus.SUCCESS))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                            .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaReceived_authorisation_decoupled_chosen_success() {
        // Given
        AuthorisationProcessorRequest request = buildAuthorisationProcessorRequest();
        GetPisAuthorisationResponse pisAuthorisationResponse = (GetPisAuthorisationResponse) request.getAuthorisation();
        pisAuthorisationResponse.setChosenScaApproach(ScaApproach.DECOUPLED);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());

        // When
        pisAuthorisationProcessorService.doScaReceived(request);

        // Then
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
    }

    @Test
    public void doScaReceived_authorisation_request_availableScaMethods_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                                     .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaReceived_authorisation_request_availableScaMethods_exemption_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                                            .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaReceived_authorisation_request_availableScaMethods_exemption_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                            .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaReceived_authorisation_no_sca_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(new SpiAvailableScaMethodsResponse(false, Collections.emptyList()))
                                                                                                     .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                            .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaReceived_authorisation_one_sca_decoupled_chosen_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.getAvailableScaMethods().get(0).setDecoupled(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());

        // When
        pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
    }

    @Test
    public void doScaReceived_authorisation_one_sca_requestAuthorisationCode_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                                          .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaReceived_authorisation_request_requestAuthorisationCode_exemption_success() {
        // Given
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        SpiAuthorizationCodeResult result = buildSpiAuthorizationCodeResult();
        result.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(result)
                                                                                                          .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                                            .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaReceived_authorisation_request_requestAuthorisationCode_exemption_payment_execution_failure() {
        // Given
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        SpiAuthorizationCodeResult result = buildSpiAuthorizationCodeResult();
        result.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(result)
                                                                                                          .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                            .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaReceived_identification_success() {
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildIdentificationAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.PSUIDENTIFIED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test
    public void doScaReceived_identification_no_psu_failure() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildIdentificationAuthorisationProcessorRequest();
        ((Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest()).setPsuData(null);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(authorisationProcessorRequest);

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
    }

    @Test
    public void doScaPsuIdentified_authorisation_no_sca_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(new SpiAvailableScaMethodsResponse(false, Collections.emptyList()))
                                                                                                     .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                                            .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaPsuIdentified_authorisation_one_sca_success() {
        // Given
        TEST_SPI_SINGLE_PAYMENT.setPaymentId(TEST_PAYMENT_ID);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(buildSpiAuthorizationCodeResult())
                                                                                                          .build());
        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(buildSpiAuthenticationObjectSingleValueList().get(0))).thenReturn(buildXs2aAuthenticationObject());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.SCAMETHODSELECTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        assertThat(actual.getChosenScaMethod()).isEqualTo(buildXs2aAuthenticationObject());
        assertThat(actual.getChallengeData()).isEqualTo(buildChallengeData());
        verify(xs2aPisCommonPaymentService).saveAuthenticationMethods(eq(TEST_AUTHORISATION_ID), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_multiple_sca_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildMultipleScaMethodsResponse())
                                                                                                     .build());
        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(buildSpiAuthenticationObjectList())).thenReturn(buildXs2aAuthenticationObjectList());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.PSUAUTHENTICATED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        assertThat(actual.getAvailableScaMethods()).isEqualTo(buildXs2aAuthenticationObjectList());
        verify(xs2aPisCommonPaymentService).saveAuthenticationMethods(eq(TEST_AUTHORISATION_ID), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_authorise_Psu_with_error_failure() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_authorise_Psu_incorrect_credentials_failure() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(spiResponse);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual);
        ErrorHolder errorHolder = actual.getErrorHolder();
        TppMessageInformation tppMessageInformation = errorHolder.getTppMessageInformationList().get(0);
        assertNotNull(errorHolder);
        assertThat(errorHolder.getErrorType()).isEqualTo(PIS_401);
        assertThat(tppMessageInformation.getCategory()).isEqualTo(MessageCategory.ERROR);
        assertThat(tppMessageInformation.getMessageErrorCode()).isEqualTo(MessageErrorCode.PSU_CREDENTIALS_INVALID);
    }

    @Test
    public void doScaPsuIdentified_authorisation_authorise_Psu_exemption_success() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(true, SpiAuthorisationStatus.SUCCESS))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                                            .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaPsuIdentified_authorisation_authorise_Psu_sca_exemption_payment_execution_failure() {

        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(true, SpiAuthorisationStatus.SUCCESS))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                            .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_decoupled_chosen_success() {
        // Given
        AuthorisationProcessorRequest request = buildAuthorisationProcessorRequest();
        GetPisAuthorisationResponse pisAuthorisationResponse = (GetPisAuthorisationResponse) request.getAuthorisation();
        pisAuthorisationResponse.setChosenScaApproach(ScaApproach.DECOUPLED);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());

        // When
        pisAuthorisationProcessorService.doScaPsuIdentified(request);

        // Then
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_request_availableScaMethods_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                                     .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_request_availableScaMethods_exemption_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                                            .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaPsuIdentified_authorisation_request_availableScaMethods_exemption_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                            .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_no_sca_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(new SpiAvailableScaMethodsResponse(false, Collections.emptyList()))
                                                                                                     .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                            .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_one_sca_decoupled_chosen_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.getAvailableScaMethods().get(0).setDecoupled(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());

        // When
        pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_one_sca_requestAuthorisationCode_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                                          .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaPsuIdentified_authorisation_request_requestAuthorisationCode_exemption_success() {
        // Given
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        SpiAuthorizationCodeResult result = buildSpiAuthorizationCodeResult();
        result.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(result)
                                                                                                          .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                                            .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaPsuIdentified_authorisation_request_requestAuthorisationCode_exemption_payment_execution_failure() {
        // Given
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                     .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        SpiAuthorizationCodeResult result = buildSpiAuthorizationCodeResult();
        result.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(result)
                                                                                                          .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                            .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                            .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaPsuIdentified_identification_success() {
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildIdentificationAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.PSUIDENTIFIED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test
    public void doScaPsuIdentified_identification_no_psu_failure() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildIdentificationAuthorisationProcessorRequest();
        ((Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest()).setPsuData(null);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(authorisationProcessorRequest);

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
    }

    @Test
    public void doScaPsuAuthenticated_embedded_success() {
        // Given
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest();

        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment((GetPisAuthorisationResponse) processorRequest.getAuthorisation(), PaymentType.SINGLE, TEST_PAYMENT_PRODUCT))
            .thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(buildSpiAuthorizationCodeResult())
                                                                                                          .build());
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.SCAMETHODSELECTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(spiToXs2aAuthenticationObjectMapper).mapToXs2aAuthenticationObject(buildSpiAuthorizationCodeResult().getSelectedScaMethod());
    }

    @Test
    public void doScaPsuAuthenticated_decoupled_success() {
        // Given
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(true);

        // When
        pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        verify(xs2aPisCommonPaymentService).updateScaApproach(TEST_AUTHORISATION_ID, ScaApproach.DECOUPLED);
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
    }

    @Test
    public void doScaPsuAuthenticated_embedded_spi_hasError_failure() {
        // Given
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                  .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                  .build();
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400)
                                                                                     .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                                                                     .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaPsuAuthenticated_embedded_empty_result_failure() {
        // Given
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                  .payload(buildEmptySpiAuthorizationCodeResult())
                                                                  .build();
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(spiResponse);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        ErrorHolder errorHolder = actual.getErrorHolder();
        TppMessageInformation tppMessageInformation = errorHolder.getTppMessageInformationList().get(0);
        assertNotNull(errorHolder);
        assertThat(errorHolder.getErrorType()).isEqualTo(PIS_400);
        assertThat(tppMessageInformation.getCategory()).isEqualTo(MessageCategory.ERROR);
        assertThat(tppMessageInformation.getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }

    @Test
    public void doScaPsuAuthenticated_embedded_sca_exemption_success() {
        // Given
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = buildSpiAuthorizationCodeResult();
        spiAuthorizationCodeResult.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                                                .payload(spiAuthorizationCodeResult)
                                                                                                                                .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                                  .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                                                                  .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaPsuAuthenticated_embedded_sca_exemption_payment_execution_failure() {
        // Given
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = buildSpiAuthorizationCodeResult();
        spiAuthorizationCodeResult.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                                                .payload(spiAuthorizationCodeResult)
                                                                                                                                .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                   .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                   .build();
        when(singlePaymentSpi.executePaymentWithoutSca(any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaPsuAuthenticated_embedded_sca_exemption_multilevel_status_success() {
        // Given
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(), any(), any())).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = buildSpiAuthorizationCodeResult();
        spiAuthorizationCodeResult.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                                                .payload(spiAuthorizationCodeResult)
                                                                                                                                .build());
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        when(singlePaymentSpi.executePaymentWithoutSca(any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                                  .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_MULTILEVEL_SCA))
                                                                                                                  .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_MULTILEVEL_SCA);
        verify(xs2aPisCommonPaymentService).updateMultilevelSca(TEST_PAYMENT_ID, true);
    }

    @Test
    public void doScaMethodSelected_success() {
        // Given
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                   .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_SUCCESS))
                                                                   .build();
        when(singlePaymentSpi.verifyScaAuthorisationAndExecutePayment(any(), any(), any(), any())).thenReturn(spiResponse);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaMethodSelected(buildAuthorisationProcessorRequest());

        //Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    public void doScaMethodSelected_multilevel_sca_success() {
        // Given
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                   .payload(buildSpiPaymentExecutionResponse(TEST_TRANSACTION_STATUS_MULTILEVEL_SCA))
                                                                   .build();
        when(singlePaymentSpi.verifyScaAuthorisationAndExecutePayment(any(), any(), any(), any())).thenReturn(spiResponse);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaMethodSelected(buildAuthorisationProcessorRequest());

        //Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(xs2aPisCommonPaymentService).updateMultilevelSca(TEST_PAYMENT_ID, true);
    }

    @Test
    public void doScaMethodSelected_verifySca_fail_failure() {
        // Given
        when(spiPaymentServiceResolver.getPaymentService(any(), any())).thenReturn(singlePaymentSpi);
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                   .error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                   .build();
        when(singlePaymentSpi.verifyScaAuthorisationAndExecutePayment(any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400)
                                                                                     .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                                                                     .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaMethodSelected(buildAuthorisationProcessorRequest());

        //Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    public void doScaExempted_success() {
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaExempted(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void doScaStarted_success() {
        // When
        pisAuthorisationProcessorService.doScaStarted(buildEmptyAuthorisationProcessorRequest());
    }

    @Test
    public void doScaFinalised_success() {
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaFinalised(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(((Xs2aUpdatePisCommonPaymentPsuDataResponse) actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void doScaFailed_success() {
        // When
        pisAuthorisationProcessorService.doScaStarted(buildEmptyAuthorisationProcessorRequest());
    }

    private AuthorisationProcessorRequest buildEmptyAuthorisationProcessorRequest() {
        return new PisAuthorisationProcessorRequest(null,
                                                    null,
                                                    null,
                                                    null);
    }

    private AuthorisationProcessorRequest buildAuthorisationProcessorRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setPaymentId(TEST_PAYMENT_ID);
        request.setAuthorisationId(TEST_AUTHORISATION_ID);
        request.setPsuData(TEST_PSU_DATA);
        GetPisAuthorisationResponse authorisation = new GetPisAuthorisationResponse();
        authorisation.setPaymentType(PaymentType.SINGLE);
        authorisation.setPaymentProduct(TEST_PAYMENT_PRODUCT);
        return new PisAuthorisationProcessorRequest(TEST_SCA_APPROACH,
                                                    TEST_SCA_STATUS,
                                                    request,
                                                    authorisation);
    }

    private AuthorisationProcessorRequest buildIdentificationAuthorisationProcessorRequest() {
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        ((Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest()).setUpdatePsuIdentification(true);
        return authorisationProcessorRequest;
    }

    private SpiAuthorizationCodeResult buildSpiAuthorizationCodeResult() {
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        SpiAuthenticationObject method = new SpiAuthenticationObject();
        method.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        method.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        spiAuthorizationCodeResult.setSelectedScaMethod(method);
        spiAuthorizationCodeResult.setChallengeData(buildChallengeData());
        return spiAuthorizationCodeResult;
    }

    private ChallengeData buildChallengeData() {
        return new ChallengeData(null, Collections.singletonList("some data"), "some link", 100, null, "info");
    }

    private SpiAuthorizationCodeResult buildEmptySpiAuthorizationCodeResult() {
        return new SpiAuthorizationCodeResult();
    }

    private SpiPaymentExecutionResponse buildSpiPaymentExecutionResponse(TransactionStatus status) {
        return new SpiPaymentExecutionResponse(status);
    }

    private AuthorisationProcessorResponse buildAuthorisationProcessorResponse() {
        return new AuthorisationProcessorResponse();
    }

    private SpiAvailableScaMethodsResponse buildMultipleScaMethodsResponse() {
        return new SpiAvailableScaMethodsResponse(false, buildSpiAuthenticationObjectList());
    }

    private List<Xs2aAuthenticationObject> buildXs2aAuthenticationObjectList() {
        List<Xs2aAuthenticationObject> xs2aAuthenticationObjects = new ArrayList<>();
        Xs2aAuthenticationObject sms = new Xs2aAuthenticationObject();
        sms.setAuthenticationType("SMS_OTP");
        sms.setAuthenticationMethodId("sms");
        xs2aAuthenticationObjects.add(sms);
        Xs2aAuthenticationObject push = new Xs2aAuthenticationObject();
        push.setAuthenticationType("PUSH_OTP");
        push.setAuthenticationMethodId("push");
        push.setDecoupled(true);
        xs2aAuthenticationObjects.add(push);
        return xs2aAuthenticationObjects;
    }

    private List<SpiAuthenticationObject> buildSpiAuthenticationObjectList() {
        List<SpiAuthenticationObject> spiAuthenticationObjects = new ArrayList<>();
        SpiAuthenticationObject sms = new SpiAuthenticationObject();
        sms.setAuthenticationType("SMS_OTP");
        sms.setAuthenticationMethodId("sms");
        spiAuthenticationObjects.add(sms);
        SpiAuthenticationObject push = new SpiAuthenticationObject();
        push.setAuthenticationType("PUSH_OTP");
        push.setAuthenticationMethodId("push");
        push.setDecoupled(true);
        spiAuthenticationObjects.add(push);
        return spiAuthenticationObjects;
    }

    private SpiAvailableScaMethodsResponse buildSingleScaMethodsResponse() {
        return new SpiAvailableScaMethodsResponse(false, buildSpiAuthenticationObjectSingleValueList());
    }

    private Xs2aAuthenticationObject buildXs2aAuthenticationObject() {
        Xs2aAuthenticationObject sms = new Xs2aAuthenticationObject();
        sms.setAuthenticationType("SMS_OTP");
        sms.setAuthenticationMethodId("sms");
        return sms;
    }

    private List<SpiAuthenticationObject> buildSpiAuthenticationObjectSingleValueList() {
        List<SpiAuthenticationObject> spiAuthenticationObjects = new ArrayList<>();
        SpiAuthenticationObject sms = new SpiAuthenticationObject();
        sms.setAuthenticationType("SMS_OTP");
        sms.setAuthenticationMethodId("sms");
        spiAuthenticationObjects.add(sms);
        return spiAuthenticationObjects;
    }
}
