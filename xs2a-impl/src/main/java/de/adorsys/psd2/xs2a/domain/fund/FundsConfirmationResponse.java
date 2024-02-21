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

package de.adorsys.psd2.xs2a.domain.fund;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "FundsConfirmationResponse", name = "Funds confirmation response")
public class FundsConfirmationResponse {
    @Schema(description = "Equals 'true' if sufficient funds are available at the time of the request, 'false' otherwise.", example = "true")
    private boolean fundsAvailable;
    private ErrorHolder errorHolder;

    public FundsConfirmationResponse(boolean fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
    }

    public FundsConfirmationResponse(ErrorHolder errorHolder) {
        this.errorHolder = errorHolder;
    }

    public boolean hasError() {
        return errorHolder != null;
    }
}
