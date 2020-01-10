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
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;

@Slf4j
@Service
public class ScaApproachResolver {
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final Xs2aAisConsentService xs2aAisConsentService;
    private final PisAuthorisationService pisAuthorisationService;
    private final AspspProfileService aspspProfileService;
    private final RequestProviderService requestProviderService;

    public ScaApproachResolver(ServiceTypeDiscoveryService serviceTypeDiscoveryService,
                               @Lazy Xs2aAisConsentService xs2aAisConsentService,
                               @Lazy PisAuthorisationService pisAuthorisationService,
                               AspspProfileService aspspProfileService,
                               RequestProviderService requestProviderService) {
        this.serviceTypeDiscoveryService = serviceTypeDiscoveryService;
        this.xs2aAisConsentService = xs2aAisConsentService;
        this.pisAuthorisationService = pisAuthorisationService;
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
        List<ScaApproach> scaApproaches = aspspProfileService.getScaApproaches();
        ScaApproach firstScaApproach = getFirst(scaApproaches);
        Optional<Boolean> tppRedirectPreferredOptional = requestProviderService.resolveTppRedirectPreferred();
        if (!tppRedirectPreferredOptional.isPresent()) {
            return firstScaApproach;
        }

        boolean tppRedirectPreferred = tppRedirectPreferredOptional.get();
        if (tppRedirectPreferred && scaApproaches.contains(REDIRECT)) {
            return REDIRECT;
        }

        if (!tppRedirectPreferred
                && REDIRECT == firstScaApproach
                && scaApproaches.size() > 1) {
            return getSecond(scaApproaches);
        }

        return firstScaApproach;
    }

    /**
     * Gets SCA approach from the existing initiation authorisation
     *
     * @param authorisationId authorisation identifier
     * @return SCA approach, stored in the authorisation
     */
    @NotNull
    public ScaApproach getInitiationScaApproach(@NotNull String authorisationId) {
        return resolveScaApproach(authorisationId, PaymentAuthorisationType.CREATED);
    }

    /**
     * Gets SCA approach from the existing cancellation authorisation
     *
     * @param authorisationId authorisation identifier
     * @return SCA approach, stored in the authorisation
     */
    @NotNull
    public ScaApproach getCancellationScaApproach(@NotNull String authorisationId) {
        return resolveScaApproach(authorisationId, PaymentAuthorisationType.CANCELLED);
    }

    @NotNull
    private ScaApproach resolveScaApproach(@NotNull String authorisationId, PaymentAuthorisationType authorisationType) {
        Optional<AuthorisationScaApproachResponse> scaApproachResponse = Optional.empty();
        ServiceType serviceType = serviceTypeDiscoveryService.getServiceType();
        if (serviceType == ServiceType.AIS) {
            scaApproachResponse = xs2aAisConsentService.getAuthorisationScaApproach(authorisationId);
        } else if (serviceType == ServiceType.PIS) {
            scaApproachResponse = pisAuthorisationService.getAuthorisationScaApproach(authorisationId, authorisationType);
        }

        if (!scaApproachResponse.isPresent()) {
            log.info("Couldn't retrieve SCA approach from the authorisation with id: {} and type: {}",
                     authorisationId, authorisationType);
            throw new IllegalArgumentException("Wrong authorisation id: " + authorisationId +
                                                   " or type: " + authorisationType);
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
