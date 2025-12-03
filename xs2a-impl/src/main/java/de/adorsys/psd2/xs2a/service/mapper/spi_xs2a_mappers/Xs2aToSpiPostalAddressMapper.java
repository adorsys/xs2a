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

import de.adorsys.psd2.xs2a.core.vop.PostalAddress;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiPostalAddress;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting PostalAddress to SpiPostalAddress.
 */
@Component
public class Xs2aToSpiPostalAddressMapper {

    /**
     * Maps PostalAddress to SpiPostalAddress.
     *
     * @param source the XS2A PostalAddress to map
     * @return the mapped SpiPostalAddress
     */
    public SpiPostalAddress mapToSpiPostalAddress(PostalAddress source) {
        if (source == null) {
            return null;
        }

        SpiPostalAddress target = new SpiPostalAddress();
        target.setCountry(source.getCountry());
        target.setAddressLine(source.getAddressLine());
        target.setAddressLines(source.getAddressLines());
        target.setDepartment(source.getDepartment());
        target.setSubDepartment(source.getSubDepartment());
        target.setStreetName(source.getStreetName());
        target.setBuildingNumber(source.getBuildingNumber());
        target.setBuildingName(source.getBuildingName());
        target.setFloor(source.getFloor());
        target.setPostBox(source.getPostBox());
        target.setRoom(source.getRoom());
        target.setPostCode(source.getPostCode());
        target.setTownName(source.getTownName());
        target.setTownLocationName(source.getTownLocationName());
        target.setDistrictName(source.getDistrictName());
        target.setCountrySubDivision(source.getCountrySubDivision());

        return target;
    }
}

