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

import de.adorsys.psd2.model.PersonIdentificationCode;
import de.adorsys.psd2.xs2a.core.vop.PersonIdentification;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiPersonIdentification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting PersonIdentification to SpiPersonIdentification.
 */
@Component
public class Xs2aToSpiPersonIdentificationMapper {

    /**
     * Maps PersonIdentification to SpiPersonIdentification.
     *
     * @param source the XS2A PersonIdentification to map
     * @return the mapped SpiPersonIdentification
     */
    public SpiPersonIdentification mapToSpiPersonIdentification(PersonIdentification source) {
        if (source == null) {
            return null;
        }

        SpiPersonIdentification target = new SpiPersonIdentification();
        target.setIdentification(source.getIdentification());
        target.setSchemeNameCode(mapToPersonIdentificationCode(source.getSchemeNameCode()));
        target.setSchemeNameProprietary(source.getSchemeNameProprietary());
        target.setIssuer(source.getIssuer());

        return target;
    }

    /**
     * Maps a list of PersonIdentification to a list of SpiPersonIdentification.
     *
     * @param sourceList the list of XS2A PersonIdentification to map
     * @return the mapped list of SpiPersonIdentification
     */
    public List<SpiPersonIdentification> mapToSpiPersonIdentificationList(List<PersonIdentification> sourceList) {
        if (sourceList == null) {
            return null;
        }

        return sourceList.stream()
                .map(this::mapToSpiPersonIdentification)
                .collect(Collectors.toList());
    }

    /**
     * Maps String scheme name code to PersonIdentificationCode enum.
     *
     * @param schemeNameCode the String scheme name code
     * @return the mapped PersonIdentificationCode enum, or null if the input is null or invalid
     */
    private PersonIdentificationCode mapToPersonIdentificationCode(String schemeNameCode) {
        if (schemeNameCode == null) {
            return null;
        }

        try {
            return PersonIdentificationCode.valueOf(schemeNameCode);
        } catch (IllegalArgumentException e) {
            // If the value doesn't match any enum constant, return null
            return null;
        }
    }
}
