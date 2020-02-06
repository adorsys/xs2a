/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
