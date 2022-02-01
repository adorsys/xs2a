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
            address.getStreetName(),
            address.getBuildingNumber(),
            address.getTownName(),
            address.getPostCode(),
            Optional.ofNullable(address.getCountry()).map(Xs2aCountryCode::getCode).orElse(""));
    }

    public Xs2aAddress mapToXs2aAddress(SpiAddress address) {
        if (address == null) {
            return null;
        }

        Xs2aAddress xs2aAddress = new Xs2aAddress();
        xs2aAddress.setStreetName(address.getStreetName());
        xs2aAddress.setBuildingNumber(address.getBuildingNumber());
        xs2aAddress.setTownName(address.getTownName());
        xs2aAddress.setPostCode(address.getPostCode());
        xs2aAddress.setCountry(new Xs2aCountryCode(address.getCountry()));

        return xs2aAddress;
    }
}
