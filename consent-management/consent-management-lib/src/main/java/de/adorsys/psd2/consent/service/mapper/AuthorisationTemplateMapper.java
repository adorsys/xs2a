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
