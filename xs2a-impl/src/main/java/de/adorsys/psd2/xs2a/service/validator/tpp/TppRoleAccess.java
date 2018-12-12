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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import lombok.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant.*;

@Value
public class TppRoleAccess {
    private static AntPathMatcher matcher = new AntPathMatcher();
    private static Map<String, Set<TppRole>> secureURIs;

    static {
        TppRoleAccess.builder()
            .linkTppRolePatterns(ACCOUNTS_PATH, TppRole.AISP)
            .linkTppRolePatterns(CONSENTS_PATH, TppRole.AISP)
            .linkTppRolePatterns(BULK_PAYMENTS_PATH, TppRole.PISP)
            .linkTppRolePatterns(SINGLE_PAYMENTS_PATH, TppRole.PISP)
            .linkTppRolePatterns(PERIODIC_PAYMENTS_PATH, TppRole.PISP)
            .linkTppRolePatterns(FUNDS_CONFIRMATION_PATH, TppRole.PIISP)
            .build();
    }

    private TppRoleAccess(TppAccessBuilder builder) {
        secureURIs = builder.secureURIs;
    }

    private static TppAccessBuilder builder() {
        return new TppAccessBuilder();
    }

    static boolean hasAccessForPath(List<TppRole> tppRoles, String targetPath) {
        for (Map.Entry<String, Set<TppRole>> entry : secureURIs.entrySet()) {
            Set<TppRole> allowedRoles = entry.getValue();
            String pattern = entry.getKey();
            if (matcher.match(pattern, targetPath)) {
                return tppRoles.stream()
                           .anyMatch(allowedRoles::contains);
            }
        }
        /* We check the TPP roles for uris matching the patterns in secureURIs map, the rest of requests are considered valid e.g. swagger-ui.html */
        return true;
    }

    @Value
    private static class TppAccessBuilder {
        private Map<String, Set<TppRole>> secureURIs = new HashMap<>();

        private TppAccessBuilder() {
        }

        TppAccessBuilder linkTppRolePatterns(String pattern, TppRole... tppRoles) {
            Assert.notEmpty(tppRoles, "Tpp roles must be set!");

            Set<TppRole> roles = Arrays.stream(tppRoles)
                                     .collect(Collectors.toSet());

            if (this.secureURIs.containsKey(pattern)) {
                this.secureURIs.get(pattern)
                    .addAll(roles);
            } else {
                this.secureURIs.put(pattern, roles);
            }
            return this;
        }

        private TppRoleAccess build() {
            return new TppRoleAccess(this);
        }
    }
}
