package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAuthenticationObjectToCmsScaMethodMapper;
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
public class Xs2aPisCommonPaymentServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String AUTHENTICATION_METHOD_ID = "19ff-4b5a-8188";
    private static final byte[] PAYMENT_DATA = new byte[16];
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final PaymentInitiationParameters PAYMENT_INITIATION_PARAMETERS = buildPaymentInitiationParameters();
    private static final PisPaymentInfo PIS_PAYMENT_INFO = buildPisPaymentInfo(PAYMENT_DATA);
    private static final PisPaymentInfo PIS_PAYMENT_INFO_NULL = buildPisPaymentInfo(null);
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final CreatePisCommonPaymentResponse CREATE_PIS_COMMON_PAYMENT_RESPONSE = new CreatePisCommonPaymentResponse(PAYMENT_ID);
    private static final PisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = new PisCommonPaymentResponse();
    private static final List<Xs2aAuthenticationObject> AUTHENTICATION_OBJECT_LIST = Collections.singletonList(new Xs2aAuthenticationObject());
    private static final List<CmsScaMethod> CMS_SCA_METHOD_LIST = Collections.singletonList(new CmsScaMethod(AUTHORISATION_ID, true));

    @InjectMocks
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @Mock
    private Xs2aAuthenticationObjectToCmsScaMethodMapper xs2AAuthenticationObjectToCmsScaMethodMapper;


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
    public void getPisCommonPaymentById_success() {
        //given
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        //when
        Optional<PisCommonPaymentResponse> actualResponse = xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID);

        //then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(PIS_COMMON_PAYMENT_RESPONSE);
    }

    @Test
    public void getPisCommonPaymentById_failed() {
        //given
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.empty());

        //when
        Optional<PisCommonPaymentResponse> actualResponse = xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID);

        //then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void isAuthenticationMethodDecoupled_success() {
        //given
        when(pisCommonPaymentServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(true);

        //when
        boolean actualResponse = xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        //then
        assertThat(actualResponse).isTrue();
    }

    @Test
    public void isAuthenticationMethodDecoupled_failed() {
        //given
        when(pisCommonPaymentServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(false);

        //when
        boolean actualResponse = xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        //then
        assertThat(actualResponse).isFalse();
    }

    @Test
    public void saveAuthenticationMethods_success() {
        //given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(pisCommonPaymentServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(true);

        //when
        boolean actualResponse = xs2aPisCommonPaymentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        //then
        assertThat(actualResponse).isTrue();
    }

    @Test
    public void saveAuthenticationMethods_failed() {
        //given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(pisCommonPaymentServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(false);

        //when
        boolean actualResponse = xs2aPisCommonPaymentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        //then
        assertThat(actualResponse).isFalse();
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static PisPaymentInfo buildPisPaymentInfo(byte[] paymentData) {
        PisPaymentInfo request = new PisPaymentInfo();
        request.setPaymentProduct(PAYMENT_INITIATION_PARAMETERS.getPaymentProduct());
        request.setPaymentType(PAYMENT_INITIATION_PARAMETERS.getPaymentType());
        request.setTransactionStatus(TransactionStatus.RCVD);
        request.setPaymentData(paymentData);
        request.setTppInfo(TPP_INFO);
        request.setPsuDataList(Collections.singletonList(PAYMENT_INITIATION_PARAMETERS.getPsuData()));
        return request;
    }

    private static PaymentInitiationParameters buildPaymentInitiationParameters() {
        PaymentInitiationParameters parameters = new PaymentInitiationParameters();
        parameters.setPaymentProduct(PRODUCT);
        parameters.setPaymentType(PaymentType.SINGLE);
        parameters.setPsuData(PSU_DATA);
        return parameters;
    }
}
