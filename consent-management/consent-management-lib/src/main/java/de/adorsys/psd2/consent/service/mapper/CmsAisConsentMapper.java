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

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public abstract class CmsAisConsentMapper {
    @Autowired
    protected ConsentDataMapper consentDataMapper;


    @Mapping(target = "consentTppInformation", source = "tppInformation")
    @Mapping(target = "consentData", expression = "java(consentDataMapper.mapToAisConsentData(cmsConsent.getConsentData()))")
    @Mapping(target = "authorisations", expression = "java(mapToAccountConsentAuthorisation(cmsConsent.getAuthorisations()))")
    public abstract AisConsent mapToAisConsent(CmsConsent cmsConsent);

    List<ConsentAuthorization> mapToAccountConsentAuthorisation(List<Authorisation> authorisations) {
        if (CollectionUtils.isEmpty(authorisations)) {
            return Collections.emptyList();
        }
        return authorisations.stream()
                   .map(this::mapToAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    ConsentAuthorization mapToAccountConsentAuthorisation(Authorisation authorisation) {
        return Optional.ofNullable(authorisation)
                   .map(auth -> {
                       ConsentAuthorization accountConsentAuthorisation = new ConsentAuthorization();
                       accountConsentAuthorisation.setId(auth.getAuthorisationId());
                       accountConsentAuthorisation.setPsuIdData(auth.getPsuIdData());
                       accountConsentAuthorisation.setScaStatus(auth.getScaStatus());
                       return accountConsentAuthorisation;
                   })
                   .orElse(null);
    }
}
