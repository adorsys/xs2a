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

package de.adorsys.psd2.xs2a.service.vop.status;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.vop.GetCommonVopResponse;
import de.adorsys.psd2.xs2a.domain.vop.GetCommonVopStatusResponse;

/**
 * Service interface for retrieving the status of individual party verification operations (VOP).
 * <p>
 * This service is responsible for querying and returning the current status of a single
 * party verification request identified by its unique VOP ID. It complements the bulk
 * verification status service by providing status information for individual verifications.
 * </p>
 *
 * @see ResponseObject
 * @see de.adorsys.psd2.xs2a.service.vop.read.ReadBulkVopService
 */
public interface ReadVopStatusService {

    /**
     * Retrieves the current status of a party verification operation.
     * <p>
     * This method queries the verification status for a single VOP identified by the provided
     * VOP ID. The response includes the current verification state and relevant metadata.
     * </p>
     *
     * @param vopId the unique identifier of the verification operation (VOP). Must not be null.
     * @return a {@link ResponseObject} containing:
     *         <ul>
     *           <li>On success: {@link GetCommonVopResponse} with the current VOP status and details</li>
     *           <li>On failure: appropriate error information (e.g., VOP not found, invalid ID format)</li>
     *         </ul>
     * @throws IllegalArgumentException if vopId is null or empty
     */
    ResponseObject<GetCommonVopStatusResponse> getVopStatus(String vopId);
}
