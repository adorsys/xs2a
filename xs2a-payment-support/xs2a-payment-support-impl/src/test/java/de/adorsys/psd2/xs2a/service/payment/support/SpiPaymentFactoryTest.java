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

package de.adorsys.psd2.xs2a.service.payment.support;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.CmsToXs2aPaymentSupportMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpiPaymentFactoryTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final SinglePayment SINGLE_PAYMENT = new SinglePayment();
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private static final PeriodicPayment PERIODIC_PAYMENT = new PeriodicPayment();
    private static final SpiPeriodicPayment SPI_PERIODIC_PAYMENT = new SpiPeriodicPayment(PRODUCT);
    private static final BulkPayment BULK_PAYMENT = new BulkPayment();
    private static final SpiBulkPayment SPI_BULK_PAYMENT = new SpiBulkPayment();

    @InjectMocks
    private SpiPaymentFactoryImpl spiPaymentFactory;

    @Mock
    private CmsToXs2aPaymentSupportMapper cmsToXs2APaymentSupportMapper;
    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    @Mock
    private Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    @Mock
    private Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;

    @Test
    void createSpiPaymentByPaymentType_single_success() {
        // Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.SINGLE);
        when(cmsToXs2APaymentSupportMapper.mapToSinglePayment(commonPaymentData))
            .thenReturn(SINGLE_PAYMENT);
        when(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(SINGLE_PAYMENT, PRODUCT))
            .thenReturn(SPI_SINGLE_PAYMENT);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isPresent().contains(SPI_SINGLE_PAYMENT);
    }

    @Test
    void createSpiPaymentByPaymentType_single_failed() {
        // Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.SINGLE);
        when(cmsToXs2APaymentSupportMapper.mapToSinglePayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isEmpty();
    }

    @Test
    void createSpiPaymentByPaymentType_periodic_success() {
        // Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2APaymentSupportMapper.mapToPeriodicPayment(commonPaymentData))
            .thenReturn(PERIODIC_PAYMENT);
        when(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(PERIODIC_PAYMENT, PRODUCT))
            .thenReturn(SPI_PERIODIC_PAYMENT);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isPresent().contains(SPI_PERIODIC_PAYMENT);
    }

    @Test
    void createSpiPaymentByPaymentType_periodic_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2APaymentSupportMapper.mapToPeriodicPayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isEmpty();
    }

    @Test
    void createSpiPaymentByPaymentType_bulk_success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.BULK);
        when(cmsToXs2APaymentSupportMapper.mapToBulkPayment(commonPaymentData))
            .thenReturn(BULK_PAYMENT);
        when(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(BULK_PAYMENT, PRODUCT))
            .thenReturn(SPI_BULK_PAYMENT);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isPresent().contains(SPI_BULK_PAYMENT);
    }

    @Test
    void createSpiPaymentByPaymentType_bulk_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.BULK);
        when(cmsToXs2APaymentSupportMapper.mapToBulkPayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isEmpty();
    }

    @Test
    void createSpiSinglePayment_success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.SINGLE);
        when(cmsToXs2APaymentSupportMapper.mapToSinglePayment(commonPaymentData))
            .thenReturn(SINGLE_PAYMENT);
        when(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(SINGLE_PAYMENT, PRODUCT))
            .thenReturn(SPI_SINGLE_PAYMENT);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isPresent().contains(SPI_SINGLE_PAYMENT);
    }

    @Test
    void createSpiSinglePayment_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.SINGLE);
        when(cmsToXs2APaymentSupportMapper.mapToSinglePayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isEmpty();
    }

    @Test
    void createSpiPeriodicPayment_success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2APaymentSupportMapper.mapToPeriodicPayment(commonPaymentData))
            .thenReturn(PERIODIC_PAYMENT);
        when(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(PERIODIC_PAYMENT, PRODUCT))
            .thenReturn(SPI_PERIODIC_PAYMENT);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isPresent().contains(SPI_PERIODIC_PAYMENT);
    }

    @Test
    void createSpiPeriodicPayment_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2APaymentSupportMapper.mapToPeriodicPayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isEmpty();
    }

    @Test
    void createSpiBulkPayment_success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.BULK);
        when(cmsToXs2APaymentSupportMapper.mapToBulkPayment(commonPaymentData))
            .thenReturn(BULK_PAYMENT);
        when(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(BULK_PAYMENT, PRODUCT))
            .thenReturn(SPI_BULK_PAYMENT);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isPresent().contains(SPI_BULK_PAYMENT);
    }

    @Test
    void createSpiBulkPayment_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);

        //When
        Optional<SpiPayment> actualResponse = spiPaymentFactory.getSpiPayment(commonPaymentData);

        //Then
        assertThat(actualResponse).isEmpty();
    }

    private CommonPaymentData buildCommonPaymentData(PaymentType paymentType) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setPaymentType(paymentType);
        pisCommonPaymentResponse.setPaymentProduct(PRODUCT);
        return pisCommonPaymentResponse;
    }
}
