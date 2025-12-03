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

import de.adorsys.psd2.xs2a.core.vop.PrivateIdentification;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiPrivateIdentification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting PrivateIdentification to SpiPrivateIdentification.
 */
@Component
@RequiredArgsConstructor
public class Xs2aToSpiPrivateIdentificationMapper {

    private final Xs2aToSpiPersonIdentificationMapper personIdentificationMapper;

    /**
     * Maps PrivateIdentification to SpiPrivateIdentification.
     *
     * @param source the XS2A PrivateIdentification to map
     * @return the mapped SpiPrivateIdentification
     */
    public SpiPrivateIdentification mapToSpiPrivateIdentification(PrivateIdentification source) {
        if (source == null) {
            return null;
        }

        SpiPrivateIdentification target = new SpiPrivateIdentification();
        target.setBirthDate(source.getBirthDate());
        target.setProvinceOfBirth(source.getProvinceOfBirth());
        target.setCityOfBirth(source.getCityOfBirth());
        target.setCountryOfBirth(source.getCountryOfBirth());
        target.setOthers(personIdentificationMapper.mapToSpiPersonIdentificationList(source.getOthers()));

        return target;
    }
}
