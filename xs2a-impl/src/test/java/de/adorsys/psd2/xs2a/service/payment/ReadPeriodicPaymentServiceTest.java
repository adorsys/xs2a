package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadPeriodicPaymentServiceTest {

    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null);
    private final List<PisPayment> PIS_PAYMETNS = getListPisPayment();
    private final PisPayment PIS_PAYMENT = getPisPayment();
    private final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();
    private final SpiPeriodicPayment SPI_PERIODIC_PAYMENT = new SpiPeriodicPayment(PRODUCT);
    private final PeriodicPayment PERIODIC_PAYMENT = new PeriodicPayment();
    private final PaymentInformationResponse PAYMENT_INFORMATION_REPSONSE = getPaymentInformationResponse();
    private final PisPaymentInfo PIS_PAYMENT_INFO = getPisPaymentInfo();

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
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiPaymentFactory spiPaymentFactory;

    @Before
    public void init() {
        when(spiPaymentFactory.createSpiPeriodicPayment(PIS_PAYMETNS.get(0), PRODUCT)).thenReturn(Optional.of(SPI_PERIODIC_PAYMENT));
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(periodicPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, SPI_PERIODIC_PAYMENT, SOME_ASPSP_CONSENT_DATA))
            .thenReturn(SpiResponse.<SpiPeriodicPayment>builder()
                .aspspConsentData(SOME_ASPSP_CONSENT_DATA.respondWith("some data".getBytes()))
                .payload(SPI_PERIODIC_PAYMENT)
                .success());
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(SOME_ASPSP_CONSENT_DATA.getConsentId(), PERIODIC_PAYMENT.getTransactionStatus()))
            .thenReturn(true);
    }

    @Test
    public void getPayment() {
        //when
        PaymentInformationResponse<PeriodicPayment> actualRespone = readPeriodicPaymentService.getPayment(PIS_PAYMETNS, PRODUCT, PSU_DATA, SOME_ASPSP_CONSENT_DATA);

        //then
        assertThat(actualRespone.hasError()).isFalse();
    }

    private SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData("", "", "", ""),
            new TppInfo()
        );
    }

    private PisPayment getPisPayment() {
        return new PisPayment();
    }

    private List<PisPayment> getListPisPayment() {
        List<PisPayment> list = new ArrayList<PisPayment>();
        PisPayment pisPayment = new PisPayment();
        list.add(pisPayment);
        return list;
    }

    private PaymentInformationResponse getPaymentInformationResponse() {
        return new PaymentInformationResponse<>(
            ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
                .messages(Collections.singletonList("Payment is finalised already, so its status cannot be changed"))
                .build()
        );
    }

    private PisPaymentInfo getPisPaymentInfo() {
        return new PisPaymentInfo();
    }
}
