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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsAuthorisation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PsuDataMapper.class})
public interface CmsPsuAuthorisationMapper {
    @Mapping(target = "psuId", source = "psuData.psuId")
    @Mapping(target = "authorisationId", source = "externalId")
    @Mapping(target = "type", source = "type")
    CmsPsuAuthorisation mapToCmsPsuAuthorisation(AuthorisationEntity authorisationEntity);

    @Mapping(target = "psuIdData", source = "psuData")
    @Mapping(target = "piisConsentId", source = "parentExternalId")
    @Mapping(target = "authorisationId", source = "externalId")
    @Mapping(target = "type", source = "type")
    CmsPsuConfirmationOfFundsAuthorisation mapToCmsPsuConfirmationOfFundsAuthorisation(AuthorisationEntity authorisationEntity);
}
