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

package de.adorsys.psd2.xs2a.service.payment.support.create.spi;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.support.TestSpiDataProvider;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SinglePaymentInitiationServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.defaultSpiContextData();

    private static final SinglePayment SINGLE_PAYMENT = new SinglePayment();
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private static final SpiSinglePaymentInitiationResponse SPI_SINGLE_PAYMENT_RESPONSE = buildSpiSinglePaymentInitiationResponse();
    private static final SpiResponse<SpiSinglePaymentInitiationResponse> SPI_SINGLE_RESPONSE = buildSpiResponse();
    private static final SinglePaymentInitiationResponse SINGLE_PAYMENT_RESPONSE = new SinglePaymentInitiationResponse();

    private static final TppMessage FORMAT_ERROR = new TppMessage(MessageErrorCode.FORMAT_ERROR);
    private static final ErrorHolder EXPECTED_ERROR = ErrorHolder.builder(ErrorType.PIS_404)
                                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                                          .build();

    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2AToSpiSinglePaymentMapper;
    @Mock
    private SinglePaymentSpi singlePaymentSpi;

    @InjectMocks
    private SinglePaymentInitiationService singlePaymentService;

    @Mock
    private InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider;

    @BeforeEach
    void setUp() {
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
    }

    @Test
    void createSinglePayment_success() {
        //Given
        when(xs2AToSpiSinglePaymentMapper.mapToSpiSinglePayment(SINGLE_PAYMENT, PRODUCT))
            .thenReturn(SPI_SINGLE_PAYMENT);
        when(singlePaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, initialSpiAspspConsentDataProvider))
            .thenReturn(SPI_SINGLE_RESPONSE);
        when(spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(SPI_SINGLE_PAYMENT_RESPONSE, initialSpiAspspConsentDataProvider))
            .thenReturn(SINGLE_PAYMENT_RESPONSE);

        //When
        PaymentInitiationResponse actualResponse = singlePaymentService.initiatePayment(SINGLE_PAYMENT, PRODUCT, PSU_DATA);

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isNull();
        assertThat(actualResponse).isEqualTo(SINGLE_PAYMENT_RESPONSE);
    }

    @Test
    void createSinglePayment_singlePaymentSpi_initiatePayment_failed() {
        // Given
        SpiResponse<SpiSinglePaymentInitiationResponse> expectedFailureResponse = SpiResponse.<SpiSinglePaymentInitiationResponse>builder()
                                                                                      .error(FORMAT_ERROR)
                                                                                      .build();

        when(xs2AToSpiSinglePaymentMapper.mapToSpiSinglePayment(SINGLE_PAYMENT, PRODUCT))
            .thenReturn(SPI_SINGLE_PAYMENT);
        when(singlePaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, initialSpiAspspConsentDataProvider))
            .thenReturn(expectedFailureResponse);
        when(spiErrorMapper.mapToErrorHolder(expectedFailureResponse, ServiceType.PIS))
            .thenReturn(EXPECTED_ERROR);

        // When
        PaymentInitiationResponse actualResponse = singlePaymentService.initiatePayment(SINGLE_PAYMENT, PRODUCT, PSU_DATA);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(EXPECTED_ERROR);
    }

    private static SpiSinglePaymentInitiationResponse buildSpiSinglePaymentInitiationResponse() {
        SpiSinglePaymentInitiationResponse response = new SpiSinglePaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspAccountId(ASPSP_ACCOUNT_ID);
        return response;
    }

    private static SpiResponse<SpiSinglePaymentInitiationResponse> buildSpiResponse() {
        return SpiResponse.<SpiSinglePaymentInitiationResponse>builder()
                   .payload(SPI_SINGLE_PAYMENT_RESPONSE)
                   .build();
    }
}
