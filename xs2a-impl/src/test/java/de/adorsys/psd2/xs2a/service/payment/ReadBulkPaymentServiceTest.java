package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBulkPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ReadBulkPaymentServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private final static UUID X_REQUEST_ID = UUID.randomUUID();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final List<PisPayment> PIS_PAYMENTS = getListPisPayment();
    private static final BulkPayment BULK_PAYMENT = new BulkPayment();
    private static final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();
    private static final SpiBulkPayment SPI_BULK_PAYMENT = new SpiBulkPayment();
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[16], "some consent id");
    private static final SpiResponse<SpiBulkPayment> BULK_PAYMENT_SPI_RESPONSE = buildSpiResponse();

    @InjectMocks
    private ReadBulkPaymentService readBulkPaymentService;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aUpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;
    @Mock
    private BulkPaymentSpi bulkPaymentSpi;
    @Mock
    private SpiToXs2aBulkPaymentMapper spiToXs2aBulkPaymentMapper;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiPaymentFactory spiPaymentFactory;
    @Mock
    private RequestProviderService requestProviderService;


    @Before
    public void init() {
        when(spiPaymentFactory.createSpiBulkPayment(PIS_PAYMENTS, PRODUCT))
            .thenReturn(Optional.of(SPI_BULK_PAYMENT));
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(bulkPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, SPI_BULK_PAYMENT, ASPSP_CONSENT_DATA))
            .thenReturn(BULK_PAYMENT_SPI_RESPONSE);
        when(spiToXs2aBulkPaymentMapper.mapToXs2aBulkPayment(SPI_BULK_PAYMENT))
            .thenReturn(BULK_PAYMENT);
    }

    @Test
    public void getPayment_success() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(ASPSP_CONSENT_DATA.getConsentId(), BULK_PAYMENT.getTransactionStatus()))
            .thenReturn(true);

        //When
        PaymentInformationResponse<BulkPayment> actualResponse = readBulkPaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, ASPSP_CONSENT_DATA);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(BULK_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    public void getPayment_updatePaymentStatusAfterSpiService_updatePaymentStatus_failed() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(ASPSP_CONSENT_DATA.getConsentId(), BULK_PAYMENT.getTransactionStatus()))
            .thenReturn(false);
        when(requestProviderService.getRequestId()).thenReturn(X_REQUEST_ID);

        //When
        PaymentInformationResponse<BulkPayment> actualResponse = readBulkPaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, ASPSP_CONSENT_DATA);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(BULK_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    public void getPayment_bulkPaymentSpi_getPaymentById_failed() {
        //Given
        SpiResponse<SpiBulkPayment> spiResponseError = SpiResponse.<SpiBulkPayment>builder()
                                                           .aspspConsentData(ASPSP_CONSENT_DATA)
                                                           .fail(SpiResponseStatus.LOGICAL_FAILURE);
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
                                        .messages(Collections.singletonList("Payment not found"))
                                        .build();

        when(bulkPaymentSpi.getPaymentById(SPI_CONTEXT_DATA, SPI_BULK_PAYMENT, ASPSP_CONSENT_DATA))
            .thenReturn(spiResponseError);
        when(spiErrorMapper.mapToErrorHolder(spiResponseError, ServiceType.PIS))
            .thenReturn(expectedError);

        //When
        PaymentInformationResponse<BulkPayment> actualResponse = readBulkPaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, ASPSP_CONSENT_DATA);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    public void getPayment_spiPaymentFactory_createSpiBulkPayment_failed() {
        //Given
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
                                        .messages(Collections.singletonList("Payment not found"))
                                        .build();

        when(spiPaymentFactory.createSpiBulkPayment(PIS_PAYMENTS, PRODUCT))
            .thenReturn(Optional.empty());

        //When
        PaymentInformationResponse<BulkPayment> actualResponse = readBulkPaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, ASPSP_CONSENT_DATA);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType"),
            new TppInfo(),
            X_REQUEST_ID
        );
    }

    private static List<PisPayment> getListPisPayment() {
        return Collections.singletonList(new PisPayment());
    }

    private static SpiResponse<SpiBulkPayment> buildSpiResponse() {
        return SpiResponse.<SpiBulkPayment>builder()
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .payload(SPI_BULK_PAYMENT)
                   .success();
    }
}
