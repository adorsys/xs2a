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

package de.adorsys.psd2.xs2a.web.error;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.web.filter.TppErrorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TppErrorMessageWriter {
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ErrorMapperContainer errorMapperContainer;
    private final Xs2aObjectMapper xs2aObjectMapper;

    public void writeError(HttpServletResponse response, TppErrorMessage tppErrorMessage) throws IOException {
        MessageErrorCode messageErrorCode = tppErrorMessage.getCode();
        MessageError messageError = new MessageError(getErrorType(messageErrorCode.getCode()), TppMessageInformation.of(tppErrorMessage.getCategory(), messageErrorCode, tppErrorMessage.getTextParams()));
        writeMessageError(response, messageError);
    }

    public void writeError(HttpServletResponse response, MessageError messageError) throws IOException {
        writeMessageError(response, messageError);
    }

    private void writeMessageError(HttpServletResponse response, MessageError messageError) throws IOException {
        response.setStatus(messageError.getErrorType().getErrorCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        xs2aObjectMapper.writeValue(response.getWriter(), errorMapperContainer.getErrorBody(messageError).getBody());
    }

    public void writeServiceUnavailableError(HttpServletResponse response, String message) {
        try {
            log.warn("ResourceAccessException handled with message: {}", message);
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            xs2aObjectMapper.writeValue(response.getWriter(), new ServiceUnavailableError());
        } catch (IOException e) {
            log.info(" Writing to the httpServletResponse failed.");
        }
    }

    private ErrorType getErrorType(int errorCode) {
        return ErrorType.getByServiceTypeAndErrorCode(serviceTypeDiscoveryService.getServiceType(), errorCode)
                   .orElseThrow(() -> new IllegalArgumentException("ErrorCode is not correct for given service type."));
    }
}
