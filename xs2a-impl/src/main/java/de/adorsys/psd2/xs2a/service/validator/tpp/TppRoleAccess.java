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
    private static Map<String, Set<TppRole>> secureURIs;

    static {
        TppRoleAccess.builder()
            .linkTppRolePatterns("/api/v1/accounts/**", TppRole.AISP)
            .linkTppRolePatterns("/v1/accounts/**", TppRole.AISP)
            .linkTppRolePatterns("/api/v1/consents/**", TppRole.AISP)
            .linkTppRolePatterns("/v1/consents/**", TppRole.AISP)
            .linkTppRolePatterns("/api/v1/bulk-payments/**", TppRole.PISP)
            .linkTppRolePatterns("/v1/bulk-payments/**", TppRole.PISP)
            .linkTppRolePatterns("/api/v1/payments/**", TppRole.PISP)
            .linkTppRolePatterns("/v1/payments/**", TppRole.PISP)
            .linkTppRolePatterns("/api/v1/periodic-payments/**", TppRole.PISP)
            .linkTppRolePatterns("/v1/periodic-payments/**", TppRole.PISP)
            .linkTppRolePatterns("/api/v1/funds-confirmations/**", TppRole.PIISP)
            .linkTppRolePatterns("/v1/funds-confirmations/**", TppRole.PIISP)
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
        for (Map.Entry<String, Set<TppRole>> entry : secureURIs.entrySet()) {
            Set<TppRole> allowedRoles = entry.getValue();
            String pattern = entry.getKey();
            if (matcher.match(pattern, targetPath)) {
                for (TppRole role : tppRoles) {
                    if (allowedRoles.contains(role)) {
                        return true;
                    }
                }
                return false;
            }
        }
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
