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
        assertEquals(aisAuthService.getClass(), authServiceResolver.getAuthService(AuthorisationType.AIS).getClass());
        assertEquals(pisAuthService.getClass(), authServiceResolver.getAuthService(AuthorisationType.PIS_CREATION).getClass());
        assertEquals(pisCancellationAuthService.getClass(), authServiceResolver.getAuthService(AuthorisationType.PIS_CANCELLATION).getClass());
    }
}
