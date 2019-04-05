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

package de.adorsys.psd2.xs2a.web.validator.methods.factory;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.methods.MethodHeadersValidator;
import de.adorsys.psd2.xs2a.web.validator.methods.service.TppRedirectUriValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_PREFERRED;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_URI;

@RequiredArgsConstructor
@Service("_createConsent")
public class CreateConsentValidator implements MethodHeadersValidator {

    private final ErrorBuildingService errorBuildingService;
    private final TppRedirectUriValidationService tppRedirectUriValidationService;

    @Override
    public boolean validate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ValidationResult creteConsentValidationResult = validateCreateConsent(request);

        if (creteConsentValidationResult.isNotValid()) {
            errorBuildingService.buildErrorResponse(response, creteConsentValidationResult.getMessageError());
            return false;
        }

        return true;
    }

    private ValidationResult validateCreateConsent(HttpServletRequest request) {
        boolean tppRedirectPreferred = Boolean.parseBoolean(request.getHeader(TPP_REDIRECT_PREFERRED));
        String tppRedirectUri = request.getHeader(TPP_REDIRECT_URI);

        if (tppRedirectUriValidationService.isNotValid(tppRedirectPreferred, tppRedirectUri)) {
            return ValidationResult.invalid(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR, TPP_REDIRECT_URI + " is not correct or empty"));
        }

        return ValidationResult.valid();
    }
}
