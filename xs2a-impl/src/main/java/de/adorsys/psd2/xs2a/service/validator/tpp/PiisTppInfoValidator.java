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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIIS_400;

@Slf4j
@Component
@RequiredArgsConstructor
public class PiisTppInfoValidator {
    static final String TPP_ERROR_MESSAGE = "TPP certificate doesn’t match the initial request";

    private final RequestProviderService requestProviderService;
    private final TppService tppService;

    public ValidationResult validateTpp(String authorisationNumber) {
        if (differsFromTppInRequest(authorisationNumber)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}]. TPP validation has failed: TPP in consent/payment doesn't match the TPP in request",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId());
            return ValidationResult.invalid(PIIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400, TPP_ERROR_MESSAGE));
        }

        return ValidationResult.valid();
    }

    private boolean differsFromTppInRequest(String tppAuthorisationNumber) {
        if (StringUtils.isBlank(tppAuthorisationNumber)) {
            return true;
        }

        TppInfo tppInRequest = tppService.getTppInfo();
        return !tppInRequest.getAuthorisationNumber().equals(tppAuthorisationNumber);
    }
}
