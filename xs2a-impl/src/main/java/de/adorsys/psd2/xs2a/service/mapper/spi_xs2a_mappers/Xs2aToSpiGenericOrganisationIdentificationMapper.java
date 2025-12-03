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

import de.adorsys.psd2.xs2a.core.vop.GenericOrganisationIdentification;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiGenericOrganisationIdentification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting GenericOrganisationIdentification to SpiGenericOrganisationIdentification.
 */
@Component
public class Xs2aToSpiGenericOrganisationIdentificationMapper {

    /**
     * Maps GenericOrganisationIdentification to SpiGenericOrganisationIdentification.
     *
     * @param source the XS2A GenericOrganisationIdentification to map
     * @return the mapped SpiGenericOrganisationIdentification
     */
    public SpiGenericOrganisationIdentification mapToSpiGenericOrganisationIdentification(
            GenericOrganisationIdentification source) {
        if (source == null) {
            return null;
        }

        SpiGenericOrganisationIdentification target = new SpiGenericOrganisationIdentification();
        target.setIdentification(source.getIdentification());
        target.setSchemeNameCode(source.getSchemeNameCode());
        // Note: schemeNameProprietary and issuer are not present in SPI model

        return target;
    }

    /**
     * Maps a list of GenericOrganisationIdentification to a list of SpiGenericOrganisationIdentification.
     *
     * @param sourceList the list of XS2A GenericOrganisationIdentification to map
     * @return the mapped list of SpiGenericOrganisationIdentification
     */
    public List<SpiGenericOrganisationIdentification> mapToSpiGenericOrganisationIdentificationList(
            List<GenericOrganisationIdentification> sourceList) {
        if (sourceList == null) {
            return null;
        }

        return sourceList.stream()
                .map(this::mapToSpiGenericOrganisationIdentification)
                .collect(Collectors.toList());
    }
}
