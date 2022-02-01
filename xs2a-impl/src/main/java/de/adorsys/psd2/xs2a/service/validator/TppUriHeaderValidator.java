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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppDomainValidator;
import de.adorsys.psd2.xs2a.web.validator.header.ConsentHeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.header.PaymentHeaderValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_NOK_REDIRECT_URI;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_URI;

@Component
@RequiredArgsConstructor
public class TppUriHeaderValidator implements BusinessValidator<TppRedirectUri>, PaymentHeaderValidator, ConsentHeaderValidator {
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

    @Override
    public MessageError validate(Map<String, String> headers, MessageError messageError) {
        ValidationResult uriValidationResult = tppDomainValidator.validate(headers.get(TPP_REDIRECT_URI));
        if (uriValidationResult.isNotValid()) {
            return uriValidationResult.getMessageError();
        }

        ValidationResult nokUriValidationResult = tppDomainValidator.validate(headers.get(TPP_NOK_REDIRECT_URI));
        if (nokUriValidationResult.isNotValid()) {
            return nokUriValidationResult.getMessageError();
        }

        return messageError;
    }
}
