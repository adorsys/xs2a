/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
