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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromController;

@AllArgsConstructor
public class BaseAspectService<T> {
    final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    String getHttpUrl() {
        return aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()
                   ? aspspProfileServiceWrapper.getXs2aBaseLinksUrl()
                   : fromController(this.getControllerClass()).pathSegment(StringUtils.EMPTY).toUriString();
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

    ScaRedirectFlow getScaRedirectFlow() {
        return aspspProfileServiceWrapper.getScaRedirectFlow();
    }

    boolean isAuthorisationConfirmationRequestMandated() {
        return aspspProfileServiceWrapper.isAuthorisationConfirmationRequestMandated();
    }
}
