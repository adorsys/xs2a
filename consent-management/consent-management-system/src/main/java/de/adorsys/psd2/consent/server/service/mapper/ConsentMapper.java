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

package de.adorsys.psd2.consent.server.service.mapper;

import de.adorsys.psd2.consent.api.CmsTppInfo;
import de.adorsys.psd2.consent.api.CmsTppRole;
import de.adorsys.psd2.consent.server.domain.TppInfo;
import de.adorsys.psd2.consent.server.domain.TppRole;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsentMapper {
    public TppInfo mapToTppInfo(CmsTppInfo tppInfo) {
        return Optional.ofNullable(tppInfo)
                   .map(tin -> {
                       TppInfo pisTppInfo = new TppInfo();
                       pisTppInfo.setAuthorisationNumber(tin.getAuthorisationNumber());
                       pisTppInfo.setTppName(tin.getTppName());
                       pisTppInfo.setTppRoles(mapToTppRoles(tin.getTppRoles()));
                       pisTppInfo.setAuthorityId(tin.getAuthorityId());
                       pisTppInfo.setAuthorityName(tin.getAuthorityName());
                       pisTppInfo.setCountry(tin.getCountry());
                       pisTppInfo.setOrganisation(tin.getOrganisation());
                       pisTppInfo.setOrganisationUnit(tin.getOrganisationUnit());
                       pisTppInfo.setCity(tin.getCity());
                       pisTppInfo.setState(tin.getState());
                       pisTppInfo.setRedirectUri(tin.getRedirectUri());
                       pisTppInfo.setNokRedirectUri(tin.getNokRedirectUri());

                       return pisTppInfo;
                   }).orElse(null);
    }

    public CmsTppInfo mapToCmsTppInfo(TppInfo pisTppInfo) {
        return Optional.ofNullable(pisTppInfo)
                   .map(tpp -> {
                       CmsTppInfo tppInfo = new CmsTppInfo();

                       tppInfo.setAuthorisationNumber(tpp.getAuthorisationNumber());
                       tppInfo.setTppName(tpp.getTppName());
                       tppInfo.setTppRoles(mapToCmsTppRoles(tpp.getTppRoles()));
                       tppInfo.setAuthorityId(tpp.getAuthorityId());
                       tppInfo.setAuthorityName(tpp.getAuthorityName());
                       tppInfo.setCountry(tpp.getCountry());
                       tppInfo.setOrganisation(tpp.getOrganisation());
                       tppInfo.setOrganisationUnit(tpp.getOrganisationUnit());
                       tppInfo.setCity(tpp.getCity());
                       tppInfo.setState(tpp.getState());
                       tppInfo.setRedirectUri(tpp.getRedirectUri());
                       tppInfo.setNokRedirectUri(tpp.getNokRedirectUri());

                       return tppInfo;
                   }).orElse(null);
    }

    private List<TppRole> mapToTppRoles(List<CmsTppRole> tppRoles) {
        return Optional.ofNullable(tppRoles)
                   .map(roles -> roles.stream()
                                     .map(role -> TppRole.valueOf(role.name()))
                                     .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private List<CmsTppRole> mapToCmsTppRoles(List<TppRole> tppRoles) {
        return Optional.ofNullable(tppRoles)
                   .map(roles -> roles.stream()
                                     .map(role -> CmsTppRole.valueOf(role.name()))
                                     .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }
}
