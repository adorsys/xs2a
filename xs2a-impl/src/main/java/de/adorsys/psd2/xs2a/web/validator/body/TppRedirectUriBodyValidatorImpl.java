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

package de.adorsys.psd2.xs2a.web.validator.body;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_URI;

@Component
@RequiredArgsConstructor
public class TppRedirectUriBodyValidatorImpl implements BodyValidator {

    private final ScaApproachResolver scaApproachResolver;
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final ErrorBuildingService errorBuildingService;

    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        if (isRedirectScaApproach()) {
            String tppRedirectUriHeader = request.getHeader(TPP_REDIRECT_URI);
            Map<String, String> headers = Collections.list(request.getHeaderNames())
                                              .stream()
                                              .collect(Collectors.toMap(String::toLowerCase, request::getHeader));

            if (!headers.containsKey(TPP_REDIRECT_URI)) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_ABSENT_HEADER, TPP_REDIRECT_URI));
            } else if (tppRedirectUriHeader == null) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_HEADER, TPP_REDIRECT_URI));
            } else if (StringUtils.isBlank(tppRedirectUriHeader)) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_BLANK_HEADER, TPP_REDIRECT_URI));
            }
        }

        return messageError;
    }

    private boolean isRedirectScaApproach() {
        ScaApproach scaApproach = scaApproachResolver.resolveScaApproach();
        return ScaApproach.REDIRECT == scaApproach && aspspProfileServiceWrapper.getScaRedirectFlow().equals(ScaRedirectFlow.REDIRECT);
    }
}
