package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInformationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadSinglePaymentServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String END_TO_END_IDENTIFICATION = "PAYMENT_ID";
    private static final String CREDITOR_AGENT = "AAAADEBBXXX";
    private static final String CREDITOR_NAME = "WBG";
    private static final String REMITTANCE_INFORMATION_UNSTRUCTURED = "Ref Number Merchant";
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.now();
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.now();
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final List<PisPayment> PIS_PAYMENTS = getListPisPayment();
    private static final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private static final SinglePayment SINGLE_PAYMENT = buildSinglePayment();
    private static final String SOME_ENCRYPTED_PAYMENT_ID = "Encrypted Payment Id";

    @InjectMocks
    private ReadSinglePaymentService readSinglePaymentService;

    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aUpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;
    @Mock
    private SinglePaymentSpi singlePaymentSpi;
    @Mock
    private SpiToXs2aSinglePaymentMapper spiToXs2aSinglePaymentMapper;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiPaymentFactory spiPaymentFactory;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    @Before
    public void init() {
        when(spiPaymentFactory.createSpiSinglePayment(PIS_PAYMENTS.get(0), PRODUCT)).thenReturn(Optional.of(SPI_SINGLE_PAYMENT));
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(singlePaymentSpi.getPaymentById(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiSinglePayment>builder()
                .payload(SPI_SINGLE_PAYMENT)
                .success());
        when(spiToXs2aSinglePaymentMapper.mapToXs2aSinglePayment(SPI_SINGLE_PAYMENT)).thenReturn(SINGLE_PAYMENT);
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(anyString()))
            .thenReturn(spiAspspConsentDataProvider);
    }

    @Test
    public void getPayment_success() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus("SOME_ASPSP_CONSENT_DATA.getConsentId()", SINGLE_PAYMENT.getTransactionStatus()))
            .thenReturn(true);

        //When
        PaymentInformationResponse<SinglePayment> actualResponse = readSinglePaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(SINGLE_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    public void getPayment_updatePaymentStatusAfterSpiService_updatePaymentStatus_failed() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(SOME_ENCRYPTED_PAYMENT_ID, SINGLE_PAYMENT.getTransactionStatus()))
            .thenReturn(false);

        //When
        PaymentInformationResponse<SinglePayment> actualResponse = readSinglePaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID);

        //Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse.getPayment()).isNotNull();
        assertThat(actualResponse.getPayment()).isEqualTo(SINGLE_PAYMENT);
        assertThat(actualResponse.getErrorHolder()).isNull();
    }

    @Test
    public void getPayment_singlePaymentSpi_getPaymentById_failed() {
        //Given
        SpiResponse<SpiSinglePayment> spiResponseError = SpiResponse.<SpiSinglePayment>builder()
            .fail(SpiResponseStatus.LOGICAL_FAILURE);

        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR)
            .messages(Collections.singletonList("Payment is finalised already, so its status cannot be changed"))
            .build();

        when(singlePaymentSpi.getPaymentById(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider)).thenReturn(spiResponseError);
        when(spiErrorMapper.mapToErrorHolder(spiResponseError, ServiceType.PIS)).thenReturn(expectedError);

        //When
        PaymentInformationResponse<SinglePayment> actualResponse = readSinglePaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    public void getPayment_spiPaymentFactory_createSpiSinglePayment_failed() {
        //Given
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
            .messages(Collections.singletonList("Payment not found"))
            .build();

        when(spiPaymentFactory.createSpiSinglePayment(PIS_PAYMENTS.get(0), PRODUCT)).thenReturn(Optional.empty());

        //When
        PaymentInformationResponse<SinglePayment> actualResponse = readSinglePaymentService.getPayment(PIS_PAYMENTS, PRODUCT, PSU_DATA, SOME_ENCRYPTED_PAYMENT_ID);

        //Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getPayment()).isNull();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType"),
            new TppInfo(),
            UUID.randomUUID()
        );
    }

    private static List<PisPayment> getListPisPayment() {
        return Collections.singletonList(new PisPayment());
    }

    private static SinglePayment buildSinglePayment() {
        SinglePayment singlePayment = new SinglePayment();
        singlePayment.setPaymentId(PAYMENT_ID);
        singlePayment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        singlePayment.setCreditorAgent(CREDITOR_AGENT);
        singlePayment.setCreditorName(CREDITOR_NAME);
        singlePayment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTURED);
        singlePayment.setTransactionStatus(TRANSACTION_STATUS);
        singlePayment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        singlePayment.setRequestedExecutionTime(REQUESTED_EXECUTION_TIME);
        return singlePayment;
    }
}
