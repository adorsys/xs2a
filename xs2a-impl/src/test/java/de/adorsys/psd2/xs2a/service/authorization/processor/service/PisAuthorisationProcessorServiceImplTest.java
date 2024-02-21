/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreatePaymentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.*;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiMessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiTppMessage;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiTppMessageInformation;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaApproach;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaStatus;
import de.adorsys.psd2.xs2a.spi.service.CurrencyConversionInfoSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_400;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_401;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisAuthorisationProcessorServiceImplTest {
    private static final String TEST_PAYMENT_ID = "12345676";
    private static final String TEST_AUTHORISATION_ID = "assddsff";
    private static final PsuIdData TEST_PSU_DATA = new PsuIdData("test-user", null, null, null, null);
    private static final ScaApproach TEST_SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final SpiScaApproach TEST_SPI_SCA_APPROACH = SpiScaApproach.EMBEDDED;
    private static final ScaStatus TEST_SCA_STATUS = ScaStatus.RECEIVED;
    private static final SpiScaStatus TEST_SPI_SCA_STATUS = SpiScaStatus.RECEIVED;
    private static final String TEST_PAYMENT_PRODUCT = "sepa- credit-transfers";
    private static final SpiSinglePayment TEST_SPI_SINGLE_PAYMENT = new SpiSinglePayment(TEST_PAYMENT_PRODUCT);
    private static final String TEST_AUTHENTICATION_METHOD_ID = "sms";
    private static final String TEST_AUTHENTICATION_TYPE = "SMS_OTP";
    private static final ErrorType TEST_ERROR_TYPE_400 = PIS_400;
    private static final TransactionStatus TEST_TRANSACTION_STATUS_SUCCESS = TransactionStatus.ACSC;
    private static final TransactionStatus TEST_TRANSACTION_STATUS_MULTILEVEL_SCA = TransactionStatus.PATC;
    private static final SpiTransactionStatus TEST_SPI_TRANSACTION_STATUS_SUCCESS = SpiTransactionStatus.ACSC;
    private static final SpiTransactionStatus TEST_SPI_TRANSACTION_STATUS_MULTILEVEL_SCA = SpiTransactionStatus.PATC;
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = buildTppMessageInformationSet();
    private static final Set<SpiTppMessageInformation> TEST_SPI_TPP_MESSAGES = buildSpiTppMessageInformationSet();
    private static final String TEST_PSU_MESSAGE = "psu message";

    @InjectMocks
    private PisAuthorisationProcessorServiceImpl pisAuthorisationProcessorService;
    @Mock
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private Xs2aAuthorisationService xs2aAuthorisationService;
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
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private PisCommonDecoupledService pisCommonDecoupledService;
    @Mock
    private PaymentAuthorisationSpi paymentAuthorisationSpi;
    @Mock
    private List<PisScaAuthorisationService> services;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private PisExecutePaymentService pisExecutePaymentService;
    @Mock
    private CurrencyConversionInfoSpi currencyConversionInfoSpi;
    @Mock
    private SpiToXs2aCurrencyConversionInfoMapper spiToXs2aCurrencyConversionInfoMapper;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    SpiToXs2aChallengeDataMapper challengeDataMapper;
    @Mock
    SpiToXs2aTppMessageInformationMapper tppMessageInformationMapper;
    @Mock
    SpiToXs2aAuthenticationObjectMapper authenticationObjectMapper;
    @Mock
    SpiToXs2aAuthorizationMapper spiToXs2aAuthorizationMapper;
    @Mock
    SpiToXs2aPisMapper spiToXs2aPisMapper;
    @Mock
    Xs2aToSpiAuthorizationMapper xs2aToSpiAuthorizationMapper;
    @Mock
    SpiToXs2aTransactionMapper spiToXs2aTransactionMapper;

    private PisCommonPaymentResponse commonPaymentResponse;

    @BeforeEach
    void setUp() {
        commonPaymentResponse = new PisCommonPaymentResponse();
    }

    @Test
    void updateAuthorisation_success() {
        // Given
        EmbeddedPisScaAuthorisationService embeddedPisScaAuthorisationService = Mockito.mock(EmbeddedPisScaAuthorisationService.class);
        DecoupledPisScaAuthorisationService decoupledPisScaAuthorisationService = Mockito.mock(DecoupledPisScaAuthorisationService.class);
        services = Arrays.asList(embeddedPisScaAuthorisationService, decoupledPisScaAuthorisationService);

        when(embeddedPisScaAuthorisationService.getScaApproachServiceType()).thenReturn(ScaApproach.EMBEDDED);

        PisAuthorisationProcessorServiceImpl pisAuthorisationProcessorService = new PisAuthorisationProcessorServiceImpl(services,
                                                                                                                         null, null,
                                                                                                                         null, null,
                                                                                                                         null, null, null,
                                                                                                                         null, null,
                                                                                                                         null, null,
                                                                                                                         null, null, null,
                                                                                                                         null, null,
                                                                                                                         null, null,
                                                                                                                         null, null,
                                                                                                                         null, null);

        //When
        pisAuthorisationProcessorService.updateAuthorisation(buildAuthorisationProcessorRequest(), buildAuthorisationProcessorResponse());

        //Then
        verify(embeddedPisScaAuthorisationService).updateAuthorisation(any(), eq(buildAuthorisationProcessorResponse()));
    }

    @Test
    void doScaReceived_authorisation_no_sca_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(new SpiAvailableScaMethodsResponse(false, Collections.emptyList()))
                                                                                                     .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                                                    .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(TEST_SPI_TRANSACTION_STATUS_SUCCESS)).thenReturn(TEST_TRANSACTION_STATUS_SUCCESS);


        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaReceived_authorisation_one_sca_success() {
        // Given
        TEST_SPI_SINGLE_PAYMENT.setPaymentId(TEST_PAYMENT_ID);

        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(buildSpiAuthorizationCodeResult())
                                                                                                          .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList(false))).thenReturn(buildAuthenticationObjectSingleValueList(false));
        when(spiToXs2aPisMapper.mapToPaymentType(SpiPaymentType.SINGLE)).thenReturn(PaymentType.SINGLE);
        when(authenticationObjectMapper.toAuthenticationObject(buildSpiAuthenticationObject(false))).thenReturn(buildAuthenticationObject(false));
        when(challengeDataMapper.toChallengeData(buildSpiChallengeData())).thenReturn(buildChallengeData());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.SCAMETHODSELECTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        assertThat(actual.getChosenScaMethod()).isEqualTo(buildAuthenticationObject());
        assertThat(actual.getChallengeData()).isEqualTo(buildChallengeData());
        verify(xs2aAuthorisationService).saveAuthenticationMethods(eq(TEST_AUTHORISATION_ID), any());
    }

    @Test
    void doScaReceived_authorisation_multiple_sca_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildMultipleScaMethodsResponse())
                                                                                                     .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectList())).thenReturn(buildXs2aAuthenticationObjectList());
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.PSUAUTHENTICATED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        assertThat(actual.getAvailableScaMethods()).isEqualTo(buildXs2aAuthenticationObjectList());
        verify(xs2aAuthorisationService).saveAuthenticationMethods(eq(TEST_AUTHORISATION_ID), any());
    }

    @Test
    void doScaReceived_authorisation_authorise_Psu_with_error_failure() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    void doScaReceived_authorisation_authorise_Psu_incorrect_credentials_failure() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(spiResponse);

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
    void doScaReceived_authorisation_authorise_Psu_exemption_success() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(true, SpiAuthorisationStatus.SUCCESS))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                                                    .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.ACSC)).thenReturn(TransactionStatus.ACSC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaReceived_authorisation_authorise_Psu_sca_exemption_payment_execution_failure() {

        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(true, SpiAuthorisationStatus.SUCCESS))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(spiResponse);
        playCurrencyConversionInfo();
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
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
    void doScaReceived_authorisation_decoupled_chosen_success() {
        // Given
        AuthorisationProcessorRequest request = buildAuthorisationProcessorRequest();
        Authorisation authorisation = request.getAuthorisation();
        authorisation.setChosenScaApproach(ScaApproach.DECOUPLED);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();

        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(buildSpiAuthenticationObjectSingleValueList(true));
        when(paymentAuthorisationSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, TEST_SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList(true))).thenReturn(buildAuthenticationObjectSingleValueList(true));

        // When
        pisAuthorisationProcessorService.doScaReceived(request);

        // Then
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
        verify(xs2aAuthorisationService, times(1)).saveAuthenticationMethods(TEST_AUTHORISATION_ID, buildAuthenticationObjectSingleValueList(true));
        verify(xs2aAuthorisationService, times(1)).updateScaApproach(TEST_AUTHORISATION_ID, ScaApproach.DECOUPLED);
    }

    @Test
    void doScaReceived_authorisation_request_availableScaMethods_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
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
    void doScaReceived_authorisation_request_availableScaMethods_exemption_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                                                    .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.ACSC)).thenReturn(TransactionStatus.ACSC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaReceived_authorisation_request_availableScaMethods_exemption_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
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
    void doScaReceived_authorisation_no_sca_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(new SpiAvailableScaMethodsResponse(false, Collections.emptyList()))
                                                                                                     .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
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
    void doScaReceived_authorisation_one_sca_decoupled_chosen_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.getAvailableScaMethods().get(0).setDecoupled(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList(true))).thenReturn(Collections.singletonList(buildAuthenticationObject(true)));

        // When
        pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
    }

    @Test
    void doScaReceived_authorisation_one_sca_requestAuthorisationCode_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                                          .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList(false))).thenReturn(Collections.singletonList(buildAuthenticationObject(false)));


        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    void doScaReceived_authorisation_request_requestAuthorisationCode_exemption_success() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
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
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                                                    .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList(false))).thenReturn(Collections.singletonList(buildAuthenticationObject(false)));
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.ACSC)).thenReturn(TransactionStatus.ACSC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaReceived_authorisation_request_requestAuthorisationCode_exemption_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        SpiAuthorizationCodeResult result = buildSpiAuthorizationCodeResult();
        result.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(result)
                                                                                                          .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                                    .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList(false))).thenReturn(Collections.singletonList(buildAuthenticationObject(false)));

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    void doScaReceived_identification_success() {
        // Given
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(buildIdentificationAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.PSUIDENTIFIED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test
    void doScaReceived_identification_no_psu_failure() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildIdentificationAuthorisationProcessorRequest();
        ((PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest()).setPsuData(null);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaReceived(authorisationProcessorRequest);

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
    }

    @Test
    void doScaPsuIdentified_authorisation_no_sca_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(new SpiAvailableScaMethodsResponse(false, Collections.emptyList()))
                                                                                                     .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                                                    .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.ACSC)).thenReturn(TransactionStatus.ACSC);
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaPsuIdentified_authorisation_one_sca_success() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();

        TEST_SPI_SINGLE_PAYMENT.setPaymentId(TEST_PAYMENT_ID);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));

        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(buildSpiAuthorizationCodeResult())
                                                                                                          .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList(false))).thenReturn(Collections.singletonList(buildAuthenticationObject(false)));
        when(spiToXs2aPisMapper.mapToPaymentType(SpiPaymentType.SINGLE)).thenReturn(PaymentType.SINGLE);
        when(authenticationObjectMapper.toAuthenticationObject(buildSpiAuthenticationObject(false))).thenReturn(buildAuthenticationObject(false));
        when(challengeDataMapper.toChallengeData(buildSpiChallengeData())).thenReturn(buildChallengeData());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.SCAMETHODSELECTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        assertThat(actual.getChosenScaMethod()).isEqualTo(buildAuthenticationObject());
        assertThat(actual.getChallengeData()).isEqualTo(buildChallengeData());
        verify(xs2aAuthorisationService).saveAuthenticationMethods(eq(TEST_AUTHORISATION_ID), any());
    }

    @Test
    void doScaPsuIdentified_authorisation_multiple_sca_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildMultipleScaMethodsResponse())
                                                                                                     .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectList())).thenReturn(buildXs2aAuthenticationObjectList());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.PSUAUTHENTICATED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        assertThat(actual.getAvailableScaMethods()).isEqualTo(buildXs2aAuthenticationObjectList());
        verify(xs2aAuthorisationService).saveAuthenticationMethods(eq(TEST_AUTHORISATION_ID), any());
    }

    @Test
    void doScaPsuIdentified_authorisation_authorise_Psu_with_error_failure() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    void doScaPsuIdentified_authorisation_authorise_Psu_incorrect_credentials_failure() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.FAILURE))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(spiResponse);

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
    void doScaPsuIdentified_authorisation_authorise_Psu_exemption_success() {
        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(true, SpiAuthorisationStatus.SUCCESS))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(spiResponse);
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                                                    .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.ACSC)).thenReturn(TransactionStatus.ACSC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaPsuIdentified_authorisation_authorise_Psu_sca_exemption_payment_execution_failure() {

        // Given
        SpiResponse<SpiPsuAuthorisationResponse> spiResponse = SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                   .payload(new SpiPsuAuthorisationResponse(true, SpiAuthorisationStatus.SUCCESS))
                                                                   .build();
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(spiResponse);

        playCurrencyConversionInfo();

        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
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
    void doScaPsuIdentified_authorisation_decoupled_chosen_success() {
        // Given
        AuthorisationProcessorRequest request = buildAuthorisationProcessorRequest();
        Authorisation authorisation = request.getAuthorisation();
        authorisation.setChosenScaApproach(ScaApproach.DECOUPLED);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        SpiAvailableScaMethodsResponse spiAvailableScaMethodsResponse = new SpiAvailableScaMethodsResponse(buildSpiAuthenticationObjectSingleValueList(true));
        when(paymentAuthorisationSpi.requestAvailableScaMethods(SPI_CONTEXT_DATA, TEST_SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                            .payload(spiAvailableScaMethodsResponse)
                            .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList(true))).thenReturn(buildAuthenticationObjectSingleValueList(true));

        // When
        pisAuthorisationProcessorService.doScaPsuIdentified(request);

        // Then
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
        verify(xs2aAuthorisationService, times(1)).saveAuthenticationMethods(TEST_AUTHORISATION_ID, buildAuthenticationObjectSingleValueList(true));
        verify(xs2aAuthorisationService, times(1)).updateScaApproach(TEST_AUTHORISATION_ID, ScaApproach.DECOUPLED);
    }

    @Test
    void doScaPsuIdentified_authorisation_request_availableScaMethods_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();

        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
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
    void doScaPsuIdentified_authorisation_request_availableScaMethods_exemption_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                                                    .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.ACSC)).thenReturn(TransactionStatus.ACSC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaPsuIdentified_authorisation_request_availableScaMethods_exemption_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());

        playCurrencyConversionInfo();

        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
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
    void doScaPsuIdentified_authorisation_no_sca_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(new SpiAvailableScaMethodsResponse(false, Collections.emptyList()))
                                                                                                     .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
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
    void doScaPsuIdentified_authorisation_one_sca_decoupled_chosen_success() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();

        SpiAvailableScaMethodsResponse response = buildSingleScaMethodsResponse();
        response.getAvailableScaMethods().get(0).setDecoupled(true);
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(response)
                                                                                                     .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList(true))).thenReturn(buildAuthenticationObjectSingleValueList(true));

        // When
        pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
    }

    @Test
    void doScaPsuIdentified_authorisation_one_sca_requestAuthorisationCode_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                                          .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList())).thenReturn(buildAuthenticationObjectSingleValueList());


        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    void doScaPsuIdentified_authorisation_request_requestAuthorisationCode_exemption_success() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
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
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                                                    .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList())).thenReturn(buildAuthenticationObjectSingleValueList());
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.ACSC)).thenReturn(TransactionStatus.ACSC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaPsuIdentified_authorisation_request_requestAuthorisationCode_exemption_payment_execution_failure() {
        // Given
        when(paymentAuthorisationSpi.authorisePsu(any(), any(), any(), any(), any(), any())).thenReturn(SpiResponse.<SpiPsuAuthorisationResponse>builder()
                                                                                                            .payload(new SpiPsuAuthorisationResponse(false, SpiAuthorisationStatus.SUCCESS))
                                                                                                            .build());
        playCurrencyConversionInfo();
        when(paymentAuthorisationSpi.requestAvailableScaMethods(any(), any(), any())).thenReturn(SpiResponse.<SpiAvailableScaMethodsResponse>builder()
                                                                                                     .payload(buildSingleScaMethodsResponse())
                                                                                                     .build());
        SpiAuthorizationCodeResult result = buildSpiAuthorizationCodeResult();
        result.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(result)
                                                                                                          .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), any(), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                    .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                                                    .build());
        when(spiErrorMapper.mapToErrorHolder(any(), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());
        when(authenticationObjectMapper.toAuthenticationObjectList(buildSpiAuthenticationObjectSingleValueList())).thenReturn(buildAuthenticationObjectSingleValueList());


        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    void doScaPsuIdentified_identification_success() {
        // Given
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(buildIdentificationAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.PSUIDENTIFIED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test
    void doScaPsuIdentified_identification_no_psu_failure() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildIdentificationAuthorisationProcessorRequest();
        ((PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest()).setPsuData(null);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuIdentified(authorisationProcessorRequest);

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
    }

    @Test
    void doScaPsuAuthenticated_embedded_success() {
        // Given
        AuthorisationProcessorRequest processorRequest = buildAuthorisationProcessorRequest();
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aAuthorisationService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), any(), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                          .payload(buildSpiAuthorizationCodeResult())
                                                                                                          .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentResponse));
        SpiSinglePayment spiPayment = new SpiSinglePayment("sepa-credit-transfers");
        spiPayment.setPaymentId(TEST_PAYMENT_ID);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse)).thenReturn(spiPayment);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(processorRequest);

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.SCAMETHODSELECTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test
    void doScaPsuAuthenticated_decoupled_success() {
        // Given
        when(xs2aAuthorisationService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(true);

        // When
        pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        verify(xs2aAuthorisationService).updateScaApproach(TEST_AUTHORISATION_ID, ScaApproach.DECOUPLED);
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(any(), any(), any());
    }

    @Test
    void doScaPsuAuthenticated_embedded_spi_hasError_failure() {
        // Given
        when(xs2aAuthorisationService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                  .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                  .build();

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(TEST_PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);

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
    void doScaPsuAuthenticated_embedded_empty_result_failure() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(xs2aAuthorisationService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        playCurrencyConversionInfo();

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(TEST_SPI_SINGLE_PAYMENT);
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
    void doScaPsuAuthenticated_embedded_sca_exemption_success() {
        // Given
        when(xs2aAuthorisationService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = buildSpiAuthorizationCodeResult();
        spiAuthorizationCodeResult.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                                                .payload(spiAuthorizationCodeResult)
                                                                                                                                .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                                          .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                                                                          .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.ACSC)).thenReturn(TransactionStatus.ACSC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaPsuAuthenticated_embedded_sca_exemption_payment_execution_failure() {
        // Given
        when(xs2aAuthorisationService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        playCurrencyConversionInfo();

        SpiAuthorizationCodeResult spiAuthorizationCodeResult = buildSpiAuthorizationCodeResult();
        spiAuthorizationCodeResult.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                                                .payload(spiAuthorizationCodeResult)
                                                                                                                                .build());
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                   .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                   .build();
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400).build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    void doScaPsuAuthenticated_embedded_sca_exemption_multilevel_status_success() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(xs2aAuthorisationService.isAuthenticationMethodDecoupled(eq(TEST_AUTHORISATION_ID), any())).thenReturn(false);
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = buildSpiAuthorizationCodeResult();
        spiAuthorizationCodeResult.setScaExempted(true);
        when(paymentAuthorisationSpi.requestAuthorisationCode(any(), any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiAuthorizationCodeResult>builder()
                                                                                                                                .payload(spiAuthorizationCodeResult)
                                                                                                                                .build());
        when(pisExecutePaymentService.executePaymentWithoutSca(any(), eq(TEST_SPI_SINGLE_PAYMENT), any())).thenReturn(SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                                                                          .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_MULTILEVEL_SCA))
                                                                                                                          .build());
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.PATC)).thenReturn(TransactionStatus.PATC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaPsuAuthenticated(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_MULTILEVEL_SCA);
        verify(xs2aPisCommonPaymentService).updateMultilevelSca(TEST_PAYMENT_ID, true);
    }

    @Test
    void doScaMethodSelected_success() {
        // Given
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                   .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_SUCCESS))
                                                                   .build();
        when(pisExecutePaymentService.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(any(), any(), any(), any())).thenReturn(spiResponse);
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentResponse));
        SpiSinglePayment spiPayment = new SpiSinglePayment("sepa-credit-transfers");
        spiPayment.setPaymentId(TEST_PAYMENT_ID);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse)).thenReturn(spiPayment);
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.ACSC)).thenReturn(TransactionStatus.ACSC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaMethodSelected(buildAuthorisationProcessorRequest());

        //Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(updatePaymentAfterSpiService).updatePaymentStatus(TEST_PAYMENT_ID, TEST_TRANSACTION_STATUS_SUCCESS);
    }

    @Test
    void doScaMethodSelected_multilevel_sca_success() {
        // Given
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                   .payload(buildSpiPaymentExecutionResponse(TEST_SPI_TRANSACTION_STATUS_MULTILEVEL_SCA))
                                                                   .build();
        when(pisExecutePaymentService.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(any(), any(), any(), any())).thenReturn(spiResponse);

        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentResponse));
        SpiSinglePayment spiPayment = new SpiSinglePayment("sepa-credit-transfers");
        spiPayment.setPaymentId(TEST_PAYMENT_ID);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse)).thenReturn(spiPayment);
        when(spiToXs2aTransactionMapper.mapToTransactionStatus(SpiTransactionStatus.PATC)).thenReturn(TransactionStatus.PATC);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaMethodSelected(buildAuthorisationProcessorRequest());

        //Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(xs2aPisCommonPaymentService).updateMultilevelSca(TEST_PAYMENT_ID, true);
    }

    @Test
    void doScaMethodSelected_verifySca_fail_failure() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(TEST_PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                   .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                   .build();
        when(pisExecutePaymentService.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(any(), any(), any(), any())).thenReturn(spiResponse);
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
    void doScaMethodSelected_verifySca_fail_attemptFailure() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(TEST_PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                   .payload(new SpiPaymentExecutionResponse(SpiAuthorisationStatus.ATTEMPT_FAILURE))
                                                                   .error(new SpiTppMessage(SpiMessageErrorCode.INTERNAL_SERVER_ERROR, "Internal server error"))
                                                                   .build();
        when(pisExecutePaymentService.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(ErrorHolder.builder(TEST_ERROR_TYPE_400)
                                                                                     .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                                                                     .build());

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaMethodSelected(buildAuthorisationProcessorRequest());

        //Then
        assertNotNull(actual);
        assertNotNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.RECEIVED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
        verify(spiErrorMapper).mapToErrorHolder(any(), any());
    }

    @Test
    void doScaExempted_success() {
        // Given
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaExempted(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.EXEMPTED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test
    void doScaStarted_success() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(TEST_PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = SpiResponse.<SpiStartAuthorisationResponse>builder()
                                                                     .payload(new SpiStartAuthorisationResponse(TEST_SPI_SCA_APPROACH, TEST_SPI_SCA_STATUS, TEST_PSU_MESSAGE, TEST_SPI_TPP_MESSAGES))
                                                                     .build();
        when(paymentAuthorisationSpi.startAuthorisation(SPI_CONTEXT_DATA, TEST_SPI_SCA_APPROACH, TEST_SPI_SCA_STATUS, TEST_AUTHORISATION_ID, TEST_SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(xs2aToSpiAuthorizationMapper.mapToSpiScaStatus(TEST_SCA_STATUS)).thenReturn(TEST_SPI_SCA_STATUS);
        when(xs2aToSpiAuthorizationMapper.mapToSpiScaApproach(TEST_SCA_APPROACH)).thenReturn(TEST_SPI_SCA_APPROACH);
        when(spiToXs2aAuthorizationMapper.mapToScaStatus(TEST_SPI_SCA_STATUS)).thenReturn(TEST_SCA_STATUS);
        when(spiToXs2aAuthorizationMapper.mapToScaApproach(TEST_SPI_SCA_APPROACH)).thenReturn(TEST_SCA_APPROACH);
        when(tppMessageInformationMapper.toTppMessageInformationSet(TEST_SPI_TPP_MESSAGES)).thenReturn(TEST_TPP_MESSAGES);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);
        CreatePaymentAuthorisationProcessorResponse expected = buildCreatePaymentAuthorisationProcessorResponse();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doScaStarted_spiError() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(TEST_PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = SpiResponse.<SpiStartAuthorisationResponse>builder()
                                                                     .error(new SpiTppMessage(SpiMessageErrorCode.FORMAT_ERROR_ABSENT_HEADER))
                                                                     .build();
        when(paymentAuthorisationSpi.startAuthorisation(SPI_CONTEXT_DATA, TEST_SPI_SCA_APPROACH, TEST_SPI_SCA_STATUS, TEST_AUTHORISATION_ID, TEST_SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);
        ErrorHolder errorHolder = ErrorHolder.builder(TEST_ERROR_TYPE_400).build();
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(errorHolder);
        when(xs2aToSpiAuthorizationMapper.mapToSpiScaStatus(TEST_SCA_STATUS)).thenReturn(TEST_SPI_SCA_STATUS);
        when(xs2aToSpiAuthorizationMapper.mapToSpiScaApproach(TEST_SCA_APPROACH)).thenReturn(TEST_SPI_SCA_APPROACH);

        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);
        CreatePaymentAuthorisationProcessorResponse expected = buildCreatePaymentAuthorisationProcessorResponseWithError(errorHolder);

        // Then
        assertThat(actual.hasError()).isTrue();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doScaFinalised_success() {
        // Given
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(any(), any(), any(), any()))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());
        // When
        AuthorisationProcessorResponse actual = pisAuthorisationProcessorService.doScaFinalised(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(actual.getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test
    void doScaFailed_success() {
        AuthorisationProcessorRequest authorisationProcessorRequest = buildEmptyAuthorisationProcessorRequest();

        assertThrows(UnsupportedOperationException.class,
                     () -> pisAuthorisationProcessorService.doScaFailed(authorisationProcessorRequest));
    }

    private AuthorisationProcessorRequest buildEmptyAuthorisationProcessorRequest() {
        return new PisAuthorisationProcessorRequest(null,
                                                    null,
                                                    null,
                                                    null);
    }

    private AuthorisationProcessorRequest buildAuthorisationProcessorRequest() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setPaymentId(TEST_PAYMENT_ID);
        request.setAuthorisationId(TEST_AUTHORISATION_ID);
        request.setPsuData(TEST_PSU_DATA);
        request.setPaymentService(PaymentType.SINGLE);
        request.setPaymentProduct(TEST_PAYMENT_PRODUCT);
        Authorisation authorisation = new Authorisation();
        return new PisAuthorisationProcessorRequest(TEST_SCA_APPROACH,
                                                    TEST_SCA_STATUS,
                                                    request,
                                                    authorisation);
    }

    private AuthorisationProcessorRequest buildIdentificationAuthorisationProcessorRequest() {
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        ((PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest()).setUpdatePsuIdentification(true);
        return authorisationProcessorRequest;
    }

    private SpiAuthorizationCodeResult buildSpiAuthorizationCodeResult() {
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        SpiAuthenticationObject method = new SpiAuthenticationObject();
        method.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        method.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        spiAuthorizationCodeResult.setSelectedScaMethod(method);
        spiAuthorizationCodeResult.setChallengeData(buildSpiChallengeData());
        return spiAuthorizationCodeResult;
    }

    private ChallengeData buildChallengeData() {
        return new ChallengeData(null, Collections.singletonList("some data"), "some link", 100, null, "info");
    }

    private SpiChallengeData buildSpiChallengeData() {
        return new SpiChallengeData(null, Collections.singletonList("some data"), "some link", 100, null, "info");
    }

    private SpiAuthorizationCodeResult buildEmptySpiAuthorizationCodeResult() {
        return new SpiAuthorizationCodeResult();
    }

    private SpiPaymentExecutionResponse buildSpiPaymentExecutionResponse(SpiTransactionStatus status) {
        return new SpiPaymentExecutionResponse(status);
    }

    private AuthorisationProcessorResponse buildAuthorisationProcessorResponse() {
        return new AuthorisationProcessorResponse();
    }

    private SpiAvailableScaMethodsResponse buildMultipleScaMethodsResponse() {
        return new SpiAvailableScaMethodsResponse(false, buildSpiAuthenticationObjectList());
    }

    private List<AuthenticationObject> buildXs2aAuthenticationObjectList() {
        List<AuthenticationObject> authenticationObjects = new ArrayList<>();
        AuthenticationObject sms = new AuthenticationObject();
        sms.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        sms.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        authenticationObjects.add(sms);
        AuthenticationObject push = new AuthenticationObject();
        push.setAuthenticationType("PUSH_OTP");
        push.setAuthenticationMethodId("push");
        push.setDecoupled(true);
        authenticationObjects.add(push);
        return authenticationObjects;
    }

    private List<SpiAuthenticationObject> buildSpiAuthenticationObjectList() {
        List<SpiAuthenticationObject> spiAuthenticationObjects = new ArrayList<>();
        SpiAuthenticationObject sms = new SpiAuthenticationObject();
        sms.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        sms.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
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

    private AuthenticationObject buildAuthenticationObject() {
        AuthenticationObject sms = new AuthenticationObject();
        sms.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        sms.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        return sms;
    }

    private List<SpiAuthenticationObject> buildSpiAuthenticationObjectSingleValueList() {
        SpiAuthenticationObject sms = new SpiAuthenticationObject();
        sms.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        sms.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        return Collections.singletonList(sms);
    }

    private List<AuthenticationObject> buildAuthenticationObjectSingleValueList() {
        AuthenticationObject sms = new AuthenticationObject();
        sms.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        sms.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        return Collections.singletonList(sms);
    }

    private AuthenticationObject buildAuthenticationObject(boolean isDecoupled) {
        AuthenticationObject sms = new AuthenticationObject();
        sms.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        sms.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        sms.setDecoupled(isDecoupled);
        return sms;
    }

    private SpiAuthenticationObject buildSpiAuthenticationObject(boolean isDecoupled) {
        SpiAuthenticationObject sms = new SpiAuthenticationObject();
        sms.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        sms.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        sms.setDecoupled(isDecoupled);
        return sms;
    }

    private List<SpiAuthenticationObject> buildSpiAuthenticationObjectSingleValueList(boolean isDecoupled) {
        SpiAuthenticationObject sms = new SpiAuthenticationObject();
        sms.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        sms.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        sms.setDecoupled(isDecoupled);
        return Collections.singletonList(sms);
    }

    private List<AuthenticationObject> buildAuthenticationObjectSingleValueList(boolean isDecoupled) {
        AuthenticationObject sms = new AuthenticationObject();
        sms.setAuthenticationType(TEST_AUTHENTICATION_TYPE);
        sms.setAuthenticationMethodId(TEST_AUTHENTICATION_METHOD_ID);
        sms.setDecoupled(isDecoupled);
        return Collections.singletonList(sms);
    }

    private void playCurrencyConversionInfo() {
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(TEST_PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(SPI_CONTEXT_DATA, TEST_SPI_SINGLE_PAYMENT, TEST_AUTHORISATION_ID, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder().payload(null).build());
    }

    private static Set<TppMessageInformation> buildTppMessageInformationSet() {
        return Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
    }

    private static Set<SpiTppMessageInformation> buildSpiTppMessageInformationSet() {
        return Collections.singleton(SpiTppMessageInformation.of(SpiMessageErrorCode.FORMAT_ERROR));
    }

    private CreatePaymentAuthorisationProcessorResponse buildCreatePaymentAuthorisationProcessorResponse() {
        return new CreatePaymentAuthorisationProcessorResponse(TEST_SCA_STATUS, TEST_SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, TEST_PAYMENT_ID, TEST_PSU_DATA);
    }

    private CreatePaymentAuthorisationProcessorResponse buildCreatePaymentAuthorisationProcessorResponseWithError(ErrorHolder errorHolder) {
        return new CreatePaymentAuthorisationProcessorResponse(errorHolder, TEST_SCA_APPROACH, TEST_PAYMENT_ID, TEST_PSU_DATA);
    }
}
