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

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.xs2a.service.RequestProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkExtractor {
    private static final String HREF = "href";

    private final RequestProviderService requestProviderService;

    /**
     * Extracts value of some specific link from the links map
     *
     * @param linksMap links map from the response object
     * @param linkName name of the link to be extracted
     * @return self link if it's present
     */
    public Optional<String> extract(Map<?, ?> linksMap, @NotNull String linkName) {
        if (MapUtils.isEmpty(linksMap)) {
            log.info("X-Request-ID: [{}]. Couldn't extract the {} link from the  link map: map with links is null or empty",
                     requestProviderService.getRequestId(), linkName);
            return Optional.empty();
        }

        Object linkObject = linksMap.get(linkName);

        if (isNotInstanceOfMap(linkObject)) {
            log.info("X-Request-ID: [{}]. Couldn't extract the {} link from the response: link is null or is not wrapped in the Map object",
                     requestProviderService.getRequestId(), linkName);
            return Optional.empty();
        }

        return Optional.ofNullable(((Map) linkObject).get(HREF))
                   .map(Object::toString);
    }

    private boolean isNotInstanceOfMap(Object object) {
        return !(object instanceof Map);
    }
}
