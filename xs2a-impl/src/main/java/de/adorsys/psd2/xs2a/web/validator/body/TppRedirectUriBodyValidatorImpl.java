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

package de.adorsys.psd2.xs2a.web.validator.body;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_URI;

@Component
@RequiredArgsConstructor
public class TppRedirectUriBodyValidatorImpl implements BodyValidator {
    static final String ERROR_TEXT_ABSENT_HEADER = "Header '%s' is missing in request";
    static final String ERROR_TEXT_NULL_HEADER = "Header '%s' should not be null";
    static final String ERROR_TEXT_BLANK_HEADER = "Header '%s' should not be blank";

    private final ScaApproachResolver scaApproachResolver;
    private final ErrorBuildingService errorBuildingService;

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        if (isRedirectScaApproach()) {
            String tppRedirectUriHeader = request.getHeader(TPP_REDIRECT_URI);
            Map<String, String> headers = Collections.list(request.getHeaderNames())
                                              .stream()
                                              .collect(Collectors.toMap(String::toLowerCase, request::getHeader));

            if (!headers.containsKey(TPP_REDIRECT_URI)) {
                errorBuildingService.enrichMessageError(messageError, String.format(ERROR_TEXT_ABSENT_HEADER, TPP_REDIRECT_URI));
            } else if (Objects.isNull(tppRedirectUriHeader)) {
                errorBuildingService.enrichMessageError(messageError, String.format(ERROR_TEXT_NULL_HEADER, TPP_REDIRECT_URI));
            } else if (StringUtils.isBlank(tppRedirectUriHeader)) {
                errorBuildingService.enrichMessageError(messageError, String.format(ERROR_TEXT_BLANK_HEADER, TPP_REDIRECT_URI));
            }
        }
    }

    private boolean isRedirectScaApproach() {
        ScaApproach scaApproach = scaApproachResolver.resolveScaApproach();
        return ScaApproach.REDIRECT == scaApproach;
    }
}
