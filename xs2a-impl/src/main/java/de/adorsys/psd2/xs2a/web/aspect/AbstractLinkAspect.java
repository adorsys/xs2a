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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromController;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class AbstractLinkAspect<T> {
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    protected <B> boolean hasError(ResponseEntity<B> target) {
        Optional<B> body = Optional.ofNullable(target.getBody());
        return body.isPresent() && body.get().getClass()
                                       .isAssignableFrom(MessageError.class);
    }

    ScaRedirectFlow getScaRedirectFlow() {
        return aspspProfileServiceWrapper.getScaRedirectFlow();
    }

    String getHttpUrl() {
        return aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()
                   ? aspspProfileServiceWrapper.getXs2aBaseLinksUrl()
                   : fromController(this.getControllerClass()).pathSegment(StringUtils.EMPTY).toUriString();
    }

    boolean isAuthorisationConfirmationRequestMandated() {
        return aspspProfileServiceWrapper.isAuthorisationConfirmationRequestMandated();
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
