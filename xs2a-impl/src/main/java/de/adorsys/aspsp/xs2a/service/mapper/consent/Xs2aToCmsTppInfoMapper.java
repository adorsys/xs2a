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

package de.adorsys.aspsp.xs2a.service.mapper.consent;


import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.Xs2aTppRole;
import de.adorsys.psd2.consent.api.CmsTppInfo;
import de.adorsys.psd2.consent.api.CmsTppRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Xs2aToCmsTppInfoMapper {
    public CmsTppInfo mapToCmsTppInfo(TppInfo tppInfo) {
        return Optional.ofNullable(tppInfo)
                   .map(tin -> {
                       CmsTppInfo pisTppInfo = new CmsTppInfo();
                       pisTppInfo.setAuthorisationNumber(tin.getAuthorisationNumber());
                       pisTppInfo.setTppName(tin.getTppName());
                       pisTppInfo.setTppRoles(mapToListCmsTppRole(tppInfo.getTppRoles()));
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

    private List<CmsTppRole> mapToListCmsTppRole(List<Xs2aTppRole> tppRoles) {
        return Optional.ofNullable(tppRoles)
                   .map(l -> l.stream()
                                 .map(e -> CmsTppRole.valueOf(e.name()))
                                 .collect(Collectors.toList()))
                   .orElse(null);
    }
}
