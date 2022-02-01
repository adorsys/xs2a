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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.body.BodyValidator;
import de.adorsys.psd2.xs2a.web.validator.header.HeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.path.PathParameterValidator;
import de.adorsys.psd2.xs2a.web.validator.query.QueryParameterValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractMethodValidator implements MethodValidator {

    @Getter(AccessLevel.PACKAGE)
    private final ValidatorWrapper validatorWrapper;

    /**
     * Common method which processes all stuff by validators provided in {@link ValidatorWrapper} class.
     *
     * @param request      {@link HttpServletRequest}
     * @param messageError object to be populated by errors ({@link TppMessageInformation}
     *                     objects) during validation
     * @return {@link MessageError} object, enriched or not.
     */
    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        Map<String, String> headers = Collections.list(request.getHeaderNames())
                                          .stream()
                                          .collect(Collectors.toMap(h -> h, request::getHeader));

        // This is implemented for handling the headers in case insensitive mode.
        TreeMap<String, String> caseInsensitiveHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveHeaders.putAll(headers);

        MessageError resultingMessageError = messageError;

        Map<String, List<String>> queryParameters = extractQueryParameters(request);
        Map<String, String> pathParameters = extractPathParameters(request);

        for (PathParameterValidator pathParameterValidator : validatorWrapper.getPathParameterValidators()) {
            resultingMessageError = pathParameterValidator.validate(pathParameters, resultingMessageError);
        }

        for (QueryParameterValidator queryParameterValidator : validatorWrapper.getQueryParameterValidators()) {
            resultingMessageError = queryParameterValidator.validate(queryParameters, resultingMessageError);
        }

        for (HeaderValidator headerValidator : validatorWrapper.getHeaderValidators()) {
            resultingMessageError = headerValidator.validate(caseInsensitiveHeaders, resultingMessageError);
        }

        for (BodyValidator bodyValidator : validatorWrapper.getBodyValidators()) {
            resultingMessageError = bodyValidator.validate(request, resultingMessageError);
        }

        return resultingMessageError;
    }

    private Map<String, String> extractPathParameters(HttpServletRequest request) {
        return (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    private Map<String, List<String>> extractQueryParameters(HttpServletRequest request) {
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        return requestParameterMap.entrySet()
                   .stream()
                   .collect(Collectors.toMap(Map.Entry::getKey,
                                             e -> Arrays.asList(e.getValue())));
    }
}
