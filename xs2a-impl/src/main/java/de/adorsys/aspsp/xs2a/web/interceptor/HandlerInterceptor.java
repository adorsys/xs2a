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

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.service.validator.RequestValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

@Component
public class HandlerInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerInterceptor.class);
    private final RequestValidatorService requestValidatorService;

    @Autowired
    public HandlerInterceptor(RequestValidatorService requestValidatorService) {
        this.requestValidatorService = requestValidatorService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return isRequestValidAndSendRespIfError(request, response, handler);
    }

    private boolean isRequestValidAndSendRespIfError(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String, String> violationsMap = requestValidatorService.getRequestViolationMap(request, handler);

        if (violationsMap.isEmpty()) {
            return true;
        } else {
            MessageErrorCode errorCode = getActualMessageCode(violationsMap.keySet());

            List<String> violations = violationsMap.entrySet().stream()
                                                .map(entry -> entry.getKey() + " : " + entry.getValue())
                                                .collect(Collectors.toList());

            LOGGER.debug(violations.toString());
            response.sendError(errorCode.getCode(), errorCode.name() + ": " + violations.toString());
            return false;
        }
    }

    private MessageErrorCode getActualMessageCode(Set<String> errors) {
        List<MessageErrorCode> actualMessages = Arrays.stream(MessageErrorCode.values())
                                               .filter(mess -> errors.contains(mess.getName()))
                                               .collect(Collectors.toList());

        return actualMessages.isEmpty()
                   ? FORMAT_ERROR
                   : actualMessages.get(0); // TODO make prioritize for getting message code;
    }
}
