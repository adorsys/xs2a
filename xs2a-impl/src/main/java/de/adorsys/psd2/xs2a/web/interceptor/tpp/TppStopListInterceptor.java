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

package de.adorsys.psd2.xs2a.web.interceptor.tpp;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CERTIFICATE_BLOCKED;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;

@Slf4j
@RequiredArgsConstructor
public class TppStopListInterceptor extends HandlerInterceptorAdapter {
    private static final String STOP_LIST_ERROR_MESSAGE = "Signature/corporate seal certificate has been blocked by the ASPSP";

    private final ErrorMapperContainer errorMapperContainer;
    private final TppService tppService;
    private final TppStopListService tppStopListService;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;
    private final ObjectMapper objectMapper;
    private final RequestProviderService requestProviderService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        TppInfo tppInfo = tppService.getTppInfo();

        if (tppStopListService.checkIfTppBlocked(tppInfo.getAuthorisationNumber())) {
            response.getWriter().write(objectMapper.writeValueAsString(createError()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(CERTIFICATE_BLOCKED.getCode());

            log.info("X-Request-ID: [{}]. TPP {}.", requestProviderService.getRequestId(), STOP_LIST_ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private Object createError() {
        MessageError messageError = new MessageError(errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), CERTIFICATE_BLOCKED.getCode()), buildErrorTppMessages());
        return Optional.ofNullable(errorMapperContainer.getErrorBody(messageError))
                   .map(ErrorMapperContainer.ErrorBody::getBody)
                   .orElse(null);
    }

    private TppMessageInformation buildErrorTppMessages() {
        return of(CERTIFICATE_BLOCKED, STOP_LIST_ERROR_MESSAGE);
    }
}
