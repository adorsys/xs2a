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

import de.adorsys.psd2.xs2a.core.security.SecuringBodyMessage;
import de.adorsys.psd2.xs2a.spi.domain.vop.security.SpiSecuringBodyMessageData;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Mapper for converting XS2A SecuringBodyMessage to SPI SpiSecuringBodyMessageData.
 * Follows the mapping pattern established in Xs2aToSpiPsuDataMapper.
 */
@Component
public class Xs2aToSpiSecuringBodyMessageDataMapper {

    /**
     * Maps XS2A SecuringBodyMessage to SPI SpiSecuringBodyMessageData.
     *
     * @param securingBodyMessage the XS2A securing body message
     * @return SpiSecuringBodyMessageData with mapped values, or empty SpiSecuringBodyMessageData if input is null
     */
    public SpiSecuringBodyMessageData mapToSpiSecuringBodyMessageData(SecuringBodyMessage securingBodyMessage) {
        return Optional.ofNullable(securingBodyMessage)
                   .map(this::buildSpiSecuringBodyMessageData)
                   .orElseGet(SpiSecuringBodyMessageData::new);
    }

    private SpiSecuringBodyMessageData buildSpiSecuringBodyMessageData(SecuringBodyMessage securingBodyMessage) {
        SpiSecuringBodyMessageData spiData = new SpiSecuringBodyMessageData();
        spiData.setBodySigProfile(securingBodyMessage.bodySigProfile());
        spiData.setBodyEncProfile(securingBodyMessage.bodyEncProfile());
        spiData.setBodyEncList(securingBodyMessage.bodyEncList());
        return spiData;
    }
}
