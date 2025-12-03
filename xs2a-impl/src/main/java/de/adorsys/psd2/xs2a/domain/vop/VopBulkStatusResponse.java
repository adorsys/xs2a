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

package de.adorsys.psd2.xs2a.domain.vop;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.vop.VopBulkStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class VopBulkStatusResponse {
    private String reasonProprietary;
    private VopBulkStatus vopBulkStatus;
    private String reasonCode;
    private UUID vopBulkId;
    private ErrorHolder errorHolder;

    public VopBulkStatusResponse(String reasonProprietary, VopBulkStatus vopBulkStatus,
            String reasonCode, UUID vopBulkId) {
        this.reasonProprietary = reasonProprietary;
        this.vopBulkStatus = vopBulkStatus;
        this.reasonCode = reasonCode;
        this.vopBulkId = vopBulkId;
    }

    public VopBulkStatusResponse(ErrorHolder errorHolder) {
        this.errorHolder = errorHolder;
    }

    public boolean hasError() {
        return errorHolder != null;
    }
}