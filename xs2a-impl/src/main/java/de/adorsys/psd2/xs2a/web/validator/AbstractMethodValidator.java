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

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class AbstractMethodValidator<H extends HeaderValidator, B extends BodyValidator> implements MethodValidator {

    private final List<H> headerValidators;
    private final List<B> bodyValidators;

    protected AbstractMethodValidator(List<H> headerValidators, List<B> bodyValidators) {
        this.headerValidators = headerValidators;
        this.bodyValidators = bodyValidators;
    }

    List<H> getHeaderValidators() {
        return headerValidators;
    }

    List<B> getBodyValidators() {
        return bodyValidators;
    }

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

        getHeaderValidators().forEach(v -> v.validate(caseInsensitiveHeaders, messageError));
        getBodyValidators().forEach(v -> v.validate(request, messageError));
    }
}
