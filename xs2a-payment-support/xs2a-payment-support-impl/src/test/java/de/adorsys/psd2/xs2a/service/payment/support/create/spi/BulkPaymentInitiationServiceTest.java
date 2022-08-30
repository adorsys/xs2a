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
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.support.TestSpiDataProvider;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkPaymentInitiationServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.defaultSpiContextData();

    private static final BulkPayment BULK_PAYMENT = buildBulkPayment();
    private static final SpiBulkPayment SPI_BULK_PAYMENT = new SpiBulkPayment();
    private static final SpiBulkPaymentInitiationResponse SPI_BULK_PAYMENT_RESPONSE = buildSpiBulkPaymentInitiationResponse();
    private static final SpiResponse<SpiBulkPaymentInitiationResponse> SPI_BULK_RESPONSE = buildSpiResponse();
    private static final BulkPaymentInitiationResponse BULK_PAYMENT_RESPONSE = new BulkPaymentInitiationResponse();

    private static final TppMessage FORMAT_ERROR = new TppMessage(MessageErrorCode.FORMAT_ERROR);
    private static final ErrorHolder EXPECTED_ERROR = ErrorHolder.builder(ErrorType.PIS_404)
                                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                                          .build();

    @Mock
    private BulkPaymentSpi bulkPaymentSpi;
    @Mock
    private Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
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
    private BulkPaymentInitiationService bulkPaymentService;

    @BeforeEach
    void init() {
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
    }

    @Test
    void createBulkPayment_success() {
        // Given
        when(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(BULK_PAYMENT, PRODUCT))
            .thenReturn(SPI_BULK_PAYMENT);
        when(bulkPaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_BULK_PAYMENT, initialSpiAspspConsentDataProvider))
            .thenReturn(SPI_BULK_RESPONSE);
        when(spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(SPI_BULK_PAYMENT_RESPONSE, initialSpiAspspConsentDataProvider))
            .thenReturn(BULK_PAYMENT_RESPONSE);

        // When
        PaymentInitiationResponse actualResponse = bulkPaymentService.initiatePayment(BULK_PAYMENT, PRODUCT, PSU_DATA);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isNull();
        assertThat(actualResponse).isEqualTo(BULK_PAYMENT_RESPONSE);
    }

    @Test
    void createBulkPayment_bulkPaymentSpi_initiatePayment_failed() {
        // Given
        SpiResponse<SpiBulkPaymentInitiationResponse> expectedFailureResponse = SpiResponse.<SpiBulkPaymentInitiationResponse>builder()
                                                                                    .error(FORMAT_ERROR)
                                                                                    .build();
        when(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(BULK_PAYMENT, PRODUCT))
            .thenReturn(SPI_BULK_PAYMENT);
        when(bulkPaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_BULK_PAYMENT, initialSpiAspspConsentDataProvider))
            .thenReturn(expectedFailureResponse);
        when(spiErrorMapper.mapToErrorHolder(expectedFailureResponse, ServiceType.PIS))
            .thenReturn(EXPECTED_ERROR);

        // When
        PaymentInitiationResponse actualResponse = bulkPaymentService.initiatePayment(BULK_PAYMENT, PRODUCT, PSU_DATA);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(EXPECTED_ERROR);
    }

    private static BulkPayment buildBulkPayment() {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setPayments(Collections.singletonList(new SinglePayment()));
        bulkPayment.setRequestedExecutionDate(LocalDate.now());
        bulkPayment.setBatchBookingPreferred(false);
        return bulkPayment;
    }

    private static SpiBulkPaymentInitiationResponse buildSpiBulkPaymentInitiationResponse() {
        SpiBulkPaymentInitiationResponse response = new SpiBulkPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspAccountId(ASPSP_ACCOUNT_ID);
        return response;
    }

    private static SpiResponse<SpiBulkPaymentInitiationResponse> buildSpiResponse() {
        return SpiResponse.<SpiBulkPaymentInitiationResponse>builder()
                   .payload(SPI_BULK_PAYMENT_RESPONSE)
                   .build();
    }
}
