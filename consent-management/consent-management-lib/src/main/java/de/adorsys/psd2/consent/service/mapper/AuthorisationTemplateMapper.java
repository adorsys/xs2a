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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;
import java.util.function.Function;

@Mapper(componentModel = "spring", imports = {Optional.class, TppRedirectUri.class})
public interface AuthorisationTemplateMapper {

    @Mapping(target = "tppRedirectUri", expression = "java(createTppRedirectUri(entity.getRedirectUri(), entity.getNokRedirectUri()))")
    @Mapping(target = "cancelTppRedirectUri", expression = "java(createTppRedirectUri(entity.getCancelRedirectUri(), entity.getCancelNokRedirectUri()))")
    AuthorisationTemplate mapToAuthorisationTemplate(AuthorisationTemplateEntity entity);

    @Mapping(target = "redirectUri", expression = "java(getUriOrNull(authorisationTemplate.getTppRedirectUri(), TppRedirectUri::getUri))")
    @Mapping(target = "nokRedirectUri", expression = "java(getUriOrNull(authorisationTemplate.getTppRedirectUri(), TppRedirectUri::getNokUri))")
    @Mapping(target = "cancelRedirectUri", expression = "java(getUriOrNull(authorisationTemplate.getCancelTppRedirectUri(), TppRedirectUri::getUri))")
    @Mapping(target = "cancelNokRedirectUri", expression = "java(getUriOrNull(authorisationTemplate.getCancelTppRedirectUri(), TppRedirectUri::getNokUri))")
    AuthorisationTemplateEntity mapToAuthorisationTemplateEntity(AuthorisationTemplate authorisationTemplate);

    default TppRedirectUri createTppRedirectUri(String redirectUri, String nokRedirectUri) {
        if (redirectUri != null) {
            return new TppRedirectUri(redirectUri, nokRedirectUri);
        }
        return null;
    }

    default String getUriOrNull(TppRedirectUri tppRedirectUri, Function<TppRedirectUri, String> function) {
        return Optional.ofNullable(tppRedirectUri).map(function).orElse(null);
    }
}
