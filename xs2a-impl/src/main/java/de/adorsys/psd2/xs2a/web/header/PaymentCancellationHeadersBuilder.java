/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCancellationHeadersBuilder {
    private final ScaApproachResolver scaApproachResolver;

    /**
     * Builds response headers for successful start payment cancellation authorisation request
     *
     * @param authorisationId id of the created cancellation authorisation
     * @return response headers
     */
    public ResponseHeaders buildStartPaymentCancellationAuthorisationHeaders(@NotNull String authorisationId) {
        return buildHeadersForExistingAuthorisation(authorisationId);
    }

    /**
     * Builds response headers for start payment cancellation authorisation request that resulted in some error
     *
     * @return response headers
     */
    public ResponseHeaders buildErrorStartPaymentCancellationAuthorisationHeaders() {
        return buildScaApproachHeader(scaApproachResolver.resolveScaApproach());
    }

    /**
     * Builds response headers for successful update payment cancellation PSU Data request
     *
     * @param authorisationId id of the cancellation authorisation, used in the request
     * @return response headers
     */
    public ResponseHeaders buildUpdatePaymentCancellationPsuDataHeaders(@NotNull String authorisationId) {
        return buildHeadersForExistingAuthorisation(authorisationId);
    }

    private ResponseHeaders buildHeadersForExistingAuthorisation(String authorisationId) {
        ScaApproach authorisationScaApproach = scaApproachResolver.getCancellationScaApproach(authorisationId);
        return buildScaApproachHeader(authorisationScaApproach);
    }

    private ResponseHeaders buildScaApproachHeader(ScaApproach scaApproach) {
        return ResponseHeaders.builder()
                   .aspspScaApproach(scaApproach)
                   .build();
    }
}
