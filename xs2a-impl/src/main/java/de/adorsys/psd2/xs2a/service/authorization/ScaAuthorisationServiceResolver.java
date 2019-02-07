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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScaAuthorisationServiceResolver<T extends ScaApproachServiceTypeProvider> implements InitializingBean {
    private final List<T> services;
    private final ScaApproachResolver scaApproachResolver;
    private final Map<ScaApproach, T> SERVICE_CONTAINER = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        services.forEach(service -> SERVICE_CONTAINER.put(service.getScaApproachServiceType(), service));
    }

    /**
     * Get authorisation service for sca approach that was chosen in resolver
     *
     * @return authorisation service for chosen sca approach
     */
    public T getService() {
        return SERVICE_CONTAINER.get(scaApproachResolver.resolveScaApproach());
    }
}
