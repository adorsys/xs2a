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

package de.adorsys.psd2.aspsp.profile.domain.vop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for VOP API reference information.
 * Contains the reference API details that will be included in VOP response headers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VopxReference {

    /**
     * X-Reference-API-Name header value for VOP API
     */
    private String apiName;

    /**
     * X-Reference-API-Document header value for VOP API
     */
    private String apiDocument;

    /**
     * X-Reference-API-Version header value for VOP API
     */
    private String apiVersion;
}
