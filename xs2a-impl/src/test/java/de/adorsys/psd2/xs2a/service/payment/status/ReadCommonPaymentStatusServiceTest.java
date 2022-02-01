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

package de.adorsys.psd2.xs2a.service.payment.status;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.ContentType;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.mapper.MediaTypeMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadCommonPaymentStatusServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final CommonPayment COMMON_PAYMENT = new CommonPayment();
    private static final SpiPaymentInfo SPI_PAYMENT_INFO = new SpiPaymentInfo(PRODUCT);
    private static final String JSON_MEDIA_TYPE = ContentType.JSON.getType();
    private static final String PSU_MESSAGE = "PSU message";
    private static final SpiGetPaymentStatusResponse TRANSACTION_STATUS = new SpiGetPaymentStatusResponse(TransactionStatus.ACSP, null, JSON_MEDIA_TYPE, null, PSU_MESSAGE, null, null);
    private static final SpiResponse<SpiGetPaymentStatusResponse> TRANSACTION_RESPONSE = buildSpiResponseTransactionStatus();
    private static final SpiResponse<SpiGetPaymentStatusResponse> TRANSACTION_RESPONSE_FAILURE = buildFailSpiResponseTransactionStatus();
    private static final ReadPaymentStatusResponse READ_PAYMENT_STATUS_RESPONSE = new ReadPaymentStatusResponse(TRANSACTION_RESPONSE.getPayload().getTransactionStatus(), TRANSACTION_RESPONSE.getPayload().getFundsAvailable(), MediaType.APPLICATION_JSON, null, PSU_MESSAGE, null, null);
    private static final PisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = new PisCommonPaymentResponse();
    private static final String SOME_ENCRYPTED_PAYMENT_ID = "Encrypted Payment Id";

    @InjectMocks
    private ReadCommonPaymentStatusService readCommonPaymentStatusService;

    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    @Mock
    private CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private MediaTypeMapper mediaTypeMapper;
    @Mock
    private SpiToXs2aLinksMapper spiToXs2aLinksMapper;

    @BeforeEach
    void init() {
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);
        when(cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(PIS_COMMON_PAYMENT_RESPONSE))
            .thenReturn(COMMON_PAYMENT);
        when(xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(COMMON_PAYMENT))
            .thenReturn(SPI_PAYMENT_INFO);
    }

    @Test
    void readPaymentStatus_success() {
        // Given
        when(commonPaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, JSON_MEDIA_TYPE, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(TRANSACTION_RESPONSE);
        when(mediaTypeMapper.mapToMediaType(JSON_MEDIA_TYPE))
            .thenReturn(MediaType.APPLICATION_JSON);

        // When
        ReadPaymentStatusResponse actualResponse = readCommonPaymentStatusService.readPaymentStatus(PIS_COMMON_PAYMENT_RESPONSE, SPI_CONTEXT_DATA, SOME_ENCRYPTED_PAYMENT_ID, JSON_MEDIA_TYPE);

        // Then
        assertThat(actualResponse).isEqualTo(READ_PAYMENT_STATUS_RESPONSE);
    }

    @Test
    void readPaymentStatus_failed() {
        // Given
        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                        .build();

        when(commonPaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, JSON_MEDIA_TYPE, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(TRANSACTION_RESPONSE_FAILURE);
        when(spiErrorMapper.mapToErrorHolder(TRANSACTION_RESPONSE_FAILURE, ServiceType.PIS))
            .thenReturn(expectedError);

        // When
        ReadPaymentStatusResponse actualResponse = readCommonPaymentStatusService.readPaymentStatus(PIS_COMMON_PAYMENT_RESPONSE, SPI_CONTEXT_DATA, SOME_ENCRYPTED_PAYMENT_ID, JSON_MEDIA_TYPE);

        // Then
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
}
