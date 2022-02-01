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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AuthServiceResolverTest {

    @InjectMocks
    private AuthServiceResolver authServiceResolver;

    @Mock
    private AisAuthService aisAuthService;
    @Mock
    private PisAuthService pisAuthService;
    @Mock
    private PisCancellationAuthService pisCancellationAuthService;

    @Test
    void getAuthService() {
        assertEquals(aisAuthService.getClass(), authServiceResolver.getAuthService(AuthorisationType.CONSENT).getClass());
        assertEquals(pisAuthService.getClass(), authServiceResolver.getAuthService(AuthorisationType.PIS_CREATION).getClass());
        assertEquals(pisCancellationAuthService.getClass(), authServiceResolver.getAuthService(AuthorisationType.PIS_CANCELLATION).getClass());
    }
}
