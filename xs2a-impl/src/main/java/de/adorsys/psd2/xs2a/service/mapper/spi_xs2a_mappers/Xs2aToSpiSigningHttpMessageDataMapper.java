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

import de.adorsys.psd2.xs2a.core.security.SigningHttpMessage;
import de.adorsys.psd2.xs2a.spi.domain.vop.security.SpiSigningHttpMessageData;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Mapper for converting XS2A SigningHttpMessage to SPI SpiSigningHttpMessageData.
 * Follows the mapping pattern established in Xs2aToSpiPsuDataMapper.
 */
@Component
public class Xs2aToSpiSigningHttpMessageDataMapper {

    /**
     * Maps XS2A SigningHttpMessage to SPI SpiSigningHttpMessageData.
     *
     * @param signingHttpMessage the XS2A signing HTTP message
     * @return SpiSigningHttpMessageData with mapped values, or empty SpiSigningHttpMessageData if input is null
     */
    public SpiSigningHttpMessageData mapToSpiSigningHttpMessageData(SigningHttpMessage signingHttpMessage) {
        return Optional.ofNullable(signingHttpMessage)
                   .map(this::buildSpiSigningHttpMessageData)
                   .orElseGet(SpiSigningHttpMessageData::new);
    }

    private SpiSigningHttpMessageData buildSpiSigningHttpMessageData(SigningHttpMessage signingHttpMessage) {
        SpiSigningHttpMessageData spiData = new SpiSigningHttpMessageData();
        spiData.setDigest(signingHttpMessage.digest());
        spiData.setXJwsSignature(signingHttpMessage.xJwsSignature());
        return spiData;
    }
}
