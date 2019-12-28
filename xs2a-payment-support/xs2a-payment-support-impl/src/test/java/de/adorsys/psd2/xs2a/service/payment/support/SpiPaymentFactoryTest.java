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

package de.adorsys.psd2.xs2a.service.payment.support;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.CmsToXs2aPaymentMapperSupport;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpiPaymentFactoryTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final SinglePayment SINGLE_PAYMENT = new SinglePayment();
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private static final PeriodicPayment PERIODIC_PAYMENT = new PeriodicPayment();
    private static final SpiPeriodicPayment SPI_PERIODIC_PAYMENT = new SpiPeriodicPayment(PRODUCT);
    private static final BulkPayment BULK_PAYMENT = new BulkPayment();
    private static final SpiBulkPayment SPI_BULK_PAYMENT = new SpiBulkPayment();

    @InjectMocks
    private SpiPaymentFactory spiPaymentFactory;

    @Mock
    private CmsToXs2aPaymentMapperSupport cmsToXs2aPaymentMapperSupport;
    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    @Mock
    private Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    @Mock
    private Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    @Mock
    private RequestProviderService requestProviderService;

    @Before
    public void setUp() {
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
    }

    @Test
    public void createSpiPaymentByPaymentType_single_success() {
        // Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.SINGLE);
        when(cmsToXs2aPaymentMapperSupport.mapToSinglePayment(commonPaymentData))
            .thenReturn(SINGLE_PAYMENT);
        when(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(SINGLE_PAYMENT, PRODUCT))
            .thenReturn(SPI_SINGLE_PAYMENT);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SPI_SINGLE_PAYMENT);
    }

    @Test
    public void createSpiPaymentByPaymentType_single_failed() {
        // Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.SINGLE);
        when(cmsToXs2aPaymentMapperSupport.mapToSinglePayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiPaymentByPaymentType_periodic_success() {
        // Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2aPaymentMapperSupport.mapToPeriodicPayment(commonPaymentData))
            .thenReturn(PERIODIC_PAYMENT);
        when(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(PERIODIC_PAYMENT, PRODUCT))
            .thenReturn(SPI_PERIODIC_PAYMENT);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SPI_PERIODIC_PAYMENT);
    }

    @Test
    public void createSpiPaymentByPaymentType_periodic_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2aPaymentMapperSupport.mapToPeriodicPayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiPaymentByPaymentType_bulk_success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.BULK);
        when(cmsToXs2aPaymentMapperSupport.mapToBulkPayment(commonPaymentData))
            .thenReturn(BULK_PAYMENT);
        when(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(BULK_PAYMENT, PRODUCT))
            .thenReturn(SPI_BULK_PAYMENT);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SPI_BULK_PAYMENT);
    }

    @Test
    public void createSpiPaymentByPaymentType_bulk_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.BULK);
        when(cmsToXs2aPaymentMapperSupport.mapToBulkPayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiSinglePayment_success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.SINGLE);
        when(cmsToXs2aPaymentMapperSupport.mapToSinglePayment(commonPaymentData))
            .thenReturn(SINGLE_PAYMENT);
        when(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(SINGLE_PAYMENT, PRODUCT))
            .thenReturn(SPI_SINGLE_PAYMENT);

        //When
        Optional<SpiSinglePayment> actualResponse = spiPaymentFactory.createSpiSinglePayment(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SPI_SINGLE_PAYMENT);
    }

    @Test
    public void createSpiSinglePayment_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.SINGLE);
        when(cmsToXs2aPaymentMapperSupport.mapToSinglePayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<SpiSinglePayment> actualResponse = spiPaymentFactory.createSpiSinglePayment(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiPeriodicPayment_success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2aPaymentMapperSupport.mapToPeriodicPayment(commonPaymentData))
            .thenReturn(PERIODIC_PAYMENT);
        when(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(PERIODIC_PAYMENT, PRODUCT))
            .thenReturn(SPI_PERIODIC_PAYMENT);

        //When
        Optional<SpiPeriodicPayment> actualResponse = spiPaymentFactory.createSpiPeriodicPayment(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SPI_PERIODIC_PAYMENT);
    }

    @Test
    public void createSpiPeriodicPayment_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2aPaymentMapperSupport.mapToPeriodicPayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<SpiPeriodicPayment> actualResponse = spiPaymentFactory.createSpiPeriodicPayment(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiBulkPayment_success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2aPaymentMapperSupport.mapToBulkPayment(commonPaymentData))
            .thenReturn(BULK_PAYMENT);
        when(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(BULK_PAYMENT, PRODUCT))
            .thenReturn(SPI_BULK_PAYMENT);

        //When
        Optional<SpiBulkPayment> actualResponse = spiPaymentFactory.createSpiBulkPayment(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SPI_BULK_PAYMENT);
    }

    @Test
    public void createSpiBulkPayment_failed() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData(PaymentType.PERIODIC);
        when(cmsToXs2aPaymentMapperSupport.mapToBulkPayment(commonPaymentData))
            .thenReturn(null);

        //When
        Optional<SpiBulkPayment> actualResponse = spiPaymentFactory.createSpiBulkPayment(commonPaymentData);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    public CommonPaymentData buildCommonPaymentData(PaymentType paymentType) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setPaymentType(paymentType);
        pisCommonPaymentResponse.setPaymentProduct(PRODUCT);
        return pisCommonPaymentResponse;
    }
}
