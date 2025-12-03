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

import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Xs2aToSpiAddressMapper {

    public SpiAddress mapToSpiAddress(Xs2aAddress address) {
        if (address == null) {
            return null;
        }

        return new SpiAddress(
                address.getAddressLines(),
                address.getDepartment(),
                address.getSubDepartment(),
                address.getStreetName(),
                address.getBuildingNumber(),
                address.getBuildingName(),
                address.getFloor(),
                address.getPostBox(),
                address.getRoom(),
                address.getPostCode(),
                address.getTownName(),
                address.getTownLocationName(),
                address.getDistrictName(),
                address.getCountrySubDivision(),
                Optional.ofNullable(address.getCountry()).map(Xs2aCountryCode::getCode).orElse(""));
    }

    public Xs2aAddress mapToXs2aAddress(SpiAddress address) {
        if (address == null) {
            return null;
        }

        Xs2aAddress xs2aAddress = new Xs2aAddress();
        xs2aAddress.setAddressLines(address.getAddressLines());
        xs2aAddress.setDepartment(address.getDepartment());
        xs2aAddress.setSubDepartment(address.getSubDepartment());
        xs2aAddress.setStreetName(address.getStreetName());
        xs2aAddress.setBuildingNumber(address.getBuildingNumber());
        xs2aAddress.setBuildingName(address.getBuildingName());
        xs2aAddress.setFloor(address.getFloor());
        xs2aAddress.setPostBox(address.getPostBox());
        xs2aAddress.setRoom(address.getRoom());
        xs2aAddress.setPostCode(address.getPostCode());
        xs2aAddress.setTownName(address.getTownName());
        xs2aAddress.setTownLocationName(address.getTownLocationName());
        xs2aAddress.setDistrictName(address.getDistrictName());
        xs2aAddress.setCountrySubDivision(address.getCountrySubDivision());
        xs2aAddress.setCountry(new Xs2aCountryCode(address.getCountry()));

        return xs2aAddress;
    }
}
