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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TppRoleAccess {
    private static final AntPathMatcher matcher = new AntPathMatcher();
    private static final Map<String, Set<TppRole>> secureURIs = new HashMap<>();

    static {
        linkTppRolePatterns(ACCOUNTS_PATH, TppRole.AISP);
        linkTppRolePatterns(BENEFICIARIES_PATH, TppRole.AISP);
        linkTppRolePatterns(CARD_ACCOUNTS_PATH, TppRole.AISP);
        linkTppRolePatterns(CONSENTS_PATH, TppRole.AISP);
        linkTppRolePatterns(BULK_PAYMENTS_PATH, TppRole.PISP);
        linkTppRolePatterns(SINGLE_PAYMENTS_PATH, TppRole.PISP);
        linkTppRolePatterns(PERIODIC_PAYMENTS_PATH, TppRole.PISP);
        linkTppRolePatterns(FUNDS_CONFIRMATION_PATH, TppRole.PIISP);
        linkTppRolePatterns(CONSENTS_V2_PATH, TppRole.PIISP);
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

    static void linkTppRolePatterns(String pattern, TppRole... tppRoles) {
        Assert.notEmpty(tppRoles, "Tpp roles must be set!");

        Set<TppRole> roles = Arrays.stream(tppRoles)
                                 .collect(Collectors.toSet());

        if (secureURIs.containsKey(pattern)) {
            secureURIs.get(pattern)
                .addAll(roles);
        } else {
            secureURIs.put(pattern, roles);
        }
    }
}
