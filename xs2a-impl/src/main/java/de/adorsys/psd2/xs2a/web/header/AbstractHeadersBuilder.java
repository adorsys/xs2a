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

package de.adorsys.psd2.xs2a.web.header;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public abstract class AbstractHeadersBuilder {
    protected ScaApproachResolver scaApproachResolver;

    /**
     * Builds response headers for successful start payment authorisation request
     *
     * @param authorisationId id of the created authorisation
     * @return response headers
     */
    public ResponseHeaders buildStartAuthorisationHeaders(@NotNull String authorisationId) {
        return buildHeadersForExistingAuthorisation(authorisationId);
    }

    /**
     * Builds response headers for successful update payment initiation PSU Data request
     *
     * @param authorisationId id of the authorisation, used in the request
     * @return response headers
     */
    public ResponseHeaders buildUpdatePsuDataHeaders(@NotNull String authorisationId) {
        return buildHeadersForExistingAuthorisation(authorisationId);
    }

    private ResponseHeaders buildHeadersForExistingAuthorisation(String authorisationId) {
        ScaApproach authorisationScaApproach = scaApproachResolver.getScaApproach(authorisationId);
        return buildScaApproachHeader(authorisationScaApproach);
    }

    private ResponseHeaders buildScaApproachHeader(ScaApproach scaApproach) {
        return ResponseHeaders.builder()
                   .aspspScaApproach(scaApproach)
                   .build();
    }
}
