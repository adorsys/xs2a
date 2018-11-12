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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class TppInfoMapper {
    TppInfoEntity mapToTppInfoEntity(TppInfo tppInfo) {
        return Optional.ofNullable(tppInfo)
                   .map(tin -> {
                       TppInfoEntity pisTppInfo = new TppInfoEntity();
                       pisTppInfo.setAuthorisationNumber(tin.getAuthorisationNumber());
                       pisTppInfo.setTppName(tin.getTppName());
                       pisTppInfo.setTppRoles(new ArrayList<>(tin.getTppRoles()));
                       pisTppInfo.setAuthorityId(tin.getAuthorityId());
                       pisTppInfo.setAuthorityName(tin.getAuthorityName());
                       pisTppInfo.setCountry(tin.getCountry());
                       pisTppInfo.setOrganisation(tin.getOrganisation());
                       pisTppInfo.setOrganisationUnit(tin.getOrganisationUnit());
                       pisTppInfo.setCity(tin.getCity());
                       pisTppInfo.setState(tin.getState());
                       pisTppInfo.setRedirectUri(tin.getRedirectUri());
                       pisTppInfo.setNokRedirectUri(tin.getNokRedirectUri());
                       pisTppInfo.setStatus(tin.getStatus());


                       return pisTppInfo;
                   }).orElse(null);
    }

    TppInfo mapToTppInfo(TppInfoEntity tppInfoEntity) {
        return Optional.ofNullable(tppInfoEntity)
                   .map(tpp -> {
                       TppInfo tppInfo = new TppInfo();

                       tppInfo.setAuthorisationNumber(tpp.getAuthorisationNumber());
                       tppInfo.setTppName(tpp.getTppName());
                       tppInfo.setTppRoles(new ArrayList<>(tpp.getTppRoles()));
                       tppInfo.setAuthorityId(tpp.getAuthorityId());
                       tppInfo.setAuthorityName(tpp.getAuthorityName());
                       tppInfo.setCountry(tpp.getCountry());
                       tppInfo.setOrganisation(tpp.getOrganisation());
                       tppInfo.setOrganisationUnit(tpp.getOrganisationUnit());
                       tppInfo.setCity(tpp.getCity());
                       tppInfo.setState(tpp.getState());
                       tppInfo.setRedirectUri(tpp.getRedirectUri());
                       tppInfo.setNokRedirectUri(tpp.getNokRedirectUri());
                       tppInfo.setStatus(tpp.getStatus());

                       return tppInfo;
                   }).orElse(null);
    }
}
