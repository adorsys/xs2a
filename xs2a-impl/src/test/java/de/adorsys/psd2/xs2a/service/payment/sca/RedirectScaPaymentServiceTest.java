package de.adorsys.psd2.xs2a.service.payment.sca;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class RedirectScaPaymentServiceTest {
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;

    @InjectMocks
    private RedirectScaPaymentService redirectScaPaymentService;

    @Test
    public void getScaApproachServiceType() {
        //when
        ScaApproach actualResponse = redirectScaPaymentService.getScaApproachServiceType();

        //then
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }
}
