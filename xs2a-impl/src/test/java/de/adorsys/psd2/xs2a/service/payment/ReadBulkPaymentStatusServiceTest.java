/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadBulkPaymentStatusServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private final static UUID X_REQUEST_ID = UUID.randomUUID();
    private static final List<PisPayment> PIS_PAYMENTS = getListPisPayment();
    private static final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();
    private static final SpiBulkPayment SPI_BULK_PAYMENT = new SpiBulkPayment();
    private static final SpiGetPaymentStatusResponse TRANSACTION_STATUS = new SpiGetPaymentStatusResponse(TransactionStatus.ACSP, null);
    private static final SpiResponse<SpiGetPaymentStatusResponse> TRANSACTION_RESPONSE = buildSpiResponseTransactionStatus();
    private static final SpiResponse<SpiGetPaymentStatusResponse> TRANSACTION_RESPONSE_FAILURE = buildFailSpiResponseTransactionStatus();
    private static final ReadPaymentStatusResponse READ_PAYMENT_STATUS_RESPONSE = new ReadPaymentStatusResponse(TRANSACTION_RESPONSE.getPayload().getTransactionStatus(), TRANSACTION_RESPONSE.getPayload().getFundsAvailable());
    private static final String SOME_ENCRYPTED_PAYMENT_ID = "Encrypted Payment Id";

    @InjectMocks
    private ReadBulkPaymentStatusService readBulkPaymentStatusService;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private SpiPaymentFactory spiPaymentFactory;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private BulkPaymentSpi bulkPaymentSpi;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private RequestProviderService requestProviderService;

    @Before
    public void setUp() {
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
    }

    @Test
    public void readPaymentStatus_success() {
        // Given
        when(spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);
        when(spiPaymentFactory.createSpiBulkPayment(PIS_PAYMENTS, PRODUCT))
            .thenReturn(Optional.of(SPI_BULK_PAYMENT));
        when(bulkPaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, SPI_BULK_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(TRANSACTION_RESPONSE);

        // When
        ReadPaymentStatusResponse actualResponse = readBulkPaymentStatusService.readPaymentStatus(PIS_PAYMENTS, PRODUCT, SPI_CONTEXT_DATA, SOME_ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(actualResponse).isEqualTo(READ_PAYMENT_STATUS_RESPONSE);
    }

    @Test
    public void readPaymentStatus_spiPaymentFactory_createSpiBulkPayment_failed() {
        //Given
        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404, "Payment not found"))
                                        .build();

        when(spiPaymentFactory.createSpiBulkPayment(PIS_PAYMENTS, PRODUCT))
            .thenReturn(Optional.empty());

        // When
        ReadPaymentStatusResponse actualResponse = readBulkPaymentStatusService.readPaymentStatus(PIS_PAYMENTS, PRODUCT, SPI_CONTEXT_DATA, SOME_ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    public void readPaymentStatus_bulkPaymentSpi_getPaymentStatusById_failed() {
        //Given
        ErrorHolder expectedError = ErrorHolder.builder(ErrorType.PIS_404)
                                        .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404, "Payment not found"))
                                        .build();

        when(spiPaymentFactory.createSpiBulkPayment(PIS_PAYMENTS, PRODUCT))
            .thenReturn(Optional.of(SPI_BULK_PAYMENT));
        when(bulkPaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, SPI_BULK_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(TRANSACTION_RESPONSE_FAILURE);
        when(spiErrorMapper.mapToErrorHolder(TRANSACTION_RESPONSE_FAILURE, ServiceType.PIS))
            .thenReturn(expectedError);
        when((spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString())))
            .thenReturn(spiAspspConsentDataProvider);

        // When
        ReadPaymentStatusResponse actualResponse = readBulkPaymentStatusService.readPaymentStatus(PIS_PAYMENTS, PRODUCT, SPI_CONTEXT_DATA, SOME_ENCRYPTED_PAYMENT_ID);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType"),
            new TppInfo(),
            X_REQUEST_ID
        );
    }

    private static SpiResponse<SpiGetPaymentStatusResponse> buildSpiResponseTransactionStatus() {
        return SpiResponse.<SpiGetPaymentStatusResponse>builder()
                   .payload(TRANSACTION_STATUS)
                   .build();
    }

    private static SpiResponse<SpiGetPaymentStatusResponse> buildFailSpiResponseTransactionStatus() {
        return SpiResponse.<SpiGetPaymentStatusResponse>builder()
                   .error(new TppMessage(MessageErrorCode.FORMAT_ERROR, "Format error"))
                   .build();
    }

    private static List<PisPayment> getListPisPayment() {
        return Collections.singletonList(new PisPayment());
    }
}
