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
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceResolver {
    private final AisAuthService aisAuthService;
    private final PisAuthService pisAuthService;
    private final PisCancellationAuthService pisCancellationAuthService;

    @NotNull
    public AuthService getAuthService(AuthorisationType authorisationType) {
        switch (authorisationType) {
            case AIS:
                return aisAuthService;
            case PIS_CREATION:
                return pisAuthService;
            case PIS_CANCELLATION:
                return pisCancellationAuthService;
            default:
                throw new IllegalArgumentException("Unknown authorisation type: " + authorisationType);
        }
    }
}
