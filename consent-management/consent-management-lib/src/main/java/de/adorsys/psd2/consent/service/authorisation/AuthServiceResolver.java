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
            case CONSENT:
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
