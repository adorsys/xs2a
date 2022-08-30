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
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.payment.support.SpiPaymentFactoryImpl;
import de.adorsys.psd2.xs2a.service.payment.support.TestSpiDataProvider;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.spi.SpiToXs2aPaymentMapperSupport;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadPeriodicPaymentServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final List<PisPayment> PIS_PAYMENTS = Collections.singletonList(new PisPayment());
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.defaultSpiContextData();
    private static final SpiPeriodicPayment SPI_PERIODIC_PAYMENT = new SpiPeriodicPayment(PRODUCT);
    private static final PeriodicPayment PERIODIC_PAYMENT = buildPeriodicPayment();
    private static final String SOME_ENCRYPTED_PAYMENT_ID = "Encrypted Payment Id";
    private static final String ACCEPT_MEDIA_TYPE = ContentType.JSON.getType();
    private static final byte[] PAYMENT_BODY = "some payment body".getBytes();

    @InjectMocks
    private ReadPeriodicPaymentService readPeriodicPaymentService;

    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;
    @Mock
    private PeriodicPaymentSpi periodicPaymentSpi;
    @Mock
    private SpiToXs2aPaymentMapperSupport spiToXs2aPaymentMapperSupport;
    @Mock
    private SpiPaymentFactoryImpl spiPaymentFactory;
    @Mock
    private SpiErrorMapper spiErrorMapper;
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
        doReturn(Optional.of(SPI_PERIODIC_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(periodicPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ContentType.JSON.getType(), SPI_PERIODIC_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPeriodicPayment>builder()
                            .payload(SPI_PERIODIC_PAYMENT)
                            .build());
        when(spiToXs2aPaymentMapperSupport.mapToPeriodicPayment(SPI_PERIODIC_PAYMENT))
            .thenReturn(PERIODIC_PAYMENT);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(SOME_ENCRYPTED_PAYMENT_ID, TransactionStatus.RCVD)).thenReturn(true);

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readPeriodicPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(PERIODIC_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    void getPayment_updatePaymentStatusAfterSpiService_updatePaymentStatus_failed() {
        // Given
        doReturn(Optional.of(SPI_PERIODIC_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(periodicPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ContentType.JSON.getType(), SPI_PERIODIC_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPeriodicPayment>builder()
                            .payload(SPI_PERIODIC_PAYMENT)
                            .build());
        when(spiToXs2aPaymentMapperSupport.mapToPeriodicPayment(SPI_PERIODIC_PAYMENT))
            .thenReturn(PERIODIC_PAYMENT);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readPeriodicPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(PERIODIC_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    void getPayment_spiPaymentFactory_createSpiPeriodicPayment_failed() {
        // Given
        doReturn(Optional.of(SPI_PERIODIC_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);

        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                        .build();

        when(spiPaymentFactory.getSpiPayment(pisCommonPaymentResponse))
            .thenReturn(Optional.empty());

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readPeriodicPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

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
        PaymentInformationResponse<CommonPayment> actualResponse = readPeriodicPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        verify(spiPaymentFactory, never()).getSpiPayment(any());

        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    void getPayment_periodicPaymentSpi_getPaymentById_failed() {
        // Given
        doReturn(Optional.of(SPI_PERIODIC_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(periodicPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ContentType.JSON.getType(), SPI_PERIODIC_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPeriodicPayment>builder()
                            .payload(SPI_PERIODIC_PAYMENT)
                            .build());
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        SpiResponse<SpiPeriodicPayment> spiResponseError = SpiResponse.<SpiPeriodicPayment>builder()
                                                               .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                                                               .build();

        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                        .build();

        when(periodicPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ContentType.JSON.getType(), SPI_PERIODIC_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(spiResponseError);
        when(spiErrorMapper.mapToErrorHolder(spiResponseError, ServiceType.PIS))
            .thenReturn(expectedError);

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readPeriodicPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static PeriodicPayment buildPeriodicPayment() {
        PeriodicPayment payment = new PeriodicPayment();
        payment.setPaymentId(PAYMENT_ID);
        payment.setStartDate(LocalDate.now());
        payment.setEndDate(LocalDate.now().plusMonths(4));
        payment.setTransactionStatus(TransactionStatus.RCVD);
        return payment;
    }
}
