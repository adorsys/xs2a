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

package de.adorsys.psd2.xs2a.service.payment.support.status;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.ContentType;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.mapper.MediaTypeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.payment.support.SpiPaymentFactoryImpl;
import de.adorsys.psd2.xs2a.service.payment.support.TestSpiDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ReadSinglePaymentStatusServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.defaultSpiContextData();
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private static final String JSON_MEDIA_TYPE = ContentType.JSON.getType();
    private static final String PSU_MESSAGE = "PSU message";
    private static final SpiGetPaymentStatusResponse TRANSACTION_STATUS = new SpiGetPaymentStatusResponse(TransactionStatus.ACSP, null, JSON_MEDIA_TYPE, null, PSU_MESSAGE, null, null);
    private static final SpiResponse<SpiGetPaymentStatusResponse> TRANSACTION_RESPONSE = buildSpiResponseTransactionStatus();
    private static final SpiResponse<SpiGetPaymentStatusResponse> TRANSACTION_RESPONSE_FAILURE = buildFailSpiResponseTransactionStatus();
    private static final ReadPaymentStatusResponse READ_PAYMENT_STATUS_RESPONSE = new ReadPaymentStatusResponse(TRANSACTION_RESPONSE.getPayload().getTransactionStatus(), TRANSACTION_RESPONSE.getPayload().getFundsAvailable(), MediaType.APPLICATION_JSON, null, PSU_MESSAGE, null, null);
    private static final String SOME_ENCRYPTED_PAYMENT_ID = "Encrypted Payment Id";
    private static final byte[] PAYMENT_BODY = "some payment body".getBytes();

    @InjectMocks
    private ReadSinglePaymentStatusService readSinglePaymentStatusService;

    @Mock
    private SpiPaymentFactoryImpl spiPaymentFactory;
    @Mock
    private SinglePaymentSpi singlePaymentSpi;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private MediaTypeMapper mediaTypeMapper;
    @Mock
    private SpiToXs2aLinksMapper spiToXs2aLinksMapper;

    private PisCommonPaymentResponse commonPaymentData;

    @BeforeEach
    void init() {
        commonPaymentData = getCommonPaymentData();
    }

    @Test
    void readPaymentStatus_success() {
        // Given
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        doReturn(Optional.of(SPI_SINGLE_PAYMENT)).when(spiPaymentFactory).getSpiPayment(commonPaymentData);
        when(singlePaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, JSON_MEDIA_TYPE, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(TRANSACTION_RESPONSE);
        when(mediaTypeMapper.mapToMediaType(JSON_MEDIA_TYPE))
            .thenReturn(MediaType.APPLICATION_JSON);

        // When
        ReadPaymentStatusResponse actualResponse = readSinglePaymentStatusService.readPaymentStatus(commonPaymentData, SPI_CONTEXT_DATA, SOME_ENCRYPTED_PAYMENT_ID, JSON_MEDIA_TYPE);

        // Then
        assertThat(actualResponse).isEqualTo(READ_PAYMENT_STATUS_RESPONSE);
    }

    @Test
    void readPaymentStatus_emptyPaymentData() {
        // Given
        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_400)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_PAYMENT_NOT_FOUND))
                                        .build();
        commonPaymentData.setPaymentData(null);

        // When
        ReadPaymentStatusResponse actualResponse = readSinglePaymentStatusService.readPaymentStatus(commonPaymentData, SPI_CONTEXT_DATA, SOME_ENCRYPTED_PAYMENT_ID, JSON_MEDIA_TYPE);

        // Then
        verify(spiPaymentFactory, never()).getSpiPayment(any());

        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    void readPaymentStatus_spiPaymentFactory_getSpiPayment_failed() {
        //Given
        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                        .build();

        when(spiPaymentFactory.getSpiPayment(commonPaymentData))
            .thenReturn(Optional.empty());

        //When
        ReadPaymentStatusResponse actualResponse = readSinglePaymentStatusService.readPaymentStatus(commonPaymentData, SPI_CONTEXT_DATA, SOME_ENCRYPTED_PAYMENT_ID, JSON_MEDIA_TYPE);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder().getTppMessageInformationList().iterator().next().getMessageErrorCode()).isEqualTo(expectedError.getTppMessageInformationList().iterator().next().getMessageErrorCode());
    }

    @Test
    void readPaymentStatus_singlePaymentSpi_getPaymentStatusById_failed() {
        //Given
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                        .build();

        doReturn(Optional.of(SPI_SINGLE_PAYMENT)).when(spiPaymentFactory).getSpiPayment(commonPaymentData);
        when(singlePaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, JSON_MEDIA_TYPE, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(TRANSACTION_RESPONSE_FAILURE);
        when(spiErrorMapper.mapToErrorHolder(TRANSACTION_RESPONSE_FAILURE, ServiceType.PIS))
            .thenReturn(expectedError);

        //When
        ReadPaymentStatusResponse actualResponse = readSinglePaymentStatusService.readPaymentStatus(commonPaymentData, SPI_CONTEXT_DATA, SOME_ENCRYPTED_PAYMENT_ID, JSON_MEDIA_TYPE);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static SpiResponse<SpiGetPaymentStatusResponse> buildSpiResponseTransactionStatus() {
        return SpiResponse.<SpiGetPaymentStatusResponse>builder()
                   .payload(TRANSACTION_STATUS)
                   .build();
    }

    private static SpiResponse<SpiGetPaymentStatusResponse> buildFailSpiResponseTransactionStatus() {
        return SpiResponse.<SpiGetPaymentStatusResponse>builder()
                   .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                   .build();
    }

    private static PisCommonPaymentResponse getCommonPaymentData() {
        PisCommonPaymentResponse paymentData = new PisCommonPaymentResponse();
        paymentData.setPaymentProduct(PRODUCT);
        paymentData.setPaymentData(PAYMENT_BODY);
        return paymentData;
    }
}
