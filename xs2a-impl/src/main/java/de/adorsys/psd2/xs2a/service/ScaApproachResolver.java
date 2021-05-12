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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

@Slf4j
@Service
public class ScaApproachResolver {
    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final AspspProfileService aspspProfileService;
    private final RequestProviderService requestProviderService;

    public ScaApproachResolver(Xs2aAuthorisationService xs2aAuthorisationService,
                               AspspProfileService aspspProfileService,
                               RequestProviderService requestProviderService) {
        this.xs2aAuthorisationService = xs2aAuthorisationService;
        this.aspspProfileService = aspspProfileService;
        this.requestProviderService = requestProviderService;
    }

    /**
     * Resolve which sca approach from sca approaches list in ASPSP-profile should be used for authorisation.
     * <p>
     * If header "tpp-redirect-preferred" is provided with value "true" and ASPSP supports Redirect approach, then this approach will be used.
     * If header "tpp-redirect-preferred" is provided with value "false", the first non-Redirect approach from the list will be used.
     * If header "tpp-redirect-preferred" is not provided, the first approach from the list will be chosen.
     * If ASPSP has only one SCA approach in profile, header "tpp-redirect-preferred" will be ignored
     * and only approach from profile will be used
     *
     * @return chosen ScaApproach to be used for authorisation
     */
    public ScaApproach resolveScaApproach() {
        Optional<Boolean> tppRedirectPreferredOptional = requestProviderService.resolveTppRedirectPreferred();
        Optional<Boolean> tppDecoupledPreferredOptional = requestProviderService.resolveTppDecoupledPreferred();

        return resolveHeaders(tppRedirectPreferredOptional, tppDecoupledPreferredOptional);
    }

    private ScaApproach resolveHeaders(Optional<Boolean> tppRedirectPreferredOptional, Optional<Boolean> tppDecoupledPreferredOptional) {
        List<ScaApproach> scaApproaches = aspspProfileService.getScaApproaches(requestProviderService.getInstanceId());
        // Both empty use first in list
        if (tppRedirectPreferredOptional.isEmpty() && tppDecoupledPreferredOptional.isEmpty()) {
            return getFirst(scaApproaches);
        }

        boolean tppRedirectPreferred = tppRedirectPreferredOptional.orElse(false);
        boolean tppDecoupledPreferred = tppDecoupledPreferredOptional.orElse(false);
        // Choose not embedded if both true
        if (tppRedirectPreferred && tppDecoupledPreferred) {
            return getScaApproachByPreferredHeadersTrue(scaApproaches);
        }

        // only redirect is true - use redirect
        if (tppRedirectPreferred && scaApproaches.contains(REDIRECT)) {
            return REDIRECT;
        }

        // only decoupled is true - use decoupled
        if (tppDecoupledPreferred && scaApproaches.contains(DECOUPLED)) {
            return DECOUPLED;
        }

        return getScaApproachByPreferredHeadersFalse(tppRedirectPreferredOptional, tppDecoupledPreferredOptional, scaApproaches);
    }

    private ScaApproach getScaApproachByPreferredHeadersFalse(Optional<Boolean> tppRedirectPreferredOptional,
                                                              Optional<Boolean> tppDecoupledPreferredOptional,
                                                              List<ScaApproach> scaApproaches) {
        ScaApproach firstScaApproach = getFirst(scaApproaches);

        // redirect empty - use decoupled
        boolean tppDecoupledPreferred = tppDecoupledPreferredOptional.orElse(false);
        boolean notDecoupled = tppRedirectPreferredOptional.isEmpty()
                                   && !tppDecoupledPreferred
                                   && DECOUPLED == firstScaApproach
                                   && scaApproaches.size() > 1;

        // decoupled empty - use redirect
        boolean tppRedirectPreferred = tppRedirectPreferredOptional.orElse(false);
        boolean notRedirect = tppDecoupledPreferredOptional.isEmpty()
                                  && !tppRedirectPreferred
                                  && REDIRECT == firstScaApproach
                                  && scaApproaches.size() > 1;
        if (notDecoupled || notRedirect) {
            return getSecond(scaApproaches);
        }

        // both false - use embedded if possible
        boolean bothNotEmpty = tppRedirectPreferredOptional.isPresent() && tppDecoupledPreferredOptional.isPresent();
        boolean bothFalse = !tppDecoupledPreferred && !tppRedirectPreferred;
        if (bothNotEmpty && bothFalse && scaApproaches.contains(EMBEDDED)) {
            return EMBEDDED;
        }

        return firstScaApproach;
    }

    private ScaApproach getScaApproachByPreferredHeadersTrue(List<ScaApproach> scaApproaches) {
        if (getFirst(scaApproaches) != EMBEDDED || scaApproaches.size() == 1) {
            return getFirst(scaApproaches);
        } else {
            return getSecond(scaApproaches);
        }
    }

    /**
     * Gets SCA approach from the existing initiation authorisation
     *
     * @param authorisationId authorisation identifier
     * @return SCA approach, stored in the authorisation
     */
    @NotNull
    public ScaApproach getScaApproach(@NotNull String authorisationId) {
        return resolveScaApproach(authorisationId);
    }

    @NotNull
    private ScaApproach resolveScaApproach(@NotNull String authorisationId) {
        Optional<AuthorisationScaApproachResponse> scaApproachResponse = xs2aAuthorisationService.getAuthorisationScaApproach(authorisationId);

        if (scaApproachResponse.isEmpty()) {
            log.info("Couldn't retrieve SCA approach from the authorisation with id: {}", authorisationId);
            throw new IllegalArgumentException("Wrong authorisation id: " + authorisationId);
        }

        return scaApproachResponse.get().getScaApproach();
    }

    private ScaApproach getFirst(List<ScaApproach> scaApproaches) {
        return scaApproaches.get(0);
    }

    private ScaApproach getSecond(List<ScaApproach> scaApproaches) {
        return scaApproaches.get(1);
    }
}
