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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.REQUESTED_FORMATS_INVALID;
import static java.util.stream.Collectors.toSet;

/**
 * Validator to be used for validating whether transaction status format requested by TPP is supported by ASPSP
 */
@Component
@RequiredArgsConstructor
public class TransactionStatusAcceptHeaderValidator {
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    /**
     * Validates whether media type in Accept header is supported by the ASPSP.
     * Will not perform any validation and will return valid if no supported transaction status formats are configured in the ASPSP profile.
     *
     * @param acceptHeader accept header with requested media types
     * @return valid result if media type is supported
     */
    public ValidationResult validate(@NotNull String acceptHeader) {
        if (acceptHeader.equalsIgnoreCase(MediaType.ALL_VALUE)) {
            return ValidationResult.valid();
        }

        List<String> supportedTransactionStatusFormats = aspspProfileServiceWrapper.getSupportedTransactionStatusFormats();
        if (!isMediaTypeSupported(acceptHeader, supportedTransactionStatusFormats)) {
            return ValidationResult.invalid(ErrorType.PIS_406, REQUESTED_FORMATS_INVALID);
        }

        return ValidationResult.valid();
    }

    private boolean isMediaTypeSupported(String requestedMediaType, List<String> supportedMediaTypes) {
        List<String> acceptHeaders = Stream.of(requestedMediaType.split(","))
                                         .collect(Collectors.toList());
        return CollectionUtils.isEmpty(supportedMediaTypes) ||
                   supportedMediaTypes.stream()
                       .filter(Objects::nonNull)
                       .map(String::toLowerCase)
                       .map(String::trim)
                       .anyMatch(
                           acceptHeaders.stream()
                               .map(String::toLowerCase)
                               .map(String::trim)
                               .collect(toSet())
                               ::contains);
    }
}
