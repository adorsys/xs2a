package de.adorsys.psd2.xs2a.service.payment.sca;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.pis.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class OauthScaPaymentServiceTest {
    private static final ScaApproach SCA_APPROACH = ScaApproach.OAUTH;
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final SinglePayment SINGLE_PAYMENT = new SinglePayment();
    private static final PeriodicPayment PERIODIC_PAYMENT = new PeriodicPayment();
    private static final BulkPayment BULK_PAYMENT = buildBulkPayment(SINGLE_PAYMENT);
    private static final CommonPayment COMMON_PAYMENT = buildCommonPayment();

    @InjectMocks
    private OauthScaPaymentService oauthScaPaymentService;

    @Test(expected = UnsupportedOperationException.class)
    public void createSinglePayment() {
        //When
        SinglePaymentInitiationResponse actualResponse = oauthScaPaymentService.createSinglePayment(SINGLE_PAYMENT, TPP_INFO, PRODUCT, PSU_DATA);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createPeriodicPayment() {
        //When
        PeriodicPaymentInitiationResponse actualResponse = oauthScaPaymentService.createPeriodicPayment(PERIODIC_PAYMENT, TPP_INFO, PRODUCT, PSU_DATA);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createBulkPayment() {
        //When
        BulkPaymentInitiationResponse actualResponse = oauthScaPaymentService.createBulkPayment(BULK_PAYMENT, TPP_INFO, PRODUCT, PSU_DATA);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createCommonPayment() {
        //When
        CommonPaymentInitiationResponse actualResponse = oauthScaPaymentService.createCommonPayment(COMMON_PAYMENT, TPP_INFO, PRODUCT, PSU_DATA);
    }

    @Test
    public void getScaApproachServiceType() {
        //When
        ScaApproach actualResponse = oauthScaPaymentService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static BulkPayment buildBulkPayment(SinglePayment singlePayment) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setPayments(Collections.singletonList(singlePayment));
        bulkPayment.setRequestedExecutionDate(LocalDate.now());
        bulkPayment.setBatchBookingPreferred(false);
        return bulkPayment;
    }

    private static CommonPayment buildCommonPayment() {
        CommonPayment request = new CommonPayment();
        request.setPaymentType(PaymentType.SINGLE);
        request.setPaymentProduct("sepa-credit-transfers");
        request.setPaymentData(new byte[16]);
        request.setTppInfo(TPP_INFO);
        return request;
    }
}
