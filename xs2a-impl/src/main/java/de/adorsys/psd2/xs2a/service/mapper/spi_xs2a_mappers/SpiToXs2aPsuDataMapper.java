/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.springframework.stereotype.Component;

@Component
public class SpiToXs2aPsuDataMapper {

    public PsuIdData mapToPsuIdData(SpiPsuData spiPsuData) {
        return new PsuIdData(
            spiPsuData.getPsuId(),
            spiPsuData.getPsuIdType(),
            spiPsuData.getPsuCorporateId(),
            spiPsuData.getPsuCorporateIdType(),
            spiPsuData.getPsuIpAddress()
        );
    }
}
