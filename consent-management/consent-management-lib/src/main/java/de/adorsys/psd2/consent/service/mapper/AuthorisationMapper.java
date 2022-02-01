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

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = PsuDataMapper.class)
public interface AuthorisationMapper {
    @Mapping(target = "psuIdData", source = "psuData")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "authorisationId", source = "externalId")
    @Mapping(target = "parentId", source = "parentExternalId")
    @Mapping(target = "chosenScaApproach", source = "scaApproach")
    @Mapping(target = "authorisationType", source = "type")
    Authorisation mapToAuthorisation(AuthorisationEntity authorisationEntity);

    List<Authorisation> mapToAuthorisations(List<AuthorisationEntity> authorisationEntityList);

    default AuthorisationEntity prepareAuthorisationEntity(Authorisable authorisationParent, CreateAuthorisationRequest request,
                                                           Optional<PsuData> psuDataOptional, AuthorisationType authorisationType,
                                                           long redirectUrlExpirationTimeMs, long authorisationExpirationTimeMs) {
        AuthorisationEntity entity = new AuthorisationEntity();
        entity.setType(authorisationType);
        psuDataOptional.ifPresent(entity::setPsuData);
        entity.setExternalId(Optional.ofNullable(request.getAuthorisationId()).orElse(UUID.randomUUID().toString()));
        entity.setParentExternalId(authorisationParent.getExternalId());
        entity.setScaStatus(request.getScaStatus());
        entity.setRedirectUrlExpirationTimestamp(OffsetDateTime.now().plus(redirectUrlExpirationTimeMs, ChronoUnit.MILLIS));
        entity.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plus(authorisationExpirationTimeMs, ChronoUnit.MILLIS));
        entity.setScaApproach(request.getScaApproach());
        TppRedirectUri redirectURIs = request.getTppRedirectURIs();
        AuthorisationTemplateEntity authorisationTemplate = authorisationParent.getAuthorisationTemplate();
        entity.setTppOkRedirectUri(StringUtils.defaultIfBlank(redirectURIs.getUri(), authorisationTemplate.getRedirectUri()));
        entity.setTppNokRedirectUri(StringUtils.defaultIfBlank(redirectURIs.getNokUri(), authorisationTemplate.getNokRedirectUri()));
        entity.setInstanceId(authorisationParent.getInstanceId());
        return entity;
    }
}
