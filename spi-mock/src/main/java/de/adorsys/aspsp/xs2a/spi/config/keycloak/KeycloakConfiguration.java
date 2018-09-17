/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.spi.config.keycloak;

import de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import java.util.EnumSet;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach.EMBEDDED;
import static de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach.REDIRECT;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfiguration {
    private final AspspProfileService aspspProfileService;
    private final KeycloakInvokerService keycloakInvokerService;

    @Bean
    @RequestScope
    public BearerToken getBearerToken() {
        return new BearerToken(getAccessToken());
    }

    private String getAccessToken() {
        ScaApproach scaApproach = getScaApproach();
        String accessToken = null;
        if (EnumSet.of(REDIRECT, EMBEDDED).contains(scaApproach)) {
            accessToken = keycloakInvokerService.obtainAuthorisationData();
        }
        return Optional.ofNullable(accessToken)
                   .orElseThrow(IllegalArgumentException::new);
    }

    private ScaApproach getScaApproach() {
        return aspspProfileService.getScaApproach();
    }
}
