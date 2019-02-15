package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadPeriodicPaymentServiceTest {

    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null);
    private final List<PisPayment> PIS_PAYMENTS = getListPisPayment();
    private final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();
    private final SpiPeriodicPayment SPI_PERIODIC_PAYMENT = new SpiPeriodicPayment(PRODUCT);
    private final PeriodicPayment PERIODIC_PAYMENT = new PeriodicPayment();

    private static final AspspConsentData SOME_ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "some consent id");

    @InjectMocks
    private ReadPeriodicPaymentService readPeriodicPaymentService;

    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aUpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;
    @Mock
    private PeriodicPaymentSpi periodicPaymentSpi;
    @Mock
    private SpiToXs2aPeriodicPaymentMapper spiToXs2aPeriodicPaymentMapper;
    @Mock
    private SpiPaymentFactory spiPaymentFactory;
    @Mock
    private SpiErrorMapper spiErrorMapper;

    @Before
    public void init() {
        when(spiPaymentFactory.createSpiPeriodicPayment(PIS_PAYMENTS.get(0), PRODUCT)).thenReturn(Optional.of(SPI_PERIODIC_PAYMENT));
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(periodicPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, SPI_PERIODIC_PAYMENT, SOME_ASPSP_CONSENT_DATA))
            .thenReturn(SpiResponse.<SpiPeriodicPayment>builder()
                .aspspConsentData(SOME_ASPSP_CONSENT_DATA.respondWith("some data".getBytes()))
                .payload(SPI_PERIODIC_PAYMENT)
                .success());
        when(spiToXs2aPeriodicPaymentMapper.mapToXs2aPeriodicPayment(SPI_PERIODIC_PAYMENT)).thenReturn(PERIODIC_PAYMENT);
    }

    @Test
    public void getPayment_Success() {
        //given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(SOME_ASPSP_CONSENT_DATA.getConsentId(), PERIODIC_PAYMENT.getTransactionStatus()))
            .thenReturn(true);

        //when
        PaymentInformationResponse<PeriodicPayment> actualResponse = readPeriodicPaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, SOME_ASPSP_CONSENT_DATA);

        //then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(PERIODIC_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    public void getPayment_updatePaymentStatusAfterSpiService_updatePaymentStatus_Failed() {
        //given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(SOME_ASPSP_CONSENT_DATA.getConsentId(), PERIODIC_PAYMENT.getTransactionStatus()))
            .thenReturn(false);
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
            .messages(Collections.singletonList("Payment is finalised already, so its status cannot be changed"))
            .build();

        //when
        PaymentInformationResponse<PeriodicPayment> actualResponse = readPeriodicPaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, SOME_ASPSP_CONSENT_DATA);

        //then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    public void getPayment_spiPaymentFactory_createSpiPeriodicPayment_Failed() {
        //given
        when(spiPaymentFactory.createSpiPeriodicPayment(PIS_PAYMENTS.get(0), PRODUCT)).thenReturn(Optional.empty());
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
            .messages(Collections.singletonList("Payment not found"))
            .build();

        //when
        PaymentInformationResponse<PeriodicPayment> actualResponse = readPeriodicPaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, SOME_ASPSP_CONSENT_DATA);

        //then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    public void getPayment_periodicPaymentSpi_getPaymentById_Failed() {
        //given
        SpiResponse<SpiPeriodicPayment> spiResponseError = SpiResponse.<SpiPeriodicPayment>builder()
            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
            .fail(SpiResponseStatus.LOGICAL_FAILURE);
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
            .messages(Collections.singletonList("Payment not found"))
            .build();
        when(periodicPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, SPI_PERIODIC_PAYMENT, SOME_ASPSP_CONSENT_DATA))
            .thenReturn(spiResponseError);
        when(spiErrorMapper.mapToErrorHolder(spiResponseError, ServiceType.PIS)).thenReturn(expectedError);

        //when
        PaymentInformationResponse<PeriodicPayment> actualResponse = readPeriodicPaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, SOME_ASPSP_CONSENT_DATA);

        //then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData("", "", "", ""),
            new TppInfo()
        );
    }

    private List<PisPayment> getListPisPayment() {
        List<PisPayment> list = new ArrayList<PisPayment>();
        PisPayment pisPayment = new PisPayment();
        list.add(pisPayment);
        return list;
    }
}
