/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.interceptor;

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.MethodValidator;
import de.adorsys.psd2.xs2a.web.validator.MethodValidatorController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * This interceptor is used for headers and body validation of incoming HTTP requests. The main purposes: collect the
 * list of human-readable errors in case of using wrong headers or JSON fields (ex, IBAN has wrong format, some mandatory
 * header is missing etc). Each error in the incoming request (validated in accordance with the PSD2 documentation)
 * results in one text message in the response.
 * <p>
 * Please note, this interceptor can only return the response with the HTTP code 400 (FORMAT ERROR).
 * <p>
 * No business validation is present here, as business validation requires some specific options to process (ex, SCA approach,
 * bank-profile configuration). It is implemented in the controllers and services layers.
 */
@Component
@RequiredArgsConstructor
public class RequestValidationInterceptor extends HandlerInterceptorAdapter {

    private final ErrorBuildingService errorBuildingService;
    private final MethodValidatorController methodValidatorController;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        return isRequestValid(request, response, handler);
    }

    private boolean isRequestValid(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        // This MessageError instance may be enriched in all chains of validation (headers and body) for all methods.
        MessageError initialMessageError = new MessageError();

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String methodName = handlerMethod.getMethod().getName();

            Optional<MethodValidator> methodValidator = methodValidatorController.getMethod(methodName);
            if (methodValidator.isPresent()) {
                // TODO: think about changing the chain of void methods: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/813
                methodValidator.get().validate(request, initialMessageError);

                if (!initialMessageError.getTppMessages().isEmpty()) {
                    // Last part of all validations: if there is at least one error - we build response with HTTP code 400.
                    errorBuildingService.buildErrorResponse(response, initialMessageError);
                    return false;
                }
            } else {
                return true;
            }
        }
        return true;
    }

}
