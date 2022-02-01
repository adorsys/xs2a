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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.AspspProfileRemoteUrls;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
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
        return BooleanUtils.isTrue(aspspProfileRestTemplate.getForObject(aspspProfileRemoteUrls.isMultitenancyEnabled(), Boolean.class));
    }
}
