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

package de.adorsys.psd2.xs2a.web.interceptor.tpp;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.model.TppMessageGeneric;
import de.adorsys.psd2.model.TppMessages;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppUniqueParamsHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.TppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class TppStopListInterceptor extends HandlerInterceptorAdapter {
    private static final String STOP_LIST_ERROR_MESSAGE = "Signature/corporate seal certificate has been blocked by the ASPSP";

    private final TppService tppService;
    private final TppStopListService tppStopListService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        TppInfo tppInfo = tppService.getTppInfo();

        if (tppStopListService.checkIfTppBlocked(new TppUniqueParamsHolder(tppInfo))) {
            response.getWriter().write(objectMapper.writeValueAsString(buildErrorTppMessages()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(MessageErrorCode.CERTIFICATE_BLOCKED.getCode());
            return false;
        }

        return true;
    }

    private TppMessages buildErrorTppMessages() {
        TppMessageGeneric errorMessage = new TppMessageGeneric()
                                             .category(TppMessageCategory.ERROR)
                                             .code(MessageErrorCode.CERTIFICATE_BLOCKED)
                                             .text(STOP_LIST_ERROR_MESSAGE);

        TppMessages errorMessages = new TppMessages();
        errorMessages.add(errorMessage);

        return errorMessages;
    }
}
