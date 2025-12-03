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

package de.adorsys.psd2.xs2a.service.vop.read;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.vop.CommonVop;
import de.adorsys.psd2.xs2a.domain.vop.GetCommonVopResponse;

/**
 * Service interface for reading VOP (Verification of Payee) results.
 *
 * <p>This interface defines the contract for retrieving VOP verification results
 * from the system. It follows the Interface Segregation Principle by providing
 * a focused, single-purpose interface for VOP read operations.</p>
 *
 * <p>Implementations of this interface should handle:</p>
 * <ul>
 *   <li>Retrieval of VOP verification results by VOP ID</li>
 *   <li>Error handling and appropriate error response wrapping</li>
 *   <li>Validation of VOP ID parameter</li>
 * </ul>
 *
 * @see ResponseObject
 * @see CommonVop
 */
public interface ReadVopService {

    /**
     * Retrieves the VOP verification result for the given VOP ID.
     *
     * <p>This method fetches the complete VOP verification result information,
     * including verification status, matched account details, and any associated
     * error information if the verification failed or the VOP was not found.</p>
     *
     * @param vopId the unique identifier of the VOP verification request.
     *              Must not be null or empty.
     * @return a {@link ResponseObject} containing either the VOP result
     * or error information if the retrieval failed
     * @throws IllegalArgumentException if vopId is null or empty
     */
    ResponseObject<GetCommonVopResponse> getVopResult(String vopId);
}
