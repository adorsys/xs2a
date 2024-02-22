/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TppServiceInternal implements TppService {
    private final TppInfoRepository tppInfoRepository;
    private final TppInfoMapper tppInfoMapper;

    @Value("${xs2a.cms.service.instance-id:UNDEFINED}")
    private String serviceInstanceId;

    @Override
    @Transactional
    public CmsResponse<Boolean> updateTppInfo(@NotNull TppInfo tppInfo) {
        Optional<TppInfoEntity> tppInfoEntityOptional = tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(tppInfo.getAuthorisationNumber(), serviceInstanceId);
        if (tppInfoEntityOptional.isEmpty()) {
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }
        TppInfoEntity tppInfoEntity = tppInfoEntityOptional.get();
        TppInfoEntity tppInfoEntityFromRequest = tppInfoMapper.mapToTppInfoEntity(tppInfo);

        if (!tppInfoEntityFromRequest.equalsWithoutId(tppInfoEntity)) {
            updateTppInfoEntity(tppInfoEntity, tppInfo);

        }
        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    private void updateTppInfoEntity(TppInfoEntity tppInfoEntityToUpdate, TppInfo tppInfo) {
        tppInfoEntityToUpdate.setTppName(tppInfo.getTppName());
        tppInfoEntityToUpdate.setTppRoles(tppInfo.getTppRoles());
        tppInfoEntityToUpdate.setAuthorityId(tppInfo.getAuthorityId());
        tppInfoEntityToUpdate.setAuthorityName(tppInfo.getAuthorityName());
        tppInfoEntityToUpdate.setCountry(tppInfo.getCountry());
        tppInfoEntityToUpdate.setOrganisation(tppInfo.getOrganisation());
        tppInfoEntityToUpdate.setOrganisationUnit(tppInfo.getOrganisationUnit());
        tppInfoEntityToUpdate.setCity(tppInfo.getCity());
        tppInfoEntityToUpdate.setState(tppInfo.getState());
    }

}
