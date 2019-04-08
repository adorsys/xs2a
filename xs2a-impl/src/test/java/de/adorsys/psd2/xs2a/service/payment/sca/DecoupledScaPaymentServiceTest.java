package de.adorsys.psd2.xs2a.service.payment.sca;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class DecoupledScaPaymentServiceTest {
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;

    @InjectMocks
    private DecoupledScaPaymentService decoupledScaPaymentService;

    @Test
    public void getScaApproachServiceType() {
        //When
        ScaApproach actualResponse = decoupledScaPaymentService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }
}
