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

package de.adorsys.aspsp.xs2a.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.validator.RequestValidatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static java.util.Locale.forLanguageTag;

@Slf4j
@Component
public class HandlerInterceptor extends HandlerInterceptorAdapter {
    private final RequestValidatorService requestValidatorService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Autowired
    public HandlerInterceptor(RequestValidatorService requestValidatorService, ObjectMapper objectMapper, MessageSource messageSource) {
        this.requestValidatorService = requestValidatorService;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        return isRequestValidAndSendRespIfError(request, response, handler);
    }

    private boolean isRequestValidAndSendRespIfError(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Map<String, String> violationsMap = requestValidatorService.getRequestViolationMap(request, handler);

        if (violationsMap.isEmpty()) {
            return true;
        } else {

            Map.Entry<String, String> firstError = violationsMap.entrySet().iterator().next();
            MessageErrorCode messageCode = getActualMessageErrorCode(firstError.getKey());

            log.debug("Handled error {}", messageCode.name() + ": " + firstError.getValue());
            response.resetBuffer();
            response.setStatus(messageCode.getCode());
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/json");
            response.getWriter().write(objectMapper.writeValueAsString(getMessageError(messageCode)));
            response.flushBuffer();
            return false;
        }
    }

    private MessageErrorCode getActualMessageErrorCode(String error) {
        return MessageErrorCode.getByName(error)
                   .orElse(MessageErrorCode.FORMAT_ERROR);
    }

    private MessageError getMessageError(MessageErrorCode errorCode) {
        String message = messageSource.getMessage(errorCode.name(), null, forLanguageTag("en"));
        TppMessageInformation messageInformation = new TppMessageInformation(ERROR, errorCode);
        messageInformation.setText(message);
        return new MessageError(Xs2aTransactionStatus.RJCT, messageInformation);
    }
}
