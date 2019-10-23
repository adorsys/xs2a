/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FinalisedAuthorisationProcessorTest {

    @InjectMocks
    private FinalisedAuthorisationProcessor processor;

    @Mock
    private AuthorisationProcessorService processorService;

    @Test
    public void scaStatus() {
        assertEquals(ScaStatus.FINALISED, processor.getScaStatus());
    }

    @Test
    public void execute() {
        AisAuthorisationProcessorRequest request = new AisAuthorisationProcessorRequest(null, null, null, null);
        when(processorService.doScaFinalised(request)).thenReturn(new AuthorisationProcessorResponse());

        processor.execute(request, processorService);

        verify(processorService, times(1)).doScaFinalised(request);
    }
}
