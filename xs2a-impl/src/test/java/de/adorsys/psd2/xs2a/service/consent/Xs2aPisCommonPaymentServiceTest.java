package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisCommonPaymentRequestMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aPisCommonPaymentServiceTest {

    private final TppInfo TPP_INFO = buildTppInfo();
    private final PaymentInitiationParameters PAYMENT_INITIATION_PARAMETERS = buildPaymentInitiationParameters();
    private final PisPaymentInfo PIS_PAYMENT_INFO = buildPisPaymentInfo(PAYMENT_DATA);
    private final PisPaymentInfo PIS_PAYMENT_INFO_NULL = buildPisPaymentInfo(null);
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null);
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final byte[] PAYMENT_DATA = new byte[16];
    private final CreatePisCommonPaymentResponse CREATE_PIS_COMMON_PAYMENT_RESPONSE = new CreatePisCommonPaymentResponse("");

    @InjectMocks
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @Mock
    private Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;


    @Test
    public void createCommonPayment_by_parameters_tppInfo_success() {
        //given
        when(pisCommonPaymentServiceEncrypted.createCommonPayment(PIS_PAYMENT_INFO_NULL))
            .thenReturn(Optional.of(CREATE_PIS_COMMON_PAYMENT_RESPONSE));

        //when
        CreatePisCommonPaymentResponse actualResponse = xs2aPisCommonPaymentService.createCommonPayment(PAYMENT_INITIATION_PARAMETERS, TPP_INFO);

        //then
        assertThat(actualResponse).isEqualTo(CREATE_PIS_COMMON_PAYMENT_RESPONSE);
    }

    @Test
    public void createCommonPayment_by_parameters_tppInfo_failed() {
        //given
        when(pisCommonPaymentServiceEncrypted.createCommonPayment(PIS_PAYMENT_INFO_NULL))
            .thenReturn(Optional.empty());

        //when
        CreatePisCommonPaymentResponse actualResponse = xs2aPisCommonPaymentService.createCommonPayment(PAYMENT_INITIATION_PARAMETERS, TPP_INFO);

        //then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void createCommonPayment_by_request_success() {
        //given
        when(pisCommonPaymentServiceEncrypted.createCommonPayment(PIS_PAYMENT_INFO))
            .thenReturn(Optional.of(CREATE_PIS_COMMON_PAYMENT_RESPONSE));

        //when
        CreatePisCommonPaymentResponse actualResponse = xs2aPisCommonPaymentService.createCommonPayment(PIS_PAYMENT_INFO);

        //then
        assertThat(actualResponse).isEqualTo(CREATE_PIS_COMMON_PAYMENT_RESPONSE);
    }

    @Test
    public void createCommonPayment_by_request_failed() {
        //given
        when(pisCommonPaymentServiceEncrypted.createCommonPayment(PIS_PAYMENT_INFO))
            .thenReturn(Optional.empty());

        //when
        CreatePisCommonPaymentResponse actualResponse = xs2aPisCommonPaymentService.createCommonPayment(PIS_PAYMENT_INFO);

        //then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void createCommonPayment_by_parameters_tppInfo_paymentData_success() {
        //given
        when(pisCommonPaymentServiceEncrypted.createCommonPayment(PIS_PAYMENT_INFO))
            .thenReturn(Optional.of(CREATE_PIS_COMMON_PAYMENT_RESPONSE));

        //when
        CreatePisCommonPaymentResponse actualResponse = xs2aPisCommonPaymentService.createCommonPayment(PAYMENT_INITIATION_PARAMETERS, TPP_INFO, PAYMENT_DATA);

        //then
        assertThat(actualResponse).isEqualTo(CREATE_PIS_COMMON_PAYMENT_RESPONSE);
    }

    @Test
    public void getPisCommonPaymentById() {
    }

    @Test
    public void updateSinglePaymentInCommonPayment() {
    }

    @Test
    public void updatePeriodicPaymentInCommonPayment() {
    }

    @Test
    public void updateBulkPaymentInCommonPayment() {
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private PisPaymentInfo buildPisPaymentInfo(byte[] paymentData) {
        PisPaymentInfo request = new PisPaymentInfo();
        request.setPaymentProduct(PAYMENT_INITIATION_PARAMETERS.getPaymentProduct());
        request.setPaymentType(PAYMENT_INITIATION_PARAMETERS.getPaymentType());
        request.setTransactionStatus(TransactionStatus.RCVD);
        request.setPaymentData(paymentData);
        request.setTppInfo(TPP_INFO);
        request.setPsuDataList(Collections.singletonList(PAYMENT_INITIATION_PARAMETERS.getPsuData()));
        return request;
    }

    private PaymentInitiationParameters buildPaymentInitiationParameters() {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentProduct(PRODUCT);
        parameters.setPaymentType(PaymentType.SINGLE);
        parameters.setPsuData(PSU_DATA);
        return parameters;
    }
}
