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

package de.adorsys.psd2.xs2a.spi.domain.vop.response;

import de.adorsys.psd2.xs2a.spi.domain.vop.SpiPartyVerificationResponse;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiVopBulkStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * SPI response class for bulk VOP verification
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpiBulkVopInitiationResponse extends SpiVopInitiationResponse {
    private SpiVopBulkStatus vopBulkStatus;
    private List<SpiPartyVerificationResponse> partyVerificationResponses;
}
