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
