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

package de.adorsys.psd2.xs2a.service.validator.ais.account.common;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.REQUESTED_FORMATS_INVALID;
import static java.util.stream.Collectors.toSet;

/**
 * Validator to be used for validating if accept header is supported by ASPSP
 * - If accept header is not presented in request validator will not validate header;
 * - If property "supportedTransactionApplicationTypes" was not configured validator will not validate header;
 * - Otherwise header should be presented in supported headers in bank profile
 */
@Component
@RequiredArgsConstructor
public class TransactionReportAcceptHeaderValidator {
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    public ValidationResult validate(String acceptHeader) {
        if (StringUtils.isNotBlank(acceptHeader)) {
            List<String> supportedTransactionApplicationTypes = aspspProfileServiceWrapper.getSupportedTransactionApplicationTypes();

            if (!isAtLeastOneAcceptHeaderSupported(supportedTransactionApplicationTypes, acceptHeader)) {
                return ValidationResult.invalid(ErrorType.AIS_406, REQUESTED_FORMATS_INVALID);
            }
        }

        return ValidationResult.valid();
    }

    private boolean isAtLeastOneAcceptHeaderSupported(List<String> supportedHeaders, String acceptHeader) {
        List<String> acceptHeaders = Stream.of(acceptHeader.split(","))
                                         .collect(Collectors.toList());
        return CollectionUtils.isEmpty(supportedHeaders) ||
                   supportedHeaders.stream()
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
