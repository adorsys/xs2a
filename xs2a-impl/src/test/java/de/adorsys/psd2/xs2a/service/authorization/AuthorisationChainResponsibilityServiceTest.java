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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.AisAuthorisationProcessorServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationChainResponsibilityServiceTest {

    @InjectMocks
    private AuthorisationChainResponsibilityService service;

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private AisAuthorisationProcessorServiceImpl aisAuthorisationProcessorServiceImpl;

    @Test
    public void apply() {
        AccountConsentAuthorization authorization = new AccountConsentAuthorization();
        AisAuthorisationProcessorRequest request = new AisAuthorisationProcessorRequest(ScaApproach.EMBEDDED,
                                                                                        ScaStatus.RECEIVED,
                                                                                        new Xs2aUpdatePisCommonPaymentPsuDataRequest(),
                                                                                        authorization);
        when(applicationContext.getBean(AisAuthorisationProcessorServiceImpl.class)).thenReturn(aisAuthorisationProcessorServiceImpl);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();
        when(aisAuthorisationProcessorServiceImpl.doScaReceived(request)).thenReturn(processorResponse);
        doNothing().when(aisAuthorisationProcessorServiceImpl).updateAuthorisation(request, processorResponse);

        service.apply(request);

        verify(applicationContext, times(2)).getBean(AisAuthorisationProcessorServiceImpl.class);
        verify(aisAuthorisationProcessorServiceImpl, times(1)).doScaReceived(request);
        verify(aisAuthorisationProcessorServiceImpl, times(1)).updateAuthorisation(request, processorResponse);
    }
}
