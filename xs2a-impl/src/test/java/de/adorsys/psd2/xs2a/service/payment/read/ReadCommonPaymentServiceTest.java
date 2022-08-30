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

package de.adorsys.psd2.xs2a.service.payment.read;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ContentType;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadCommonPaymentServiceTest {
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final CommonPaymentData COMMON_PAYMENT = buildCommonPaymentData();
    private static final SpiPaymentInfo SPI_PAYMENT_INFO = new SpiPaymentInfo(PRODUCT);
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final CommonPayment PIS_PAYMENT_INFO = getCommonPayment();
    private static final String SOME_ENCRYPTED_PAYMENT_ID = "Encrypted Payment Id";
    private static final String ACCEPT_MEDIA_TYPE = ContentType.JSON.getType();

    @InjectMocks
    private ReadCommonPaymentService readCommonPaymentService;

    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    @Mock
    private SpiToXs2aPaymentInfoMapper spiToXs2aPaymentInfoMapper;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private SpiErrorMapper spiErrorMapper;

    @Test
    void getPayment_success() {
        when(xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(COMMON_PAYMENT)).thenReturn(SPI_PAYMENT_INFO);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(spiToXs2aPaymentInfoMapper.mapToXs2aPaymentInfo(any())).thenReturn(PIS_PAYMENT_INFO);
        when(commonPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPaymentInfo>builder()
                            .payload(SPI_PAYMENT_INFO)
                            .build());
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readCommonPaymentService.getPayment(COMMON_PAYMENT, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isEqualTo(PIS_PAYMENT_INFO);
    }

    @Test
    void getPayment_failed() {
        when(xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(COMMON_PAYMENT)).thenReturn(SPI_PAYMENT_INFO);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(commonPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPaymentInfo>builder()
                            .payload(SPI_PAYMENT_INFO)
                            .build());
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);

        // Given
        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                        .build();
        SpiResponse<SpiPaymentInfo> failSpiResponse = SpiResponse.<SpiPaymentInfo>builder()
                                                          .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                                                          .build();

        when(commonPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, ACCEPT_MEDIA_TYPE, SPI_PAYMENT_INFO, spiAspspConsentDataProvider)).thenReturn(failSpiResponse);
        when(spiErrorMapper.mapToErrorHolder(failSpiResponse, ServiceType.PIS))
            .thenReturn(expectedError);

        // When
        PaymentInformationResponse<CommonPayment> actualResponse = readCommonPaymentService.getPayment(COMMON_PAYMENT, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID, ACCEPT_MEDIA_TYPE);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static CommonPaymentData buildCommonPaymentData() {
        PisCommonPaymentResponse request = new PisCommonPaymentResponse();
        request.setPaymentType(PaymentType.SINGLE);
        request.setPaymentProduct("sepa-credit-transfers");
        request.setPaymentData(new byte[16]);

        return request;
    }

    private static CommonPayment getCommonPayment() {
        return new CommonPayment();
    }
}
