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

package de.adorsys.psd2.xs2a.service.authorization.processor;

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.AuthorisationProcessorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExemptedAuthorisationProcessorTest {

    @InjectMocks
    private ExemptedAuthorisationProcessor processor;

    @Mock
    private AuthorisationProcessorService processorService;

    @Test
    void scaStatus() {
        assertEquals(ScaStatus.EXEMPTED, processor.getScaStatus());
    }

    @Test
    void execute() {
        AisAuthorisationProcessorRequest request = new AisAuthorisationProcessorRequest(null, null, null, null);
        when(processorService.doScaExempted(request)).thenReturn(new AuthorisationProcessorResponse());

        processor.execute(request, processorService);

        verify(processorService, times(1)).doScaExempted(request);
    }
}
