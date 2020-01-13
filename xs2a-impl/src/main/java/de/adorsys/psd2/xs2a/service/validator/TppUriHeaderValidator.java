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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppDomainValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TppUriHeaderValidator implements BusinessValidator<TppRedirectUri> {
    private final TppDomainValidator tppDomainValidator;
    private final ScaApproachResolver scaApproachResolver;

    @Override
    public @NotNull ValidationResult validate(@NotNull TppRedirectUri tppRedirectUri) {
        return ValidationResult.valid();
    }

    @Override
    public @NotNull Set<TppMessageInformation> buildWarningMessages(@NotNull TppRedirectUri tppRedirectUri) {
        Set<TppMessageInformation> warnings = new HashSet<>();

        if (isRedirectScaApproach()) {
            warnings.addAll(tppDomainValidator.buildWarningMessages(tppRedirectUri.getUri()));
            if (StringUtils.isNotBlank(tppRedirectUri.getNokUri())) {
                warnings.addAll(tppDomainValidator.buildWarningMessages(tppRedirectUri.getNokUri()));
            }
        }

        return warnings;
    }

    private boolean isRedirectScaApproach() {
        return ScaApproach.REDIRECT == scaApproachResolver.resolveScaApproach();
    }
}
