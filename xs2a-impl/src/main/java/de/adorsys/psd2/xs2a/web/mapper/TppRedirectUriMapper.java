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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TppRedirectUriMapper {
    /**
     * Maps redirectUri and nokRedirectUri to TppRedirectUri object
     *
     * @param redirectUri    URI of the TPP, where the flow will be redirected to
     * @param nokRedirectUri Nok URI of the TPP, where the flow will be redirected to in case of a negative response
     * @return new TppRedirectUri object if the given redirectUri wasn't null, <code>null</code> otherwise.
     * Returned TppRedirectUri will always contain redirectUri, but the nokRedirectUri may be null in case it wasn't provided to the mapper.
     */
    public @Nullable TppRedirectUri mapToTppRedirectUri(@Nullable String redirectUri, @Nullable String nokRedirectUri) {
        return Optional.ofNullable(redirectUri)
                   .map(redirect -> new TppRedirectUri(redirect, nokRedirectUri))
                   .orElse(new TppRedirectUri("", ""));
    }
}
