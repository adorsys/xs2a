/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.authorization.ais.stage.decoupled;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SEPARATOR;
import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisDecoupledScaIdentifiedAuthorisationStageTest {
    @InjectMocks
    AisDecoupledScaIdentifiedAuthorisationStage aisDecoupledScaIdentifiedAuthorisationStage;

    @Mock
    private UpdateConsentPsuDataReq request;
    @Mock
    private AisScaStageAuthorisationFactory scaStageAuthorisationFactory;
    @Mock
    private AisDecoupledScaReceivedAuthorisationStage aisScaStage;

    @Test
    void apply_ShouldExecuteStartedStage() {
        when(scaStageAuthorisationFactory.getService(anyString()))
            .thenReturn(aisScaStage);

        aisDecoupledScaIdentifiedAuthorisationStage.apply(request);

        verify(scaStageAuthorisationFactory).getService(SERVICE_PREFIX + SEPARATOR + ScaApproach.DECOUPLED + SEPARATOR + ScaStatus.RECEIVED.name());
        verify(aisScaStage).apply(request);
    }
}
