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

import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface Xs2aTppInfoMapper {

    @Mapping(source = "name", target = "tppName")
    @Mapping(source = "pspAuthorisationNumber", target = "authorisationNumber")
    @Mapping(source = "pspAuthorityId", target = "authorityId")
    @Mapping(source = "pspAuthorityName", target = "authorityName")
    TppInfo mapToTppInfo(TppCertificateData tppInfoEntity);

    default List<TppRole> mapToTppRoles(List<String> rolesList) {
        return rolesList.stream()
            .map(String::trim)
            .map(this::getTppRole)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private TppRole getTppRole(String role) {
        return Arrays.stream(TppRole.values())
            .map(Enum::toString)
            .filter(roleString -> roleString.equals(role))
            .findFirst()
            .map(TppRole::valueOf)
            .orElse(null);
    }
}
