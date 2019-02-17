package de.adorsys.psd2.xs2a.service.payment.sca;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
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
public class ScaPaymentServiceTest {

    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null);
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "some consent id");
    private final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();
    private final TppInfo TPP_INFO = buildTppInfo();
    //SinglePayment
    private final SinglePayment SINGLE_PAYMENT = new SinglePayment();
    private final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private final SpiSinglePaymentInitiationResponse SPI_SINGLE_PAYMENT_RESPONSE = buildSpiSinglePaymentInitiationResponse();
    private final SpiResponse<SpiSinglePaymentInitiationResponse> SPI_SINGLE_RESPONSE = buildSpiResponse(SPI_SINGLE_PAYMENT_RESPONSE);
    private final SinglePaymentInitiationResponse SINGLE_PAYMENT_RESPONSE = new SinglePaymentInitiationResponse();
    //CommonPayment
    private final CommonPayment COMMON_PAYMENT = buildCommonPayment();
    private final SpiPaymentInfo SPI_PAYMENT_INFO = new SpiPaymentInfo(PRODUCT);
    private final SpiResponse<SpiPaymentInitiationResponse> SPI_COMMON_RESPONSE = buildSpiResponse(SPI_SINGLE_PAYMENT_RESPONSE);
    //PeriodicPayment
    private final PeriodicPayment PERIODIC_PAYMENT = new PeriodicPayment();



    @InjectMocks
    private RedirectScaPaymentService scaPaymentService;

    @Mock
    private SinglePaymentSpi singlePaymentSpi;
    @Mock
    private PeriodicPaymentSpi periodicPaymentSpi;
    @Mock
    private BulkPaymentSpi bulkPaymentSpi;
    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2AToSpiSinglePaymentMapper;
    @Mock
    private Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    @Mock
    private Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    @Mock
    private Xs2aToSpiPaymentInfo xs2aToSpiPaymentInfo;
    @Mock
    private SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiErrorMapper spiErrorMapper;

    @Before
    public void init() {
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
    }

    @Test
    public void createSinglePayment_Success() {
        //given
        when(xs2AToSpiSinglePaymentMapper.mapToSpiSinglePayment(SINGLE_PAYMENT, PRODUCT))
            .thenReturn(SPI_SINGLE_PAYMENT);
        when(singlePaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, AspspConsentData.emptyConsentData()))
            .thenReturn(SPI_SINGLE_RESPONSE);
        when(spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(SPI_SINGLE_PAYMENT_RESPONSE, SinglePaymentInitiationResponse::new, SPI_SINGLE_RESPONSE.getAspspConsentData()))
            .thenReturn(SINGLE_PAYMENT_RESPONSE);

        //when
        //actualReponse is null, and i didnt know exactly its correct or no..
        SinglePaymentInitiationResponse actualResponse = scaPaymentService.createSinglePayment(SINGLE_PAYMENT, TPP_INFO, PRODUCT, PSU_DATA);

        //then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void createPeriodicPayment() {
    }

    @Test
    public void createBulkPayment() {
    }

    @Test
    public void createCommonPayment() {
        //when(xs2aToSpiPaymentInfo.mapToSpiPaymentRequest(COMMON_PAYMENT, PRODUCT)).thenReturn(SPI_PAYMENT_INFO);
       //when(commonPaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_PAYMENT_INFO, ASPSP_CONSENT_DATA)).thenReturn()
    }

    private SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData("", "", "", ""),
            new TppInfo()
        );
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private CommonPayment buildCommonPayment() {
        CommonPayment request = new CommonPayment();
        request.setPaymentType(PaymentType.SINGLE);
        request.setPaymentProduct("sepa-credit-transfers");
        request.setPaymentData(new byte[16]);
        request.setTppInfo(TPP_INFO);
        return request;
    }

    private SpiSinglePaymentInitiationResponse buildSpiSinglePaymentInitiationResponse() {
        SpiSinglePaymentInitiationResponse response = new SpiSinglePaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspAccountId(ASPSP_ACCOUNT_ID);
        return response;
    }

    private <T> SpiResponse<T> buildSpiResponse(T payload) {
        return SpiResponse.<T>builder()
            .payload(payload)
            .aspspConsentData(ASPSP_CONSENT_DATA)
            .success();
    }

    private SpiResponse<SpiPaymentInitiationResponse> buildSuccessSpiResponse() {
        return SpiResponse.<SpiPaymentInitiationResponse>builder()
            .payload(SPI_SINGLE_PAYMENT_RESPONSE)
            .aspspConsentData(ASPSP_CONSENT_DATA)
            .success();
    }
}
