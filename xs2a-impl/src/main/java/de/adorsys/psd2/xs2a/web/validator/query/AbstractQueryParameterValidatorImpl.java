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

package de.adorsys.psd2.xs2a.web.validator.query;


import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Common validator implementation for verifying query parameters in the request
 */
@RequiredArgsConstructor
public abstract class AbstractQueryParameterValidatorImpl implements QueryParameterValidator {
    protected final ErrorBuildingService errorBuildingService;

    /**
     * Validates the presence of mandatory query parameter by checking whether:
     * <ul>
     * <li>the parameter is present in the request</li>
     * <li>the parameter's value is contained only once in the request</li>
     * <li>the parameter's value is not blank</li>
     * </ul>
     *
     * @param queryParameterMap query parameter map, with parameter names acting as keys
     * @return valid result if the parameter is present only once and doesn't have blank value,
     * validation error otherwise
     */
    protected ValidationResult validateMandatoryParameterPresence(Map<String, List<String>> queryParameterMap) {
        List<String> queryParameterValues = getQueryParameterValues(queryParameterMap);
        String queryParameterName = getQueryParameterName();

        if (queryParameterValues.isEmpty()) {
            return ValidationResult.invalid(errorBuildingService.buildErrorType(), TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_ABSENT_PARAMETER, queryParameterName));
        }

        if (hasMultipleValues(queryParameterValues)) {
            return ValidationResult.invalid(errorBuildingService.buildErrorType(), TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_INVALID_PARAMETER_VALUE, queryParameterName));
        }

        String queryParameterValue = getQueryParameterValue(queryParameterMap);
        if (StringUtils.isBlank(queryParameterValue)) {
            return ValidationResult.invalid(errorBuildingService.buildErrorType(), TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_BLANK_PARAMETER, queryParameterName));
        }

        return ValidationResult.valid();
    }

    /**
     * Returns the name of the query parameter
     *
     * @return query parameter name
     */
    protected abstract String getQueryParameterName();

    /**
     * Returns the first value of the query parameter via name from {@link #getQueryParameterName()}
     *
     * @param queryParameterMap query parameters from the request
     * @return value of the first query parameter if it was found, <code>null</code> otherwise
     */
    protected String getQueryParameterValue(Map<String, List<String>> queryParameterMap) {
        List<String> queryParameterValues = getQueryParameterValues(queryParameterMap);

        return queryParameterValues.stream()
                   .findFirst()
                   .orElse(null);
    }

    private List<String> getQueryParameterValues(Map<String, List<String>> queryParameterMap) {
        List<String> valuesList = queryParameterMap.get(getQueryParameterName());
        return Optional.ofNullable(valuesList)
                   .orElseGet(ArrayList::new);
    }

    private boolean hasMultipleValues(List<String> queryParameterValues) {
        return queryParameterValues.size() > 1;
    }
}
