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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Value
public class TppRoleAccess {
    private static AntPathMatcher matcher = new AntPathMatcher();
    private static Map<TppRole, Set<String>> secureURIs;

    static {
        TppRoleAccess.builder()
            .linkTppRolePatterns(TppRole.AISP,
                "/api/v1/accounts/**",
                "/v1/accounts/**",
                "/api/v1/consents/**",
                "/v1/consents/**")
            .linkTppRolePatterns(TppRole.PISP,
                "/api/v1/bulk-payments/**",
                "/v1/bulk-payments/**",
                "/api/v1/payments/**",
                "/v1/payments/**",
                "/api/v1/periodic-payments/**",
                "/v1/periodic-payments/**")
            .linkTppRolePatterns(TppRole.PIISP,
                "/api/v1/funds-confirmations/**",
                "/v1/funds-confirmations/**")
            .build();
    }

    private TppRoleAccess(TppAccessBuilder builder) {
        secureURIs = builder.secureURIs;
    }

    private static TppAccessBuilder builder() {
        return new TppAccessBuilder();
    }

    public static boolean hasAccessForPath(List<TppRole> tppRoles, String targetPath) {
        if (CollectionUtils.isEmpty(tppRoles)) {
            return false;
        }
        for (Map.Entry<TppRole, Set<String>> entry : secureURIs.entrySet()) {
            Set<String> patterns = entry.getValue();
            TppRole role = entry.getKey();
            for (String pattern : patterns) {
                if (matcher.match(pattern, targetPath) && !tppRoles.contains(role)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Value
    private static class TppAccessBuilder {
        private Map<TppRole, Set<String>> secureURIs = new HashMap<>();

        private TppAccessBuilder() {
        }

        TppAccessBuilder linkTppRolePatterns(TppRole tppRole, String... patterns) {
            Assert.notEmpty(patterns, "Patterns must be set!");

            Set<String> requestMatchers = Arrays.stream(patterns)
                                              .collect(Collectors.toSet());

            if (this.secureURIs.containsKey(tppRole)) {
                this.secureURIs.get(tppRole)
                    .addAll(requestMatchers);
            } else {
                this.secureURIs.put(tppRole, requestMatchers);
            }
            return this;
        }

        private TppRoleAccess build() {
            return new TppRoleAccess(this);
        }
    }
}
