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

import de.adorsys.psd2.xs2a.domain.vop.*;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiGetBulkVopResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.response.SpiBulkVopInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.response.SpiSingleVopInitiationResponse;
import de.adorsys.psd2.xs2a.web.mapper.VopEnumMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        uses = {VopEnumMapper.class})
public interface SpiToXs2aVopResponseMapper {

    @Mapping(target = "errorHolder", expression = "java(null)")
    SingleVopInitiationReponse mapToVopInitiationResponse(SpiSingleVopInitiationResponse spiResponse);

    /**
     * Maps SPI bulk VOP initiation response to XS2A domain bulk VOP initiation response.
     *
     * @param spiResponse the SPI bulk VOP initiation response
     * @return the mapped XS2A bulk VOP initiation response
     */
    @Mapping(target = "errorHolder", expression = "java(null)")
    BulkVopInitiationResponse mapToBulkVopInitiationResponse(SpiBulkVopInitiationResponse spiResponse);

    /**
     * Maps SPI bulk VOP get response to XS2A domain bulk VOP get response.
     *
     * <p>This method transforms the SPI layer bulk VOP retrieval response into the XS2A domain
     * representation. It maps:</p>
     * <ul>
     *   <li>vopBulkId - The unique identifier of the bulk VOP verification request</li>
     *   <li>vopBulkStatus - The current status of the bulk verification process</li>
     *   <li>partyVerificationRequestResponses - List of verification request/response pairs</li>
     * </ul>
     *
     * <p>The errorHolder is explicitly set to null as this mapping is used for successful
     * SPI responses. Error handling is managed separately in the service layer.</p>
     *
     * <p>Null-safety: MapStruct automatically handles null input by returning null.
     * Nested objects and collections are mapped recursively using the configured enum mapper.</p>
     *
     * @param spiResponse the SPI bulk VOP get response containing verification results
     * @return the mapped XS2A domain response, or null if input is null
     */
    @Mapping(target = "errorHolder", expression = "java(null)")
    GetBulkVopRequestResponse mapToGetBulkVopResponse(SpiGetBulkVopResponse spiResponse);

    /**
     * Maps SPI bulk VOP status response to XS2A domain bulk VOP status response.
     *
     * <p>This method transforms the SPI layer bulk VOP status response into the XS2A domain
     * representation. Unlike {@link #mapToGetBulkVopResponse}, this mapping is optimized for
     * status-only queries and maps only:</p>
     * <ul>
     *   <li><b>vopBulkId</b> - The unique identifier of the bulk VOP verification request</li>
     *   <li><b>vopBulkStatus</b> - The current status of the bulk verification process (RECEIVED, PENDING, COMPLETED, REJECTED)</li>
     *   <li><b>reasonCode</b> - Optional standardized reason code explaining the current status (e.g., AM04, BEXX, DS0A)</li>
     *   <li><b>reasonProprietary</b> - Optional ASPSP-specific reason text for additional context</li>
     * </ul>
     *
     * <p><b>Key Differences from Full Response:</b></p>
     * <ul>
     *   <li>Does NOT include individual party verification request/response pairs</li>
     *   <li>Does NOT include detailed verification results or match scores</li>
     *   <li>Lightweight for efficient status polling without full result overhead</li>
     * </ul>
     *
     * <p><b>Enum Mapping:</b> The VopBulkStatus and StatusReasonCode enums are automatically
     * mapped by MapStruct via the {@link VopEnumMapper} which uses name-based matching since
     * the enum constants have identical names in both SPI and XS2A layers.</p>
     *
     * <p><b>Error Handling:</b> The errorHolder is explicitly set to null as this mapping is
     * used for successful SPI responses. Error handling is managed separately in the service layer.</p>
     *
     * <p><b>Null-safety:</b> MapStruct automatically handles null input by returning null.
     * Null enum values are also handled gracefully and mapped to null in the target object.</p>
     *
     * @param spiResponse the SPI bulk VOP status response containing only status information
     * @return the mapped XS2A domain status response, or null if input is null
     * @see de.adorsys.psd2.xs2a.spi.domain.vop.SpiGetBulkVopStatusResponse
     * @see GetBulkVopStatusResponse
     * @see VopEnumMapper
     */
    @Mapping(target = "errorHolder", expression = "java(null)")
    GetBulkVopStatusResponse mapToGetBulkVopStatusResponse(de.adorsys.psd2.xs2a.spi.domain.vop.SpiGetBulkVopStatusResponse spiResponse);

}
