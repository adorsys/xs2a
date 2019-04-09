package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpiPaymentFactoryTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PisPayment PIS_PAYMENT = new PisPayment();
    private static final List<PisPayment> PIS_PAYMENTS = getListPisPayment();
    private static final SinglePayment SINGLE_PAYMENT = new SinglePayment();
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private static final PeriodicPayment PERIODIC_PAYMENT = new PeriodicPayment();
    private static final SpiPeriodicPayment SPI_PERIODIC_PAYMENT = new SpiPeriodicPayment(PRODUCT);
    private static final BulkPayment BULK_PAYMENT = new BulkPayment();
    private static final SpiBulkPayment SPI_BULK_PAYMENT = new SpiBulkPayment();

    @InjectMocks
    private SpiPaymentFactory spiPaymentFactory;

    @Mock
    private CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    @Mock
    private Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    @Mock
    private Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;

    @Test
    public void createSpiPaymentByPaymentType_single_success() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToSinglePayment(PIS_PAYMENT))
            .thenReturn(SINGLE_PAYMENT);
        when(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(SINGLE_PAYMENT, PRODUCT))
            .thenReturn(SPI_SINGLE_PAYMENT);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(PIS_PAYMENTS, PRODUCT, PaymentType.SINGLE);

        //Then
        assertThat(actualResponse.get()).isEqualTo(SPI_SINGLE_PAYMENT);
    }

    @Test
    public void createSpiPaymentByPaymentType_single_failed() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToSinglePayment(PIS_PAYMENT))
            .thenReturn(null);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(PIS_PAYMENTS, PRODUCT, PaymentType.SINGLE);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiPaymentByPaymentType_periodic_success() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToPeriodicPayment(PIS_PAYMENT))
            .thenReturn(PERIODIC_PAYMENT);
        when(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(PERIODIC_PAYMENT, PRODUCT))
            .thenReturn(SPI_PERIODIC_PAYMENT);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(PIS_PAYMENTS, PRODUCT, PaymentType.PERIODIC);

        //Then
        assertThat(actualResponse.get()).isEqualTo(SPI_PERIODIC_PAYMENT);
    }

    @Test
    public void createSpiPaymentByPaymentType_periodic_failed() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToPeriodicPayment(PIS_PAYMENT))
            .thenReturn(null);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(PIS_PAYMENTS, PRODUCT, PaymentType.PERIODIC);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiPaymentByPaymentType_bulk_success() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToBulkPayment(PIS_PAYMENTS))
            .thenReturn(BULK_PAYMENT);
        when(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(BULK_PAYMENT, PRODUCT))
            .thenReturn(SPI_BULK_PAYMENT);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(PIS_PAYMENTS, PRODUCT, PaymentType.BULK);

        //Then
        assertThat(actualResponse.get()).isEqualTo(SPI_BULK_PAYMENT);
    }

    @Test
    public void createSpiPaymentByPaymentType_bulk_failed() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToBulkPayment(PIS_PAYMENTS))
            .thenReturn(null);

        //When
        Optional<? extends SpiPayment> actualResponse = spiPaymentFactory.createSpiPaymentByPaymentType(PIS_PAYMENTS, PRODUCT, PaymentType.BULK);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiSinglePayment_success() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToSinglePayment(PIS_PAYMENT))
            .thenReturn(SINGLE_PAYMENT);
        when(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(SINGLE_PAYMENT, PRODUCT))
            .thenReturn(SPI_SINGLE_PAYMENT);

        //When
        Optional<SpiSinglePayment> actualResponse = spiPaymentFactory.createSpiSinglePayment(PIS_PAYMENT, PRODUCT);

        //Then
        assertThat(actualResponse.get()).isEqualTo(SPI_SINGLE_PAYMENT);
    }

    @Test
    public void createSpiSinglePayment_failed() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToSinglePayment(PIS_PAYMENT))
            .thenReturn(null);

        //When
        Optional<SpiSinglePayment> actualResponse = spiPaymentFactory.createSpiSinglePayment(PIS_PAYMENT, PRODUCT);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiPeriodicPayment_success() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToPeriodicPayment(PIS_PAYMENT))
            .thenReturn(PERIODIC_PAYMENT);
        when(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(PERIODIC_PAYMENT, PRODUCT))
            .thenReturn(SPI_PERIODIC_PAYMENT);

        //When
        Optional<SpiPeriodicPayment> actualResponse = spiPaymentFactory.createSpiPeriodicPayment(PIS_PAYMENT, PRODUCT);

        //Then
        assertThat(actualResponse.get()).isEqualTo(SPI_PERIODIC_PAYMENT);
    }

    @Test
    public void createSpiPeriodicPayment_failed() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToPeriodicPayment(PIS_PAYMENT))
            .thenReturn(null);

        //When
        Optional<SpiPeriodicPayment> actualResponse = spiPaymentFactory.createSpiPeriodicPayment(PIS_PAYMENT, PRODUCT);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    @Test
    public void createSpiBulkPayment_success() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToBulkPayment(PIS_PAYMENTS))
            .thenReturn(BULK_PAYMENT);
        when(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(BULK_PAYMENT, PRODUCT))
            .thenReturn(SPI_BULK_PAYMENT);

        //When
        Optional<SpiBulkPayment> actualResponse = spiPaymentFactory.createSpiBulkPayment(PIS_PAYMENTS, PRODUCT);

        //Then
        assertThat(actualResponse.get()).isEqualTo(SPI_BULK_PAYMENT);
    }

    @Test
    public void createSpiBulkPayment_failed() {
        //Given
        when(cmsToXs2aPaymentMapper.mapToBulkPayment(PIS_PAYMENTS))
            .thenReturn(null);

        //When
        Optional<SpiBulkPayment> actualResponse = spiPaymentFactory.createSpiBulkPayment(PIS_PAYMENTS, PRODUCT);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
        assertThat(actualResponse).isEqualTo(Optional.empty());
    }

    private static List<PisPayment> getListPisPayment() {
        return Collections.singletonList(new PisPayment());
    }
}
