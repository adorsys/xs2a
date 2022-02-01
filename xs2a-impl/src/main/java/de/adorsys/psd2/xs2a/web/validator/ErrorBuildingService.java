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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ErrorMessageBuilder;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;

@Component
@RequiredArgsConstructor
public class ErrorBuildingService implements ErrorMessageBuilder {

    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;
    private final ErrorMapperContainer errorMapperContainer;
    private final Xs2aObjectMapper xs2aObjectMapper;

    /**
     * Builds and inserts text representation of MessageError with HTTP code 400 into HTTP response. Used in request
     * validation chain and handles a list of errors.
     *
     * @param response     {@link javax.servlet.http.HttpServletRequest} the response to be enriched
     * @param messageError {@link MessageError} instance with text messages
     * @throws IOException in case IO operations error
     */
    public void buildFormatErrorResponse(HttpServletResponse response, MessageError messageError) throws IOException {
        response.resetBuffer();
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Set<TppMessageInformation> tppMessages = messageError.getTppMessages();

        response.setStatus(tppMessages.iterator().next().getMessageErrorCode().getCode());
        response.getWriter().write(xs2aObjectMapper.writeValueAsString(createError(tppMessages)));

        response.flushBuffer();
    }

    /**
     * Builds and inserts text representation of MessageError with definite HTTP code into HTTP response. Should only
     * be used for PIS services error handling.
     *
     * @param response     {@link javax.servlet.http.HttpServletRequest} the response to be enriched
     * @param messageError {@link MessageError} instance with text messag
     * @throws IOException in case IO operations error
     */
    public void buildPaymentErrorResponse(HttpServletResponse response, MessageError messageError) throws IOException {
        response.resetBuffer();
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Set<TppMessageInformation> tppMessages = messageError.getTppMessages();

        response.setStatus(tppMessages.iterator().next().getMessageErrorCode().getCode());
        response.getWriter().write(xs2aObjectMapper.writeValueAsString(createPaymentError(messageError)));

        response.flushBuffer();
    }

    public ErrorType buildErrorType() {
        return errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), FORMAT_ERROR.getCode());
    }

    private Object createError(Set<TppMessageInformation> tppMessageInformations) {
        MessageError messageError = getMessageError(tppMessageInformations);
        return Optional.ofNullable(errorMapperContainer.getErrorBody(messageError))
                   .map(ErrorMapperContainer.ErrorBody::getBody)
                   .orElse(null);
    }

    private Object createPaymentError(MessageError messageError) {
        return Optional.ofNullable(errorMapperContainer.getErrorBody(messageError))
                   .map(ErrorMapperContainer.ErrorBody::getBody)
                   .orElse(null);
    }

    private MessageError getMessageError(Set<TppMessageInformation> tppMessages) {
        ErrorType errorType = errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), FORMAT_ERROR.getCode());

        return new MessageError(errorType, tppMessages.toArray(new TppMessageInformation[tppMessages.size()]));
    }

    @Override
    public void enrichMessageError(MessageError messageError, MessageError validationMessageError) {
        enrichMessageError(messageError, validationMessageError.getTppMessage());
    }

    @Override
    public void enrichMessageError(MessageError messageError, TppMessageInformation tppMessageInformation) {
        messageError.addTppMessage(tppMessageInformation);
    }
}
