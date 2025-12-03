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

package de.adorsys.psd2.xs2a.service.vop.create;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.vop.VopInitiationParameters;
import de.adorsys.psd2.xs2a.domain.vop.VopInitiationResponse;

public interface CreateVopService {
    /**
     * Creates a VOP (Verification of Payee) verification.
     *
     * @param vop the VOP data bytes
     * @param vopInitiationParameters the VOP initiation parameters
     * @return response object containing the verification result
     */
    ResponseObject<VopInitiationResponse> createVerification(byte[] vop, VopInitiationParameters vopInitiationParameters);

    default ResponseObject<VopInitiationResponse> buildErrorResponse(ErrorHolder errorHolder) {
        return ResponseObject.<VopInitiationResponse>builder()
                .fail(new MessageError(errorHolder))
                .build();
    }
}
