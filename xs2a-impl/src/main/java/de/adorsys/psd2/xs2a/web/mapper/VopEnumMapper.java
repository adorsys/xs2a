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

package de.adorsys.psd2.xs2a.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = "spring")
public interface VopEnumMapper {

    @ValueMapping(source = "MTCH", target = "MTCH")
    @ValueMapping(source = "NMTC", target = "NMTC")
    @ValueMapping(source = "CMTC", target = "CMTC")
    @ValueMapping(source = "NOAP", target = "NOAP")
    de.adorsys.psd2.xs2a.core.vop.PartyNameMatchCode mapToXs2aPartyNameMatchCode(de.adorsys.psd2.model.PartyNameMatchCode partyNameMatchCode);

    @ValueMapping(source = "MTCH", target = "MTCH")
    @ValueMapping(source = "NMTC", target = "NMTC")
    @ValueMapping(source = "NOAP", target = "NOAP")
    de.adorsys.psd2.xs2a.core.vop.PartyIdentificationMatchCode mapToXs2aPartyIdentificationMatchCode(de.adorsys.psd2.model.PartyIdentificationMatchCode partyIdMatchCode);

    /**
     * Maps SPI VOP bulk status to XS2A VOP bulk status.
     *
     * <p>MapStruct automatically handles enum mapping by matching enum constant names.
     * Since both enums have the same constant names (RECEIVED, PENDING, COMPLETED, REJECTED),
     * no explicit value mappings are needed.</p>
     *
     * @param spiStatus the SPI VOP bulk status
     * @return the corresponding XS2A VOP bulk status, or null if input is null
     */
    de.adorsys.psd2.xs2a.core.vop.VopBulkStatus mapToXs2aVopBulkStatus(de.adorsys.psd2.xs2a.spi.domain.vop.SpiVopBulkStatus spiStatus);

    /**
     * Maps SPI status reason code to XS2A status reason code.
     *
     * <p>MapStruct automatically handles enum mapping by matching enum constant names.
     * Since both enums have the same constant names (AM04, AM21, BEXX, CN01, etc.),
     * most constants map automatically.</p>
     *
     * <p>Note: Explicit mappings are required for DS0_C, DS0_D, DS0_A, DS0_K because
     * the source SPI enum uses underscores while the target StatusReasonCode from xs2a-server-api
     * uses no underscores (DS0C, DS0D, DS0A, DS0K).</p>
     *
     * @param spiReasonCode the SPI status reason code
     * @return the corresponding XS2A status reason code, or null if input is null
     */
    @ValueMapping(source = "DS0_C", target = "DS0C")
    @ValueMapping(source = "DS0_D", target = "DS0D")
    @ValueMapping(source = "DS0_A", target = "DS0A")
    @ValueMapping(source = "DS0_K", target = "DS0K")
    de.adorsys.psd2.model.StatusReasonCode mapToXs2aStatusReasonCode(de.adorsys.psd2.xs2a.spi.domain.vop.SpiStatusReasonCode spiReasonCode);
}
