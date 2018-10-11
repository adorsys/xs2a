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

package de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Component
public class SpiToXs2aAddressMapper {

    public Xs2aAddress mapToAddress(@NotNull SpiAddress creditorAddress) {
        return Optional.ofNullable(creditorAddress)
            .map(a -> {
                Xs2aAddress address = new Xs2aAddress();
                address.setCountry(new Xs2aCountryCode(a.getCountry()));
                address.setPostalCode(a.getPostalCode());
                address.setCity(a.getCity());
                address.setStreet(a.getStreet());
                address.setBuildingNumber(a.getBuildingNumber());
                return address;
            })
            .orElseGet(Xs2aAddress::new);
    }
}
