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

import de.adorsys.psd2.xs2a.service.TppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class SigningBasketLoggingInterceptor extends HandlerInterceptorAdapter {
    private final TppService tppService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ipAddress = request.getHeader("PSU-IP-Address");
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        log.info("Request: TPP ID - {}, IP - {}, X-Request-ID - {}, URI - {}, Basket ID - {}",
            tppService.getTppId(),
            StringUtils.isNotBlank(ipAddress) ? ipAddress : request.getRemoteAddr(),
            request.getHeader("X-Request-ID"),
            request.getRequestURI(),
            pathVariables.getOrDefault("basketId", "Not exist in URI")
        );

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("Response: TPP ID - {}, X-Request-ID - {}, Status - {}",
            tppService.getTppId(),
            response.getHeader("X-Request-ID"),
            response.getStatus()
        );
    }
}
