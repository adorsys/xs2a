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
