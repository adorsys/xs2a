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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.model.TppMessageGeneric;
import de.adorsys.psd2.model.TppMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@Component
public class ResponseMapper {

    private final MessageService messageService;

    public <T, R> ResponseEntity<?> ok(ResponseObject<T> response, Function<T, R> mapper) { //NOPMD short method name ok corresponds to status code
        return getEntity(response, OK, mapper);
    }

    public <T, R> ResponseEntity<?> created(ResponseObject<T> response, Function<T, R> mapper) {
        return getEntity(response, CREATED, mapper);
    }

    public <T, R> ResponseEntity<?> delete(ResponseObject<T> response, Function<T, R> mapper) {
        return getEntity(response, NO_CONTENT, mapper);
    }

    public <T> ResponseEntity<T> ok(ResponseObject<T> response) { //NOPMD short method name ok corresponds to status code
        return getEntity(response, OK);
    }

    public <T> ResponseEntity<T> created(ResponseObject<T> response) {
        return getEntity(response, CREATED);
    }

    public <T> ResponseEntity<T> delete(ResponseObject<T> response) {
        return getEntity(response, NO_CONTENT);
    }

    private <T> ResponseEntity<T> getEntity(ResponseObject<T> response, HttpStatus status) {
        return getEntity(response, status, null);
    }

    private <T, R> ResponseEntity getEntity(ResponseObject<T> response, HttpStatus status, Function<T, R> mapper) {
        T body = response.getBody();
        return response.hasError()
                   ? createErrorResponse(response.getError())
                   : new ResponseEntity<>(getBody(body, mapper), status);
    }

    private <T, R> Object getBody(T body, Function<T, R> mapper) {
        return mapper == null
                   ? body
                   : mapper.apply(body);
    }

    private ResponseEntity createErrorResponse(MessageError error) {
        TppMessages tppMessages = Optional.ofNullable(error)
                                      .map(MessageError::getTppMessages)
                                      .map(e -> e.stream()
                                                    .map(this::mapToGenericError)
                                                    .collect(Collectors.toList()))
                                      .map(c -> {
                                          TppMessages messages = new TppMessages();
                                          messages.addAll(c);
                                          return messages;
                                      })
                                      .orElse(null);


        return new ResponseEntity<>(tppMessages, valueOf(error.getTppMessage().getMessageErrorCode().getCode()));
    }

    private TppMessageGeneric mapToGenericError(TppMessageInformation info) {
        MessageErrorCode code = info.getMessageErrorCode();

        TppMessageGeneric tppMessage = new TppMessageGeneric();
        tppMessage.setCategory(TppMessageCategory.ERROR);
        tppMessage.setPath(info.getPath());
        tppMessage.setCode(code);
        tppMessage.setText(messageService.getMessage(code.name()));

        return tppMessage;
    }
}
