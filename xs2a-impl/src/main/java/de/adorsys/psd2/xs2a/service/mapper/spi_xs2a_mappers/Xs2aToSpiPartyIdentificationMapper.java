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

import de.adorsys.psd2.xs2a.core.vop.PartyIdentification;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiPartyIdentification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting PartyIdentification to SpiPartyIdentification.
 */
@Component
@RequiredArgsConstructor
public class Xs2aToSpiPartyIdentificationMapper {

    private final Xs2aToSpiOrganisationIdentificationMapper organisationIdentificationMapper;
    private final Xs2aToSpiPrivateIdentificationMapper privateIdentificationMapper;

    /**
     * Maps PartyIdentification to SpiPartyIdentification.
     *
     * @param source the XS2A PartyIdentification to map
     * @return the mapped SpiPartyIdentification
     */
    public SpiPartyIdentification mapToSpiPartyIdentification(PartyIdentification source) {
        if (source == null) {
            return null;
        }

        SpiPartyIdentification target = new SpiPartyIdentification();
        target.setOrganisationId(organisationIdentificationMapper.mapToSpiOrganisationIdentification(source.getOrganisationId()));
        target.setPrivateId(privateIdentificationMapper.mapToSpiPrivateIdentification(source.getPrivateId()));

        return target;
    }
}
