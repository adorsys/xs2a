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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.exception.MessageError;
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
public abstract class AbstractMethodValidator<H extends HeaderValidator, B extends BodyValidator, Q extends QueryParameterValidator, P extends PathParameterValidator> implements MethodValidator {
    @Getter(AccessLevel.PACKAGE)
    private final List<H> headerValidators;
    @Getter(AccessLevel.PACKAGE)
    private final List<B> bodyValidators;
    @Getter(AccessLevel.PACKAGE)
    private final List<Q> queryParameterValidators;
    @Getter(AccessLevel.PACKAGE)
    private final List<P> pathParameterValidators;

    /**
     * Common validator which validates request headers and body
     *
     * @param request      {@link javax.servlet.http.HttpServletRequest}
     * @param messageError is populated by errors during validation
     */
    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        Map<String, String> headers = Collections.list(request.getHeaderNames())
                                          .stream()
                                          .collect(Collectors.toMap(h -> h, request::getHeader));

        // This is implemented for handling the headers in case insensitive mode.
        TreeMap<String, String> caseInsensitiveHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveHeaders.putAll(headers);

        Map<String, List<String>> queryParameters = extractQueryParameters(request);
        Map<String, String> pathParameters = extractPathParameters(request);

        getPathParameterValidators().forEach(v -> v.validate(pathParameters, messageError));
        getQueryParameterValidators().forEach(v -> v.validate(queryParameters, messageError));
        getHeaderValidators().forEach(v -> v.validate(caseInsensitiveHeaders, messageError));
        getBodyValidators().forEach(v -> v.validate(request, messageError));
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
