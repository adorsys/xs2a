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

import de.adorsys.psd2.xs2a.core.vop.ClearingSystemMemberIdentification;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiClearingSystemMemberIdentification;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiClearingSystemIdentificationCode;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting ClearingSystemMemberIdentification to SpiClearingSystemMemberIdentification.
 */
@Component
public class Xs2aToSpiClearingSystemMemberIdentificationMapper {

    /**
     * Maps ClearingSystemMemberIdentification to SpiClearingSystemMemberIdentification.
     *
     * @param source the XS2A ClearingSystemMemberIdentification to map
     * @return the mapped SpiClearingSystemMemberIdentification
     */
    public SpiClearingSystemMemberIdentification mapToSpiClearingSystemMemberIdentification(
            ClearingSystemMemberIdentification source) {
        if (source == null) {
            return null;
        }

        SpiClearingSystemMemberIdentification target = new SpiClearingSystemMemberIdentification();
        target.setMemberId(source.getMemberId());
        target.setClearingSystemIdentificationCode(mapToClearingSystemIdentificationCode(source.getClearingSystemIdentificationCode()));
        target.setClearingSystemIdentificationProprietary(source.getClearingSystemIdentificationProprietary());

        return target;
    }

    /**
     * Maps String clearingSystemIdentificationCode to SpiClearingSystemIdentificationCode enum.
     *
     * @param code the clearing system identification code as String
     * @return the corresponding SpiClearingSystemIdentificationCode enum value, or null if invalid
     */
    private SpiClearingSystemIdentificationCode mapToClearingSystemIdentificationCode(String code) {
        if (code == null) {
            return null;
        }

        try {
            return SpiClearingSystemIdentificationCode.fromValue(code);
        } catch (IllegalArgumentException e) {
            // Return null for invalid/unknown codes
            return null;
        }
    }
}
