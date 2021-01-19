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
