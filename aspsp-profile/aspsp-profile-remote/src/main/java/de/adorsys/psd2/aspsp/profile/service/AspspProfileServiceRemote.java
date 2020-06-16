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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.AspspProfileRemoteUrls;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AspspProfileServiceRemote implements AspspProfileService {
    @Qualifier("aspspProfileRestTemplate")
    private final RestTemplate aspspProfileRestTemplate;
    private final AspspProfileRemoteUrls aspspProfileRemoteUrls;

    @Override
    public AspspSettings getAspspSettings(String instanceId) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Instance-ID", instanceId);
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getAspspSettings(), HttpMethod.GET, new HttpEntity<>(headers), AspspSettings.class).getBody();
    }

    @Override
    public List<ScaApproach> getScaApproaches(String instanceId) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Instance-ID", instanceId);
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getScaApproaches(), HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<ScaApproach>>() {
            }).getBody();
    }

    @Override
    public boolean isMultitenancyEnabled() {
        return aspspProfileRestTemplate.getForObject(aspspProfileRemoteUrls.isMultitenancyEnabled(), Boolean.class);
    }
}
