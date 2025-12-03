/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.service.validator.ErrorMessageBuilder;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;

@Component
@RequiredArgsConstructor
public class ErrorBuildingService implements ErrorMessageBuilder {

    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;
    private final ErrorMapperContainer errorMapperContainer;
    private final Xs2aObjectMapper xs2aObjectMapper;
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final RequestProviderService requestProviderService;

    /**
     * Builds and inserts text representation of MessageError with HTTP code 400 into HTTP response. Used in request
     * validation chain and handles a list of errors.
     *
     * @param response     {@link jakarta.servlet.http.HttpServletRequest} the response to be enriched
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

        // Add required headers for non-200 responses
        addNonOkResponseHeaders(response);

        response.flushBuffer();
    }

    /**
     * Builds and inserts text representation of MessageError with definite HTTP code into HTTP response. Should only
     * be used for PIS services error handling.
     *
     * @param response     {@link jakarta.servlet.http.HttpServletRequest} the response to be enriched
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

        // Add required headers for non-200 responses
        addNonOkResponseHeaders(response);

        response.flushBuffer();
    }

    /**
     * Builds and inserts text representation of MessageError with definite HTTP code into HTTP response. Should only
     * be used for VOP services error handling.
     *
     * @param response     {@link jakarta.servlet.http.HttpServletRequest} the response to be enriched
     * @param messageError {@link MessageError} instance with text messages
     * @throws IOException in case IO operations error
     */
    public void buildVopErrorResponse(HttpServletResponse response, MessageError messageError) throws IOException {
        response.resetBuffer();
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Set<TppMessageInformation> tppMessages = messageError.getTppMessages();

        response.setStatus(tppMessages.iterator().next().getMessageErrorCode().getCode());
        response.getWriter().write(xs2aObjectMapper.writeValueAsString(createPaymentError(messageError)));

        // Add required headers for non-200 responses
        addNonOkResponseHeaders(response);

        response.flushBuffer();
    }

    public ErrorType buildErrorType() {
        return errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), FORMAT_ERROR.getCode());
    }

    private Object createError(Set<TppMessageInformation> tppMessageInformations) {
        String requestPath = requestProviderService.getRequestUri();
        Set<TppMessageInformation> enrichedMessages = tppMessageInformations.stream()
            .map(msg -> enrichMessageWithPath(msg, requestPath))
            .collect(Collectors.toSet());
        
        MessageError messageError = getMessageError(enrichedMessages);
        return Optional.ofNullable(errorMapperContainer.getErrorBody(messageError))
                   .map(ErrorMapperContainer.ErrorBody::getBody)
                   .orElse(null);
    }

    private Object createPaymentError(MessageError messageError) {
        String requestPath = requestProviderService.getRequestUri();
        Set<TppMessageInformation> enrichedMessages = messageError.getTppMessages().stream()
            .map(msg -> enrichMessageWithPath(msg, requestPath))
            .collect(Collectors.toSet());
        
        MessageError enrichedMessageError = new MessageError(messageError.getErrorType());
        enrichedMessages.forEach(enrichedMessageError::addTppMessage);
        
        return Optional.ofNullable(errorMapperContainer.getErrorBody(enrichedMessageError))
                   .map(ErrorMapperContainer.ErrorBody::getBody)
                   .orElse(null);
    }

    /**
     * Enriches a TppMessageInformation with the request path if it doesn't already have one.
     * Creates a new TppMessageInformation instance with the request path populated.
     */
    private TppMessageInformation enrichMessageWithPath(TppMessageInformation message, String requestPath) {
        // Check if message already has a path set
        if (message.getPath() != null) {
            return message; // Already has a path, don't override
        }
        // Create new message with path
        return TppMessageInformation.of(
            message.getCategory(), 
            message.getMessageErrorCode(), 
            requestPath, 
            message.getTextParameters()
        );
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

    /**
     * Adds required headers for non-OK responses to the HttpServletResponse.
     * Headers added:
     * - X-Request-ID (from request)
     * - X-Reference-API-Name (from ASPSP profile)
     * - X-Reference-API-Document (from ASPSP profile)
     * - X-Reference-API-Version (from ASPSP profile)
     * - Location (empty string, as per requirement)
     * - ASPSP-Notification-Support (from ASPSP profile)
     *
     * @param response the HttpServletResponse to add headers to
     */
    private void addNonOkResponseHeaders(HttpServletResponse response) {
        addHeaderIfPresent(response, "X-Request-ID", requestProviderService.getRequestIdString());
        addHeaderIfPresent(response, "X-Reference-API-Name", aspspProfileServiceWrapper.getXReferenceApiName());
        addHeaderIfPresent(response, "X-Reference-API-Document", aspspProfileServiceWrapper.getXReferenceApiDocument());
        addHeaderIfPresent(response, "X-Reference-API-Version", aspspProfileServiceWrapper.getXReferenceApiVersion());
        addLocationHeader(response);
        addHeaderIfPresent(response, "ASPSP-Notification-Support", getNotificationSupportedModesAsString());
    }

    private void addHeaderIfPresent(HttpServletResponse response, String headerName, String headerValue) {
        if (headerValue != null && !headerValue.isEmpty()) {
            response.addHeader(headerName, headerValue);
        }
    }

    private void addLocationHeader(HttpServletResponse response) {
        try {
            String requestUri = requestProviderService.getRequestData().getUri();
            if (requestUri != null && !requestUri.isEmpty()) {
                response.addHeader("Location", requestUri);
            }
        } catch (Exception e) {
            // If we can't get the request URI, add empty Location header
            response.addHeader("Location", "");
        }
    }

    /**
     * Converts the list of NotificationSupportedMode to a comma-separated string.
     *
     * @return comma-separated string of notification modes, or null if list is empty/null
     */
    private String getNotificationSupportedModesAsString() {
        List<NotificationSupportedMode> supportedModes = aspspProfileServiceWrapper.getNotificationSupportedModes();

        if (supportedModes == null || supportedModes.isEmpty()) {
            return null;
        }

        return supportedModes.stream()
                   .map(NotificationSupportedMode::getValue)
                   .collect(Collectors.joining(","));
    }
}
