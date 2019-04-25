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

package de.adorsys.psd2.xs2a.web.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

@Component
@RequiredArgsConstructor
public class ErrorBuildingService {

    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;
    private final ErrorMapperContainer errorMapperContainer;
    private final ObjectMapper objectMapper;

    public void buildErrorResponse(HttpServletResponse response, MessageError messageError) throws IOException {
        response.resetBuffer();
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Set<TppMessageInformation> tppMessages = messageError.getTppMessages();

        response.setStatus(tppMessages.iterator().next().getMessageErrorCode().getCode());
        response.getWriter().write(objectMapper.writeValueAsString(createError(tppMessages)));

        response.flushBuffer();
    }

    public void enrichMessageError(MessageError messageError, String errorMessage) {
        enrichMessageError(messageError, new MessageError(buildErrorType(), TppMessageInformation.of(FORMAT_ERROR, errorMessage)));
    }

    public void enrichMessageError(MessageError messageError, MessageError validationMessageError) {
        enrichMessageError(messageError, validationMessageError.getTppMessage());
    }

    public void enrichMessageError(MessageError messageError, TppMessageInformation tppMessageInformation) {
        messageError.addTppMessage(tppMessageInformation);
    }

    public ErrorType buildErrorType() {
        return errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), FORMAT_ERROR.getCode());
    }

    private Object createError(Set<TppMessageInformation> tppMessageInformations) {
        List<String> errorMessages = new ArrayList<>();
        tppMessageInformations.forEach(tmi -> errorMessages.add(tmi.getText()));

        MessageError messageError = getMessageError(errorMessages);
        return Optional.ofNullable(errorMapperContainer.getErrorBody(messageError))
                   .map(ErrorMapperContainer.ErrorBody::getBody)
                   .orElse(null);
    }

    private MessageError getMessageError(Collection<String> errorMessages) {
        ErrorType errorType = errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), FORMAT_ERROR.getCode());

        TppMessageInformation[] tppMessages = errorMessages.stream()
                                                  .map(e -> TppMessageInformation.of(FORMAT_ERROR, e))
                                                  .toArray(TppMessageInformation[]::new);

        return new MessageError(errorType, tppMessages);
    }

}
