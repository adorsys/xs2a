package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ReadSinglePaymentStatusServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[16], "some consent id");
    private static final List<PisPayment> PIS_PAYMENTS = getListPisPayment();
    private static final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.ACSP;
    private static final SpiResponse<TransactionStatus> TRANSACTION_STATUS_SPI_RESPONSE = buildSpiResponseTransactionStatus();
    private static final SpiResponse<TransactionStatus> TRANSACTION_STATUS_SPI_RESPONSE_FAILURE = buildFailSpiResponseTransactionStatus();
    private static final ReadPaymentStatusResponse READ_PAYMENT_STATUS_RESPONSE = new ReadPaymentStatusResponse(TRANSACTION_STATUS_SPI_RESPONSE.getPayload());

    @InjectMocks
    private ReadSinglePaymentStatusService readSinglePaymentStatusService;

    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private SpiPaymentFactory spiPaymentFactory;
    @Mock
    private SinglePaymentSpi singlePaymentSpi;
    @Mock
    private SpiErrorMapper spiErrorMapper;

    @Test
    public void readPaymentStatus_success() {
        //given
        when(spiPaymentFactory.createSpiSinglePayment(PIS_PAYMENTS.get(0), PRODUCT))
            .thenReturn(Optional.of(SPI_SINGLE_PAYMENT));
        when(singlePaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, ASPSP_CONSENT_DATA))
            .thenReturn(TRANSACTION_STATUS_SPI_RESPONSE);

        //when
        ReadPaymentStatusResponse actualResponse = readSinglePaymentStatusService.readPaymentStatus(PIS_PAYMENTS, PRODUCT, SPI_CONTEXT_DATA, ASPSP_CONSENT_DATA);

        //then
        assertThat(actualResponse).isEqualTo(READ_PAYMENT_STATUS_RESPONSE);
    }

    @Test
    public void readPaymentStatus_spiPaymentFactory_createSpiSinglePayment_failed() {
        //given
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
            .messages(Collections.singletonList("Payment not found"))
            .build();

        when(spiPaymentFactory.createSpiSinglePayment(PIS_PAYMENTS.get(0), PRODUCT))
            .thenReturn(Optional.empty());

        //when
        ReadPaymentStatusResponse actualResponse = readSinglePaymentStatusService.readPaymentStatus(PIS_PAYMENTS, PRODUCT, SPI_CONTEXT_DATA, ASPSP_CONSENT_DATA);

        //then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    @Test
    public void readPaymentStatus_singlePaymentSpi_getPaymentStatusById_failed() {
        //given
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
            .messages(Collections.singletonList("Payment not found"))
            .build();

        when(spiPaymentFactory.createSpiSinglePayment(PIS_PAYMENTS.get(0), PRODUCT))
            .thenReturn(Optional.of(SPI_SINGLE_PAYMENT));
        when(singlePaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, ASPSP_CONSENT_DATA))
            .thenReturn(TRANSACTION_STATUS_SPI_RESPONSE_FAILURE);
        when(spiErrorMapper.mapToErrorHolder(TRANSACTION_STATUS_SPI_RESPONSE_FAILURE, ServiceType.PIS))
            .thenReturn(expectedError);

        //when
        ReadPaymentStatusResponse actualResponse = readSinglePaymentStatusService.readPaymentStatus(PIS_PAYMENTS, PRODUCT, SPI_CONTEXT_DATA, ASPSP_CONSENT_DATA);

        //then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(expectedError);
    }

    private static SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType"),
            new TppInfo()
        );
    }

    private static SpiResponse<TransactionStatus> buildSpiResponseTransactionStatus() {
        return SpiResponse.<TransactionStatus>builder()
            .aspspConsentData(ASPSP_CONSENT_DATA)
            .payload(TRANSACTION_STATUS)
            .success();
    }

    private static SpiResponse<TransactionStatus> buildFailSpiResponseTransactionStatus() {
        return SpiResponse.<TransactionStatus>builder()
            .aspspConsentData(ASPSP_CONSENT_DATA)
            .fail(SpiResponseStatus.LOGICAL_FAILURE);
    }

    private static List<PisPayment> getListPisPayment() {
        return Collections.singletonList(new PisPayment());
    }
}
