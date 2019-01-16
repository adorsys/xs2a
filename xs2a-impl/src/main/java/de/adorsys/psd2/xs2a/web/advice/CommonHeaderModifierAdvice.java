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

package de.adorsys.psd2.xs2a.web.advice;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Optional;

@RequiredArgsConstructor
@ControllerAdvice(basePackages = "de.adorsys.psd2.xs2a.web.controller")
public class CommonHeaderModifierAdvice implements ResponseBodyAdvice<Object> {
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        response.getHeaders().add("X-Request-Id", request.getHeaders().getFirst("X-Request-Id"));
        return body;
    }

    protected ScaApproach getScaApproach() {
        ScaApproach scaApproach = aspspProfileServiceWrapper.getScaApproach();
        if (scaApproach == ScaApproach.OAUTH) {
            scaApproach = ScaApproach.REDIRECT;
        }
        return scaApproach;
    }

    protected boolean hasError(Object body, Class expectedClass) {
        return Optional.ofNullable(body).isPresent() && !body.getClass()
                                                             .isAssignableFrom(expectedClass);
    }
}
