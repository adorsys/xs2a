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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.service.authorization.processor.AuthorisationProcessorServiceProvider;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.AisAuthorisationProcessorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorisationChainResponsibilityServiceTest {

    @InjectMocks
    private AuthorisationChainResponsibilityService service;

    @Mock
    private AuthorisationProcessorServiceProvider provider;
    @Mock
    private AisAuthorisationProcessorServiceImpl aisAuthorisationProcessorServiceImpl;

    @Test
    void apply() {
        Authorisation authorisation = new Authorisation();
        AisAuthorisationProcessorRequest request = new AisAuthorisationProcessorRequest(ScaApproach.EMBEDDED,
                                                                                        ScaStatus.RECEIVED,
                                                                                        new PaymentAuthorisationParameters(),
                                                                                        authorisation);
        when(provider.getProcessorService(request)).thenReturn(aisAuthorisationProcessorServiceImpl);
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();
        when(aisAuthorisationProcessorServiceImpl.doScaReceived(request)).thenReturn(processorResponse);
        doNothing().when(aisAuthorisationProcessorServiceImpl).updateAuthorisation(request, processorResponse);

        service.apply(request);

        verify(provider, times(2)).getProcessorService(request);
        verify(aisAuthorisationProcessorServiceImpl, times(1)).doScaReceived(request);
        verify(aisAuthorisationProcessorServiceImpl, times(1)).updateAuthorisation(request, processorResponse);
    }
}
