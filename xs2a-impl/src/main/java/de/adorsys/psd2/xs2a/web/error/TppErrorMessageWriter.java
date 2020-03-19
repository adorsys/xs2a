/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
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
