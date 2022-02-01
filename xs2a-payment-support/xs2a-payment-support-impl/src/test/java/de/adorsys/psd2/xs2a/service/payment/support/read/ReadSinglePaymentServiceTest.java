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

package de.adorsys.psd2.xs2a.service.payment.support.read;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ContentType;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.payment.support.SpiPaymentFactoryImpl;
import de.adorsys.psd2.xs2a.service.payment.support.TestSpiDataProvider;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.spi.SpiToXs2aPaymentMapperSupport;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadSinglePaymentServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String END_TO_END_IDENTIFICATION = "PAYMENT_ID";
    private static final String CREDITOR_AGENT = "AAAADEBBXXX";
    private static final String CREDITOR_NAME = "WBG";
    private static final String REMITTANCE_INFORMATION_UNSTRUCTURED = "Ref Number Merchant";
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.now();
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.now();
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final List<PisPayment> PIS_PAYMENTS = Collections.singletonList(new PisPayment());
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.defaultSpiContextData();
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private static final SinglePayment SINGLE_PAYMENT = buildSinglePayment();
    private static final String SOME_ENCRYPTED_PAYMENT_ID = "Encrypted Payment Id";
    private static final String ACCEPT_MEDIA_TYPE = ContentType.JSON.getType();
    private static final byte[] PAYMENT_BODY = "some payment body".getBytes();

    @InjectMocks
    private ReadSinglePaymentService readSinglePaymentService;

    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;
    @Mock
    private SinglePaymentSpi singlePaymentSpi;
    @Mock
    private SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupport;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiPaymentFactoryImpl spiPaymentFactory;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private PisCommonPaymentResponse pisCommonPaymentResponse;

    @BeforeEach
    void init() {
        pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setPayments(PIS_PAYMENTS);
        pisCommonPaymentResponse.setPaymentProduct(PRODUCT);
        pisCommonPaymentResponse.setPaymentData(PAYMENT_BODY);
    }

    @Test
    void getPayment_success() {
        // Given
        doReturn(Optional.of(SPI_SINGLE_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(singlePaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiSinglePayment>builder()
                            .payload(SPI_SINGLE_PAYMENT)
                            .build());
        when(spiToXs2aPaymentMapperSupport.mapToSinglePayment(SPI_SINGLE_PAYMENT)).thenReturn(SINGLE_PAYMENT);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(SOME_ENCRYPTED_PAYMENT_ID, TransactionStatus.RCVD)).thenReturn(true);

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readSinglePaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(SINGLE_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    void getPayment_updatePaymentStatusAfterSpiService_updatePaymentStatus_failed() {
        // Given
        doReturn(Optional.of(SPI_SINGLE_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(singlePaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiSinglePayment>builder()
                            .payload(SPI_SINGLE_PAYMENT)
                            .build());
        when(spiToXs2aPaymentMapperSupport.mapToSinglePayment(SPI_SINGLE_PAYMENT)).thenReturn(SINGLE_PAYMENT);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readSinglePaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(SINGLE_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    void getPayment_singlePaymentSpi_getPaymentById_failed() {
        // Given
        doReturn(Optional.of(SPI_SINGLE_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(singlePaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiSinglePayment>builder()
                            .payload(SPI_SINGLE_PAYMENT)
                            .build());
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        SpiResponse<SpiSinglePayment> spiResponseError = SpiResponse.<SpiSinglePayment>builder()
                                                             .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                                                             .build();

        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_400)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                        .build();

        when(singlePaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ContentType.JSON.getType(), SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider)).thenReturn(spiResponseError);
        when(spiErrorMapper.mapToErrorHolder(spiResponseError, ServiceType.PIS)).thenReturn(expectedError);

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readSinglePaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    void getPayment_emptyPaymentData() {
        // Given
        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_400)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_PAYMENT_NOT_FOUND))
                                        .build();
        pisCommonPaymentResponse.setPaymentData(null);

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readSinglePaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        verify(spiPaymentFactory, never()).getSpiPayment(any());

        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    void getPayment_spiPaymentFactory_createSpiSinglePayment_failed() {
        // Given
        doReturn(Optional.of(SPI_SINGLE_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);

        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                        .build();

        when(spiPaymentFactory.getSpiPayment(pisCommonPaymentResponse)).thenReturn(Optional.empty());

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readSinglePaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static SinglePayment buildSinglePayment() {
        SinglePayment singlePayment = new SinglePayment();
        singlePayment.setPaymentId(PAYMENT_ID);
        singlePayment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        singlePayment.setCreditorAgent(CREDITOR_AGENT);
        singlePayment.setCreditorName(CREDITOR_NAME);
        singlePayment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTURED);
        singlePayment.setTransactionStatus(TRANSACTION_STATUS);
        singlePayment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        singlePayment.setRequestedExecutionTime(REQUESTED_EXECUTION_TIME);
        return singlePayment;
    }
}
