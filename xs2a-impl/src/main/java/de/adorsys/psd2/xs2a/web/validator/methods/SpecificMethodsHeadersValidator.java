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

package de.adorsys.psd2.xs2a.web.validator.methods;

import de.adorsys.psd2.xs2a.web.validator.AbstractHeadersValidator;
import de.adorsys.psd2.xs2a.web.validator.methods.factory.HeadersValidationServiceFactory;
import de.adorsys.psd2.xs2a.web.validator.HeadersValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is used for validation of some specific controller methods (ex, payment initiation or consent creation) headers.
 */
@Component
@RequiredArgsConstructor
public class SpecificMethodsHeadersValidator extends AbstractHeadersValidator {

    private final HeadersValidationServiceFactory headersValidationServiceFactory;

    @Override
    protected boolean validateThis(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        // Services for the definite method validations are called by method name via factory pattern here. To add any new
        // validators please include the new class to the 'methods.factory' package and add new enum to MethodsForSpecificHeaderValidationAvailableMethods.

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String methodName = handlerMethod.getMethod().getName();

            List<String> availableMethods = Arrays.stream(MethodsForSpecificHeaderValidationAvailableMethods.values())
                                                .map(MethodsForSpecificHeaderValidationAvailableMethods::getValue)
                                                .collect(Collectors.toList());

            if (availableMethods.contains(methodName)) {
                MethodHeadersValidator validator = headersValidationServiceFactory.getService(methodName);
                return validator.validate(request, response);
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public HeadersValidator getNextValidator() {
        return null;
    }

    public enum MethodsForSpecificHeaderValidationAvailableMethods {

        CREATE_CONSENT ("_createConsent"),
        INITIATE_PAYMENT ("_initiatePayment");

        private String value;

        MethodsForSpecificHeaderValidationAvailableMethods(String value) {
            this.value = value;
        }

        public String getValue() {
            return String.valueOf(value);
        }
    }
}
