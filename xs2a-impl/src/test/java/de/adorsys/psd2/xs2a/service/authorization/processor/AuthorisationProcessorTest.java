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

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.AisAuthorisationProcessorServiceImpl;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.PiisAuthorisationProcessorServiceImpl;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.PisAuthorisationProcessorServiceImpl;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.PisCancellationAuthorisationProcessorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorisationProcessorTest {

    private AuthorisationProcessor authorisationProcessor;

    @Mock
    private AuthorisationProcessorServiceProvider provider;
    @Mock
    private AuthorisationProcessor nextProcessor;
    @Mock
    private AisAuthorisationProcessorServiceImpl aisAuthorisationProcessorServiceImpl;
    @Mock
    private PiisAuthorisationProcessorServiceImpl piisAuthorisationProcessorService;
    @Mock
    private PisAuthorisationProcessorServiceImpl pisAuthorisationProcessorService;
    @Mock
    private PisCancellationAuthorisationProcessorServiceImpl pisCancellationAuthorisationProcessorServiceImpl;

    private AisAuthorisationProcessorRequest request;

    @BeforeEach
    void setUp() {
        authorisationProcessor = new ReceivedAuthorisationProcessor(provider);
        authorisationProcessor.setNext(nextProcessor);

        request = new AisAuthorisationProcessorRequest(ScaApproach.EMBEDDED, null, null, null);
    }

    @Test
    void apply_currentProcessor() {
        request.setScaStatus(ScaStatus.RECEIVED);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        when(provider.getProcessorService(request)).thenReturn(aisAuthorisationProcessorServiceImpl);
        when(aisAuthorisationProcessorServiceImpl.doScaReceived(request)).thenReturn(processorResponse);
        doNothing().when(aisAuthorisationProcessorServiceImpl).updateAuthorisation(request, processorResponse);

        authorisationProcessor.apply(request);

        verify(provider, times(2)).getProcessorService(request);
        verify(aisAuthorisationProcessorServiceImpl, times(1)).doScaReceived(request);
        verify(aisAuthorisationProcessorServiceImpl, times(1)).updateAuthorisation(request, processorResponse);
    }

    @Test
    void apply_nextProcessor() {
        request.setScaStatus(ScaStatus.PSUIDENTIFIED);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        when(nextProcessor.process(request)).thenReturn(processorResponse);

        when(provider.getProcessorService(request)).thenReturn(aisAuthorisationProcessorServiceImpl);
        doNothing().when(aisAuthorisationProcessorServiceImpl).updateAuthorisation(request, processorResponse);

        authorisationProcessor.apply(request);

        verify(nextProcessor, times(1)).process(request);
        verify(provider, times(1)).getProcessorService(request);
        verify(aisAuthorisationProcessorServiceImpl, times(1)).updateAuthorisation(request, processorResponse);
    }

    @Test
    void getProcessorService_AIS() {
        request.setServiceType(ServiceType.AIS);
        when(provider.getProcessorService(request)).thenReturn(aisAuthorisationProcessorServiceImpl);

        authorisationProcessor.getProcessorService(request);

        verify(provider, times(1)).getProcessorService(request);
    }

    @Test
    void getProcessorService_PIIS() {
        request.setServiceType(ServiceType.PIIS);
        when(provider.getProcessorService(request)).thenReturn(piisAuthorisationProcessorService);

        authorisationProcessor.getProcessorService(request);

        verify(provider, times(1)).getProcessorService(request);
    }

    @Test
    void getProcessorService_PIS_initiation() {
        request.setServiceType(ServiceType.PIS);
        Authorisation authorisation = new Authorisation();
        authorisation.setAuthorisationType(AuthorisationType.PIS_CREATION);
        request.setAuthorisation(authorisation);
        when(provider.getProcessorService(request)).thenReturn(pisAuthorisationProcessorService);

        authorisationProcessor.getProcessorService(request);

        verify(provider, times(1)).getProcessorService(request);
    }

    @Test
    void getProcessorService_PIS_cancellation() {
        request.setServiceType(ServiceType.PIS);
        Authorisation authorisation = new Authorisation();
        authorisation.setAuthorisationType(AuthorisationType.PIS_CANCELLATION);
        request.setAuthorisation(authorisation);
        when(provider.getProcessorService(request)).thenReturn(pisCancellationAuthorisationProcessorServiceImpl);

        authorisationProcessor.getProcessorService(request);

        verify(provider, times(1)).getProcessorService(request);
    }

    @Test
    void getProcessorService_PIS_noPaymentAuthorisationType() {
        request.setServiceType(ServiceType.PIS);
        Authorisation authorisation = new Authorisation();
        request.setAuthorisation(authorisation);
        when(provider.getProcessorService(request)).thenThrow(new IllegalArgumentException());

        assertThrows(IllegalArgumentException.class, () -> authorisationProcessor.getProcessorService(request));

        verify(provider, times(1)).getProcessorService(request);
    }

    @Test
    void process_nextProcessorIsNotSet() {
        request.setScaStatus(ScaStatus.PSUIDENTIFIED);
        authorisationProcessor.setNext(null);

        assertNull(authorisationProcessor.process(request));
    }
}
