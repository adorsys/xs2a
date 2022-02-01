/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.api.ais;

import de.adorsys.psd2.consent.api.ActionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AisConsentActionRequest {
    private String tppId;
    private String consentId;
    private ActionStatus actionStatus;
    private String requestUri;
    private boolean updateUsage;

    // These 2 parameters are optional. Are used while storing data about consent usage in case or request URI has resource
    // ID and/or transaction ID in path parameters.
    private String resourceId;
    private String transactionId;
}
