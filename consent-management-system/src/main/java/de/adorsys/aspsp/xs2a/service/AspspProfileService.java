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

package de.adorsys.aspsp.xs2a.service;


import de.adorsys.aspsp.xs2a.config.rest.AspspProfileRemoteUrls;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Log4j
@Service
@RequiredArgsConstructor
public class AspspProfileService {
    @Qualifier("aspspProfileRestTemplate")
    private final RestTemplate aspspProfileRestTemplate;
    private final AspspProfileRemoteUrls aspspProfileRemoteUrls;

    public int getMinFrequencyPerDay(int tppFrequency) {
        return Math.min(Math.abs(tppFrequency), getFrequencyPerDay());
    }

    private Integer getFrequencyPerDay() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getFrequencyPerDay(), HttpMethod.GET, null, Integer.class).getBody();
    }
}
