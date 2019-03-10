package de.adorsys.psd2.xs2a.service.payment.sca;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class EmbeddedScaPaymentServiceTest {
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;

    @InjectMocks
    private EmbeddedScaPaymentService embeddedScaPaymentService;

    @Test
    public void getScaApproachServiceType() {
        //When
        ScaApproach actualResponse = embeddedScaPaymentService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }
}
