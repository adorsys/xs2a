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

package de.adorsys.aspsp.xs2a.web.aspect;

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Component
@RequiredArgsConstructor
public abstract class AbstractLinkAspect<T> {
    protected final int maxNumberOfCharInTransactionJson;
    protected final AspspProfileService aspspProfileService;
    protected final JsonConverter jsonConverter;
    private final MessageService messageService;

    protected <B> boolean hasError(ResponseEntity<B> target) {
        Optional<B> body = Optional.ofNullable(target.getBody());
        return body.isPresent() && body.get().getClass()
                                       .isAssignableFrom(MessageError.class);
    }

    <R> ResponseObject<R> enrichErrorTextMessage(ResponseObject<R> response) {
        MessageError error = response.getError();
        TppMessageInformation tppMessage = error.getTppMessage();
        tppMessage.setText(messageService.getMessage(tppMessage.getMessageErrorCode().name()));
        error.setTppMessages(Collections.singleton(tppMessage));
        return ResponseObject.<R>builder()
                   .fail(error)
                   .build();
    }

    String buildPath(String path, Object... params) {
        return fromHttpUrl(linkTo(getControllerClass()).toString())
                   .path(path)
                   .buildAndExpand(params)
                   .toUriString();
    }

    @SuppressWarnings("unchecked")
    private Class<T> getControllerClass() {
        try {
            String className = ((ParameterizedType) this.getClass().getGenericSuperclass())
                                   .getActualTypeArguments()[0]
                                   .getTypeName();
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class isn't parametrized with generic type! Use <>");
        }
    }
}
