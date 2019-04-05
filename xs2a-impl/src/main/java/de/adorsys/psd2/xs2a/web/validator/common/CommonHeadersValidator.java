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

package de.adorsys.psd2.xs2a.web.validator.common;

import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.AbstractHeadersValidator;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.HeadersValidator;
import de.adorsys.psd2.xs2a.web.validator.common.service.XRequestIdValidationService;
import de.adorsys.psd2.xs2a.web.validator.methods.SpecificMethodsHeadersValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.X_REQUEST_ID;

/**
 * This class is used for validation of common headers, that are used in all controllers' methods.
 */
@Component
@RequiredArgsConstructor
public class CommonHeadersValidator extends AbstractHeadersValidator {

    private final XRequestIdValidationService xRequestIdValidationService;
    private final SpecificMethodsHeadersValidator specificMethodsHeadersValidator;
    private final ErrorBuildingService errorBuildingService;

    @Override
    protected boolean validateThis(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        String xRequestId = request.getHeader(X_REQUEST_ID);

        ValidationResult xRequestIdValidationResult = xRequestIdValidationService.validateXRequestId(xRequestId);

        if (xRequestIdValidationResult.isNotValid()) {
            errorBuildingService.buildErrorResponse(response, xRequestIdValidationResult.getMessageError());
            return false;
        }

        return true;
    }

    @Override
    protected HeadersValidator getNextValidator() {
        return specificMethodsHeadersValidator;
    }

}
