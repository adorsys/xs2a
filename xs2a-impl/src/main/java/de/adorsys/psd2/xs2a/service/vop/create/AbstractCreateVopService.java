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

import java.time.OffsetDateTime;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.vop.CommonVop;
import de.adorsys.psd2.xs2a.domain.vop.VopInitiationParameters;
import de.adorsys.psd2.xs2a.domain.vop.VopInitiationResponse;
import de.adorsys.psd2.xs2a.service.vop.create.spi.VopInitiationService;
import lombok.RequiredArgsConstructor;

/**
 * Abstract base class for VOP (Verification of Payee) creation services.
 * Provides common functionality for creating different types of VOP verifications.
 *
 * @param <V> the VOP type
 * @param <S> the VOP initiation service type
 */
@RequiredArgsConstructor
public abstract class AbstractCreateVopService<V extends CommonVop, S extends VopInitiationService<V>> implements CreateVopService {

    /**
     * Service for performing VOP initiation.
     */
    protected final S vopInitiationService;

    @Override
    public ResponseObject<VopInitiationResponse> createVerification(byte[] vop, VopInitiationParameters params) {

        V vopRequest = getVopRequest(vop, params);
        OffsetDateTime creationTimestamp = OffsetDateTime.now();
        vopRequest.setCreationTimestamp(creationTimestamp);
        vopRequest.setInstanceId(params.getInstanceId());

        String endpoint = (params.getPartyVerificationEndpoint() == null)
                                    ? params.getBulkPartyVerificationEndpoint()
                                    : params.getPartyVerificationEndpoint();

        VopInitiationResponse response = vopInitiationService.performVerification(vopRequest, endpoint);

        if (response.hasError()) {
            return buildErrorResponse(response.getErrorHolder());
        }

        return ResponseObject.<VopInitiationResponse>builder()
                   .body(response)
                   .build();
    }

    /**
     * Creates a VOP request object from raw VOP data.
     *
     * @param vop the raw VOP data bytes
     * @param params the VOP initiation parameters
     * @return the VOP request object
     */
    protected abstract V getVopRequest(byte[] vop, VopInitiationParameters params);
}
