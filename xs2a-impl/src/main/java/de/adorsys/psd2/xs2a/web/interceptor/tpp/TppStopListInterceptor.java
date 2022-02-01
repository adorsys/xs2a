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

package de.adorsys.psd2.xs2a.web.interceptor.tpp;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CERTIFICATE_BLOCKED;

@Slf4j
@RequiredArgsConstructor
public class TppStopListInterceptor extends HandlerInterceptorAdapter {
    private static final String STOP_LIST_ERROR_MESSAGE = "Signature/corporate seal certificate has been blocked by the ASPSP";
    private static final String INSTANCE_ID = "Instance-ID";

    private final ErrorMapperContainer errorMapperContainer;
    private final TppService tppService;
    private final TppStopListService tppStopListService;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;
    private final Xs2aObjectMapper xs2aObjectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        TppInfo tppInfo = tppService.getTppInfo();
        CmsResponse<Boolean> cmsResponse = tppStopListService.checkIfTppBlocked(tppInfo.getAuthorisationNumber(),
                                                                                request.getHeader(INSTANCE_ID));

        if (cmsResponse.isSuccessful() && BooleanUtils.isTrue(cmsResponse.getPayload())) {
            response.getWriter().write(xs2aObjectMapper.writeValueAsString(createError()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(CERTIFICATE_BLOCKED.getCode());

            log.info("TPP {}.", STOP_LIST_ERROR_MESSAGE);
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
        return of(CERTIFICATE_BLOCKED);
    }
}
