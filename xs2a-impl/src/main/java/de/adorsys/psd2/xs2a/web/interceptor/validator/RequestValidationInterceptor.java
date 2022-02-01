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

package de.adorsys.psd2.xs2a.web.interceptor.validator;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.MethodValidator;
import de.adorsys.psd2.xs2a.web.validator.MethodValidatorController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This interceptor is used for headers, query and path parameters and body validation of incoming HTTP requests. The
 * main purposes: collect the list of human-readable errors in case of using wrong headers, parameters or JSON fields
 * (ex, IBAN has wrong format, some mandatory header is missing etc). Each error in the incoming request (validated in
 * accordance with the PSD2 documentation) results in one text message in the response.
 * <p>
 * Please note, this interceptor can only return the response with the HTTP code 400 (FORMAT ERROR).
 * <p>
 * No business validation is present here, as business validation requires some specific options to process (ex, SCA approach,
 * bank-profile configuration). It is implemented in the controllers and services layers.
 */
@Component
@Slf4j
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

            MethodValidator methodValidator = methodValidatorController.getMethod(methodName);
            initialMessageError = methodValidator.validate(request, initialMessageError);

            if (!initialMessageError.getTppMessages().isEmpty()) {
                // Last part of all validations: if there is at least one error - we build response with HTTP code 400.
                log.warn("Validation of incoming request failed. Error msg: [{}]", initialMessageError);
                errorBuildingService.buildFormatErrorResponse(response, initialMessageError);
                return false;
            }
        }
        return true;
    }

}
