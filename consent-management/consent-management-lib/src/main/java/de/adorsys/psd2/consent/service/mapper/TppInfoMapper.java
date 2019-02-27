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

package de.adorsys.psd2.consent.service.mapper;


import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TppInfoMapper {
    public TppInfoEntity mapToTppInfoEntity(TppInfo tppInfo) {
        return Optional.ofNullable(tppInfo)
                   .map(tin -> {
                       TppInfoEntity tppInfoEntity = new TppInfoEntity();
                       tppInfoEntity.setAuthorisationNumber(tin.getAuthorisationNumber());
                       tppInfoEntity.setTppName(tin.getTppName());
                       tppInfoEntity.setTppRoles(copyTppRoles(tin.getTppRoles()));
                       tppInfoEntity.setAuthorityId(tin.getAuthorityId());
                       tppInfoEntity.setAuthorityName(tin.getAuthorityName());
                       tppInfoEntity.setCountry(tin.getCountry());
                       tppInfoEntity.setOrganisation(tin.getOrganisation());
                       tppInfoEntity.setOrganisationUnit(tin.getOrganisationUnit());
                       tppInfoEntity.setCity(tin.getCity());
                       tppInfoEntity.setState(tin.getState());

                       TppRedirectUri tppRedirectUri = tin.getTppRedirectUri();
                       if (tppRedirectUri != null) {
                           tppInfoEntity.setRedirectUri(tppRedirectUri.getUri());
                           tppInfoEntity.setNokRedirectUri(tppRedirectUri.getNokUri());
                       }

                       return tppInfoEntity;
                   }).orElse(null);
    }

    TppInfo mapToTppInfo(TppInfoEntity tppInfoEntity) {
        return Optional.ofNullable(tppInfoEntity)
                   .map(tpp -> {
                       TppInfo tppInfo = new TppInfo();

                       tppInfo.setAuthorisationNumber(tpp.getAuthorisationNumber());
                       tppInfo.setTppName(tpp.getTppName());
                       tppInfo.setTppRoles(copyTppRoles(tpp.getTppRoles()));
                       tppInfo.setAuthorityId(tpp.getAuthorityId());
                       tppInfo.setAuthorityName(tpp.getAuthorityName());
                       tppInfo.setCountry(tpp.getCountry());
                       tppInfo.setOrganisation(tpp.getOrganisation());
                       tppInfo.setOrganisationUnit(tpp.getOrganisationUnit());
                       tppInfo.setCity(tpp.getCity());
                       tppInfo.setState(tpp.getState());

                       if (tpp.getRedirectUri() != null) {
                           TppRedirectUri tppRedirectUri = new TppRedirectUri(tpp.getRedirectUri(),
                                                                              tpp.getNokRedirectUri());
                           tppInfo.setTppRedirectUri(tppRedirectUri);
                       }

                       return tppInfo;
                   }).orElse(null);
    }

    private @NotNull List<TppRole> copyTppRoles(@Nullable List<TppRole> tppRoles) {
        return Optional.ofNullable(tppRoles)
                   .map(ArrayList::new)
                   .orElseGet(ArrayList::new);
    }
}
