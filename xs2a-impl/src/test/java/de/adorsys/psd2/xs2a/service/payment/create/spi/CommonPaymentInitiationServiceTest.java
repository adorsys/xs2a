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

package de.adorsys.psd2.xs2a.service.payment.create.spi;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfo;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiCommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonPaymentInitiationServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();

    private static final CommonPayment COMMON_PAYMENT = buildCommonPayment();
    private static final SpiPaymentInfo SPI_PAYMENT_INFO = new SpiPaymentInfo(PRODUCT);
    private static final SpiCommonPaymentInitiationResponse SPI_PAYMENT_INITIATION_RESPONSE = new SpiCommonPaymentInitiationResponse();
    private static final SpiResponse<SpiPaymentInitiationResponse> SPI_COMMON_RESPONSE = buildSpiResponse();
    private static final CommonPaymentInitiationResponse COMMON_PAYMENT_RESPONSE = new CommonPaymentInitiationResponse();
    private static final TppMessage FORMAT_ERROR = new TppMessage(MessageErrorCode.FORMAT_ERROR);
    private static final ErrorHolder EXPECTED_ERROR = ErrorHolder.builder(ErrorType.PIS_404)
                                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                                          .build();
    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private Xs2aToSpiPaymentInfo xs2aToSpiPaymentInfo;
    @Mock
    private SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    @InjectMocks
    private CommonPaymentInitiationService commonPaymentService;

    @BeforeEach
    void init() {
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
    }

    @Test
    void createCommonPayment_success() {
        when(xs2aToSpiPaymentInfo.mapToSpiPaymentRequest(COMMON_PAYMENT, PRODUCT))
            .thenReturn(SPI_PAYMENT_INFO);
        when(commonPaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_PAYMENT_INFO, initialSpiAspspConsentDataProvider))
            .thenReturn(SPI_COMMON_RESPONSE);
        when(spiToXs2aPaymentMapper.mapToCommonPaymentInitiateResponse(SPI_COMMON_RESPONSE.getPayload(), COMMON_PAYMENT.getPaymentType(), initialSpiAspspConsentDataProvider))
            .thenReturn(COMMON_PAYMENT_RESPONSE);

        // When
        PaymentInitiationResponse actualResponse = commonPaymentService.initiatePayment(COMMON_PAYMENT, PRODUCT, PSU_DATA);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isNull();
        assertThat(actualResponse).isEqualTo(COMMON_PAYMENT_RESPONSE);
    }

    @Test
    void createCommonPayment_commonPaymentSpi_initiatePayment_failed() {
        // Given
        SpiResponse<SpiPaymentInitiationResponse> expectedFailureResponse = SpiResponse.<SpiPaymentInitiationResponse>builder()
                                                                                .error(FORMAT_ERROR)
                                                                                .build();
        when(xs2aToSpiPaymentInfo.mapToSpiPaymentRequest(COMMON_PAYMENT, PRODUCT))
            .thenReturn(SPI_PAYMENT_INFO);
        when(commonPaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_PAYMENT_INFO, initialSpiAspspConsentDataProvider))
            .thenReturn(expectedFailureResponse);
        when(spiErrorMapper.mapToErrorHolder(expectedFailureResponse, ServiceType.PIS))
            .thenReturn(EXPECTED_ERROR);

        // When
        PaymentInitiationResponse actualResponse = commonPaymentService.initiatePayment(COMMON_PAYMENT, PRODUCT, PSU_DATA);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(EXPECTED_ERROR);
    }

    private static CommonPayment buildCommonPayment() {
        CommonPayment request = new CommonPayment();
        request.setPaymentType(PaymentType.SINGLE);
        request.setPaymentProduct("sepa-credit-transfers");
        request.setPaymentData(new byte[16]);
        return request;
    }

    private static SpiResponse<SpiPaymentInitiationResponse> buildSpiResponse() {
        return SpiResponse.<SpiPaymentInitiationResponse>builder()
                   .payload(SPI_PAYMENT_INITIATION_RESPONSE)
                   .build();
    }
}
