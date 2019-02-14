package de.adorsys.psd2.xs2a.service.authorization.ais.stage.decoupled;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SEPARATOR;
import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AisDecoupledScaIdentifiedAuthorisationStageTest {
    @InjectMocks
    AisDecoupledScaIdentifiedAuthorisationStage aisDecoupledScaIdentifiedAuthorisationStage;

    @Mock
    private UpdateConsentPsuDataReq request;
    @Mock
    private AisScaStageAuthorisationFactory scaStageAuthorisationFactory;
    @Mock
    private AisDecoupledScaStartAuthorisationStage aisScaStage;

    @Test
    public void apply_ShouldExecuteStartedStage() {
        when(scaStageAuthorisationFactory.getService(anyString()))
            .thenReturn(aisScaStage);

        aisDecoupledScaIdentifiedAuthorisationStage.apply(request);

        verify(scaStageAuthorisationFactory).getService(SERVICE_PREFIX + SEPARATOR + ScaApproach.DECOUPLED + SEPARATOR + ScaStatus.STARTED.name());
        verify(aisScaStage).apply(request);
    }
}
