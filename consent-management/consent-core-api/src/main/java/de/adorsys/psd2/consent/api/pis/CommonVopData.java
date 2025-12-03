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

package de.adorsys.psd2.consent.api.pis;

import java.time.OffsetDateTime;

/**
 * Common VOP data interface containing shared fields for all VOP types.
 * This interface provides a contract for accessing VOP (Verification of Payee) related data
 * in the consent management layer.
 */
public interface CommonVopData {
    
    /**
     * Returns the unique VOP identifier.
     *
     * @return VOP ID as String
     */
    String getVopId();

    /**
     * Returns the VOP data in byte array format.
     * This contains the serialized VOP request/response data.
     *
     * @return VOP data as byte array
     */
    byte[] getVopData();

    /**
     * Returns the timestamp when the VOP record was created.
     *
     * @return creation timestamp
     */
    OffsetDateTime getCreationTimestamp();

    /**
     * Returns the content type of the VOP data.
     * Typically indicates the format of the serialized data (e.g., "application/json").
     *
     * @return content type as String
     */
    String getContentType();

    /**
     * Returns the instance ID identifying the XS2A instance that processed this VOP.
     * Used in multi-instance deployments to track which instance handled the request.
     *
     * @return instance ID as String
     */
    String getInstanceId();
}
