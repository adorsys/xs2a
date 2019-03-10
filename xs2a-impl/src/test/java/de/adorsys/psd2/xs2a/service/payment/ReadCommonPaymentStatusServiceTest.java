package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.ReadPaymentStatusResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentInfoMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadCommonPaymentStatusServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[16], "some consent id");
    private static final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();
    private static final CommonPayment COMMON_PAYMENT = new CommonPayment();
    private static final SpiPaymentInfo SPI_PAYMENT_INFO = new SpiPaymentInfo(PRODUCT);
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.ACSP;
    private static final SpiResponse<TransactionStatus> TRANSACTION_RESPONSE = buildSpiResponseTransactionStatus();
    private static final SpiResponse<TransactionStatus> TRANSACTION_RESPONSE_FAILURE = buildFailSpiResponseTransactionStatus();
    private static final ReadPaymentStatusResponse READ_PAYMENT_STATUS_RESPONSE = new ReadPaymentStatusResponse(TRANSACTION_RESPONSE.getPayload());
    private static final PisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = new PisCommonPaymentResponse();

    @InjectMocks
    private ReadCommonPaymentStatusService readCommonPaymentStatusService;
    @Mock
    private PisAspspDataService pisAspspDataService;
    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    @Mock
    private CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;

    @Before
    public void init(){
        when(cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(PIS_COMMON_PAYMENT_RESPONSE))
            .thenReturn(COMMON_PAYMENT);
        when(xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(COMMON_PAYMENT))
            .thenReturn(SPI_PAYMENT_INFO);
    }

    @Test
    public void readPaymentStatus_success() {
        //Given
        when(commonPaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, SPI_PAYMENT_INFO, ASPSP_CONSENT_DATA))
            .thenReturn(TRANSACTION_RESPONSE);

        //When
        ReadPaymentStatusResponse actualResponse = readCommonPaymentStatusService.readPaymentStatus(PIS_COMMON_PAYMENT_RESPONSE, SPI_CONTEXT_DATA, ASPSP_CONSENT_DATA);

        //Then
        assertThat(actualResponse).isEqualTo(READ_PAYMENT_STATUS_RESPONSE);
    }

    @Test
    public void readPaymentStatus_failed() {
        //Given
        ErrorHolder expectedError = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
            .messages(Collections.singletonList("Payment not found"))
            .build();

        when(commonPaymentSpi.getPaymentStatusById(SPI_CONTEXT_DATA, SPI_PAYMENT_INFO, ASPSP_CONSENT_DATA))
            .thenReturn(TRANSACTION_RESPONSE_FAILURE);
        when(spiErrorMapper.mapToErrorHolder(TRANSACTION_RESPONSE_FAILURE, ServiceType.PIS))
            .thenReturn(expectedError);

        //When
        ReadPaymentStatusResponse actualResponse = readCommonPaymentStatusService.readPaymentStatus(PIS_COMMON_PAYMENT_RESPONSE, SPI_CONTEXT_DATA, ASPSP_CONSENT_DATA);

        //Then
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
}
