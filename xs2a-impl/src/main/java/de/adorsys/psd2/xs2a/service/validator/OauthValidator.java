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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public abstract class OauthValidator<T> {
    private final RequestProviderService requestProviderService;
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final ScaApproachResolver scaApproachResolver;

    public @NotNull ValidationResult validate(@NotNull T object) {

        if (isOauthIntegrated() && checkObjectForTokenAbsence(object) && isTokenAbsent()) {
            return ValidationResult.invalid(getMessageError());
        }

        return ValidationResult.valid();
    }

    protected abstract boolean checkObjectForTokenAbsence(T object);

    protected abstract MessageError getMessageError();

    private boolean isOauthIntegrated() {
        return scaApproachResolver.resolveScaApproach() == ScaApproach.REDIRECT
                   && aspspProfileServiceWrapper.getScaRedirectFlow() == ScaRedirectFlow.OAUTH;
    }

    private boolean isTokenAbsent() {
        return StringUtils.isBlank(requestProviderService.getOAuth2Token());
    }
}
