/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.vop.OrganisationIdentification;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiOrganisationIdentification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting OrganisationIdentification to SpiOrganisationIdentification.
 */
@Component
@RequiredArgsConstructor
public class Xs2aToSpiOrganisationIdentificationMapper {

    private final Xs2aToSpiGenericOrganisationIdentificationMapper genericOrganisationIdentificationMapper;

    /**
     * Maps OrganisationIdentification to SpiOrganisationIdentification.
     *
     * @param source the XS2A OrganisationIdentification to map
     * @return the mapped SpiOrganisationIdentification
     */
    public SpiOrganisationIdentification mapToSpiOrganisationIdentification(OrganisationIdentification source) {
        if (source == null) {
            return null;
        }

        SpiOrganisationIdentification target = new SpiOrganisationIdentification();
        target.setAnyBIC(source.getAnyBIC());
        target.setLei(source.getLei());
        target.setOthers(genericOrganisationIdentificationMapper.mapToSpiGenericOrganisationIdentificationList(source.getOthers()));

        return target;
    }
}
