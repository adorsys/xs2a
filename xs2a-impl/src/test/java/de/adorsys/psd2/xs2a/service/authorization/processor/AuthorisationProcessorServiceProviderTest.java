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
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorisationProcessorServiceProviderTest {
    @InjectMocks
    private AuthorisationProcessorServiceProvider provider;
    @Mock
    private ApplicationContext applicationContext;
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
        request = new AisAuthorisationProcessorRequest(ScaApproach.EMBEDDED, null, null, null);
    }

    @Test
    void getProcessorServiceException() {
        // Given
        request.setServiceType(ServiceType.PIS);
        Authorisation authorisation = new Authorisation();
        request.setAuthorisation(authorisation);

        // When
        assertThrows(IllegalArgumentException.class, () -> provider.getProcessorService(request));

        // Then
        verify(applicationContext, never()).getBean(anyString());
    }

    @Test
    void getProcessorServiceAis() {
        // Given
        when(applicationContext.getBean(AisAuthorisationProcessorServiceImpl.class)).thenReturn(aisAuthorisationProcessorServiceImpl);

        // When
        AuthorisationProcessorService actual = provider.getProcessorService(request);

        // Then
        assertThat(actual).isEqualTo(aisAuthorisationProcessorServiceImpl);
    }

    @Test
    void getProcessorServicePiis() {
        // Given
        request.setServiceType(ServiceType.PIIS);
        when(applicationContext.getBean(PiisAuthorisationProcessorServiceImpl.class)).thenReturn(piisAuthorisationProcessorService);

        // When
        AuthorisationProcessorService actual = provider.getProcessorService(request);

        // Then
        assertThat(actual).isEqualTo(piisAuthorisationProcessorService);
    }

    @Test
    void getProcessorServicePisCreation() {
        // Given
        request.setServiceType(ServiceType.PIS);
        Authorisation authorisation = new Authorisation();
        authorisation.setAuthorisationType(AuthorisationType.PIS_CREATION);
        request.setAuthorisation(authorisation);
        when(applicationContext.getBean(PisAuthorisationProcessorServiceImpl.class))
            .thenReturn(pisAuthorisationProcessorService);

        // When
        AuthorisationProcessorService actual = provider.getProcessorService(request);

        // Then
        assertThat(actual).isEqualTo(pisAuthorisationProcessorService);
    }

    @Test
    void getProcessorServicePisCancellation() {
        // Given
        request.setServiceType(ServiceType.PIS);
        Authorisation authorisation = new Authorisation();
        authorisation.setAuthorisationType(AuthorisationType.PIS_CANCELLATION);
        request.setAuthorisation(authorisation);
        when(applicationContext.getBean(PisCancellationAuthorisationProcessorServiceImpl.class))
            .thenReturn(pisCancellationAuthorisationProcessorServiceImpl);

        // When
        AuthorisationProcessorService actual = provider.getProcessorService(request);

        // Then
        assertThat(actual).isEqualTo(pisCancellationAuthorisationProcessorServiceImpl);
    }
}
