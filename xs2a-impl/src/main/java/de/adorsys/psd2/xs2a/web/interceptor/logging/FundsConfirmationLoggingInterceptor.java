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

package de.adorsys.psd2.xs2a.web.interceptor.logging;

import de.adorsys.psd2.xs2a.component.TppLogger;
import de.adorsys.psd2.xs2a.service.TppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Component
public class FundsConfirmationLoggingInterceptor extends HandlerInterceptorAdapter {
    private final TppService tppService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        TppLogger.logRequest()
            .withParam("TPP ID", tppService.getTppId())
            .withParam("TPP IP", request.getRemoteAddr())
            .withParam("X-Request-ID", request.getHeader("X-Request-ID"))
            .withParam("URI", request.getRequestURI())
            .perform();

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TppLogger.logResponse()
            .withParam("TPP ID", tppService.getTppId())
            .withParam("X-Request-ID", response.getHeader("X-Request-ID"))
            .withParam("Status", String.valueOf(response.getStatus()))
            .perform();
    }
}
