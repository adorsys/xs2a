/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
