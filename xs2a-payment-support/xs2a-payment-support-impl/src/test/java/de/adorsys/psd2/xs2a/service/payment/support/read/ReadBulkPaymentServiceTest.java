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

package de.adorsys.psd2.xs2a.service.payment.support.read;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ContentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.payment.support.SpiPaymentFactoryImpl;
import de.adorsys.psd2.xs2a.service.payment.support.TestSpiDataProvider;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.spi.SpiToXs2aPaymentMapperSupport;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiMessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiTppMessage;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ReadBulkPaymentServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final List<PisPayment> PIS_PAYMENTS = Collections.singletonList(new PisPayment());
    private static final BulkPayment BULK_PAYMENT = new BulkPayment();
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.defaultSpiContextData();
    private static final SpiBulkPayment SPI_BULK_PAYMENT = new SpiBulkPayment();
    private static final SpiResponse<SpiBulkPayment> BULK_PAYMENT_SPI_RESPONSE = buildSpiResponse();
    private static final String SOME_ENCRYPTED_PAYMENT_ID = "Encrypted Payment Id";
    private static final String ACCEPT_MEDIA_TYPE = ContentType.JSON.getType();
    private static final byte[] PAYMENT_BODY = "some payment body".getBytes();

    @InjectMocks
    private ReadBulkPaymentService readBulkPaymentService;

    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aUpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;
    @Mock
    private BulkPaymentSpi bulkPaymentSpi;
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
        doReturn(Optional.of(SPI_BULK_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(bulkPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_BULK_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(BULK_PAYMENT_SPI_RESPONSE);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        when(spiToXs2aPaymentMapperSupport.mapToBulkPayment(SPI_BULK_PAYMENT))
            .thenReturn(BULK_PAYMENT);

        //When
        PaymentInformationResponse<CommonPayment> actualResponse = readBulkPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(BULK_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    void getPayment_updatePaymentStatusAfterSpiService_updatePaymentStatus_failed() {
        // Given
        doReturn(Optional.of(SPI_BULK_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(bulkPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_BULK_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(BULK_PAYMENT_SPI_RESPONSE);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        when(spiToXs2aPaymentMapperSupport.mapToBulkPayment(SPI_BULK_PAYMENT))
            .thenReturn(BULK_PAYMENT);

        //When
        PaymentInformationResponse<CommonPayment> actualResponse = readBulkPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(BULK_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    void getPayment_bulkPaymentSpi_getPaymentById_failed() {
        // Given
        doReturn(Optional.of(SPI_BULK_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(bulkPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_BULK_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(BULK_PAYMENT_SPI_RESPONSE);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        SpiResponse<SpiBulkPayment> spiResponseError = SpiResponse.<SpiBulkPayment>builder()
                                                           .error(new SpiTppMessage(SpiMessageErrorCode.FORMAT_ERROR))
                                                           .build();
        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                        .build();

        when(bulkPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_BULK_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(spiResponseError);
        when(spiErrorMapper.mapToErrorHolder(spiResponseError, ServiceType.PIS))
            .thenReturn(expectedError);

        //When
        PaymentInformationResponse<CommonPayment> actualResponse = readBulkPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
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
        PaymentInformationResponse<CommonPayment> actualResponse = readBulkPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        verify(spiPaymentFactory, never()).getSpiPayment(pisCommonPaymentResponse);

        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    void getPayment_spiPaymentFactory_createSpiBulkPayment_failed() {
        // Given
        doReturn(Optional.of(SPI_BULK_PAYMENT)).when(spiPaymentFactory).getSpiPayment(pisCommonPaymentResponse);

        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                        .build();

        when(spiPaymentFactory.getSpiPayment(pisCommonPaymentResponse)).thenReturn(Optional.empty());

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readBulkPaymentService.getPayment(pisCommonPaymentResponse, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static SpiResponse<SpiBulkPayment> buildSpiResponse() {
        return SpiResponse.<SpiBulkPayment>builder()
                   .payload(SPI_BULK_PAYMENT)
                   .build();
    }
}
