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

package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.config.AspspProfileRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.service.AspspProfileSpi;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@AllArgsConstructor
public class AspspProfileSpiImpl implements AspspProfileSpi {
    private final RestTemplate aspspProfileRestTemplate;
    private final AspspProfileRemoteUrls aspspProfileRemoteUrls;

    @Override
    public List<String> getAvailablePaymentProducts() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getAvailablePaymentProducts(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
            }).getBody();
    }

    @Override
    public List<String> getAvailablePaymentTypes() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getAvailablePaymentTypes(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
            }).getBody();
    }

    @Override
    public Integer getFrequencyPerDay() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getFrequencyPerDay(), HttpMethod.GET, null, Integer.class).getBody();
    }
}
