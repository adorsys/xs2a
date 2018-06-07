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
import de.adorsys.aspsp.xs2a.exception.MessageError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

@Component
public abstract class AbstractLinkAspect<T> {
    @Autowired
    protected int maxNumberOfCharInTransactionJson;
    @Autowired
    protected String redirectLinkToSource;
    @Autowired
    protected  JsonConverter jsonConverter;

    protected Class<T> getController() {
        try {
            String className = ((ParameterizedType) getClass().getGenericSuperclass())
                                   .getActualTypeArguments()[0]
                                   .getTypeName();
            return (Class<T>) Class.forName(className);
        } catch (Exception e) {
            throw new IllegalStateException("Class isn't parametrized with generic type! Use <>");
        }
    }

    protected <B> boolean hasError(ResponseEntity<B> target) {
        Optional<B> body = Optional.ofNullable(target.getBody());
        return body.isPresent() && body.get().getClass()
                                       .isAssignableFrom(MessageError.class);
    }
}
