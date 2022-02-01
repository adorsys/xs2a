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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Xs2aAuthenticationObjectToCmsScaMethodMapper {
    @NotNull
    public List<CmsScaMethod> mapToCmsScaMethods(@NotNull List<AuthenticationObject> authenticationObjects) {
        return authenticationObjects.stream()
                   .map(this::mapToCmsScaMethod)
                   .collect(Collectors.toList());
    }

    @NotNull
    public CmsScaMethod mapToCmsScaMethod(@NotNull AuthenticationObject authenticationObject) {
        return new CmsScaMethod(authenticationObject.getAuthenticationMethodId(),
                                authenticationObject.isDecoupled());
    }
}
