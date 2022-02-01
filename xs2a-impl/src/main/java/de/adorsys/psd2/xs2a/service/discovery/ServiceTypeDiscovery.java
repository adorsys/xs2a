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

package de.adorsys.psd2.xs2a.service.discovery;

import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant.*;
import static de.adorsys.psd2.xs2a.core.mapper.ServiceType.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ServiceTypeDiscovery {
    private static final AntPathMatcher matcher = new AntPathMatcher();
    private static final Map<String, ServiceType> pathToServiceType;

    static {
        pathToServiceType = new HashMap<>();
        pathToServiceType.put(ACCOUNTS_PATH, AIS);
        pathToServiceType.put(BENEFICIARIES_PATH, AIS);
        pathToServiceType.put(CARD_ACCOUNTS_PATH, AIS);
        pathToServiceType.put(CONSENTS_PATH, AIS);
        pathToServiceType.put(SINGLE_PAYMENTS_PATH, PIS);
        pathToServiceType.put(PERIODIC_PAYMENTS_PATH, PIS);
        pathToServiceType.put(BULK_PAYMENTS_PATH, PIS);
        pathToServiceType.put(FUNDS_CONFIRMATION_PATH, PIIS);
        pathToServiceType.put(CONSENTS_V2_PATH, PIIS);
        pathToServiceType.put(SIGNING_BASKETS_PATH, SB);
    }

    /**
     * Returns service type by checking incoming path on existing paths patterns matching (each pattern is associated with corresponding service type).
     *
     * @param targetPath target path to be checked on pattern matching
     * @return Service Type value
     */
    static ServiceType getServiceType(String targetPath) {
        for (Map.Entry<String, ServiceType> entry : pathToServiceType.entrySet()) {
            String pattern = entry.getKey();

            if (matcher.match(pattern, targetPath)) {
                return entry.getValue();
            }
        }

        log.warn("Can't get ServiceType because illegal path: [{}]", targetPath);
        throw new IllegalArgumentException("Illegal path: " + targetPath);
    }
}
