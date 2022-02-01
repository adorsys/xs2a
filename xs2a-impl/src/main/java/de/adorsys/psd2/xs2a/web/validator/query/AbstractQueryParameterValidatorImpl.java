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

package de.adorsys.psd2.xs2a.web.validator.query;


import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    protected String getQueryParameterValue(Map<String, List<String>> queryParameterMap, String parameter) {
        return Optional.of(parameter)
                   .map(queryParameterMap::get)
                   .orElseGet(ArrayList::new)
                   .stream()
                   .findFirst()
                   .orElse(null);

    }

    /**
     * Validates date parameter format if it exists. Based on `ISO_DATE` format (i.e. 2021-02-12)
     *
     * @param dateParam input date parameter
     * @return TRUE if date parameter has correct format and FALSE otherwise
     */
    protected boolean isDateParamValid(String dateParam) {
        if (StringUtils.isEmpty(dateParam)) {
            return false;
        }
        try {
            LocalDate.parse(dateParam, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
