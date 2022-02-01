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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScaServiceResolver<T extends ScaApproachServiceTypeProvider> implements InitializingBean {
    private final List<T> services;
    private final ScaApproachResolver scaApproachResolver;
    private final Map<ScaApproach, T> serviceContainer = new EnumMap<>(ScaApproach.class);

    @Override
    public void afterPropertiesSet() {
        services.forEach(service -> serviceContainer.put(service.getScaApproachServiceType(), service));
    }

    /**
     * Get particular service for sca approach that was chosen in resolver
     *
     * @return particular service for chosen sca approach
     */
    public T getService() {
        return serviceContainer.get(scaApproachResolver.resolveScaApproach());
    }

    /**
     * Get particular service for sca approach that was chosen in resolver, works for authorisation initiation
     *
     * @param authorisationId ID of Authorisation process
     * @return particular service for chosen sca approach
     */
    public T getService(String authorisationId) {
        return serviceContainer.get(scaApproachResolver.getScaApproach(authorisationId));
    }
}
