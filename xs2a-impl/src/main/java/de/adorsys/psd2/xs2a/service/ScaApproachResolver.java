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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.ScaApproachHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.DECOUPLED;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;

@Service
@RequiredArgsConstructor
public class ScaApproachResolver {
    private final AspspProfileService aspspProfileService;
    private final RequestProviderService requestProviderService;
    private final ScaApproachHolder scaApproachHolder;

    /**
     * Resolve which sca approach from sca approaches list in ASPSP-profile should be used for authorisation.
     * If SCA approach was forced beforehand, it will be returned instead.
     * If header "tpp-redirect-preferred" is provided with value "true", Redirect approach will be used,
     * otherwise the first approach from the list will be chosen. If ASPSP has only one SCA approach in profile, header "tpp-redirect-preferred" will be ignored
     * and only approach from profile will be used
     *
     * @return chosen ScaApproach to be used for authorisation
     */
    public ScaApproach resolveScaApproach() {
        if (scaApproachHolder.isNotEmpty()) {
            return scaApproachHolder.getScaApproach();
        }

        boolean tppRedirectPreferred = requestProviderService.resolveTppRedirectPreferred();
        List<ScaApproach> scaApproaches = aspspProfileService.getScaApproaches();

        if (tppRedirectPreferred && scaApproaches.contains(REDIRECT)) {
            return REDIRECT;
        }
        return getFirst(scaApproaches);
    }

    /**
     * Forcefully sets current SCA approach to <code>DECOUPLED</code>.
     * Should ONLY be used for switching from Embedded to Decoupled approach during SCA method selection
     */
    public void forceDecoupledScaApproach() {
        scaApproachHolder.setScaApproach(DECOUPLED);
    }

    private ScaApproach getFirst(List<ScaApproach> scaApproaches) {
        return scaApproaches.get(0);
    }
}
