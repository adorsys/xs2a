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

package de.adorsys.aspsp.xs2a.spi.impl.service;

import de.adorsys.aspsp.xs2a.spi.config.keycloak.BearerToken;
import de.adorsys.aspsp.xs2a.spi.config.keycloak.KeycloakConfigProperties;
import de.adorsys.aspsp.xs2a.spi.domain.SpiAspspAuthorisationData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.spi.config.keycloak.AuthorizationConstant.*;

@Service
@RequiredArgsConstructor
public class KeycloakInvokerService {
    @Qualifier("keycloakRestTemplate")
    private final RestTemplate keycloakRestTemplate;
    private final KeycloakConfigProperties keycloakConfig;

    @Value("${keycloak-username}")
    private String keycloakUsername;
    @Value("${keycloak-password}")
    private String keycloakPassword;

    @Bean
    @RequestScope
    public BearerToken getBearerToken() {
        return new BearerToken(getAccessToken());
    }

    private String getAccessToken() {
        return obtainAuthorisationData()
                   .map(SpiAspspAuthorisationData::getAccessToken)
                   .map(t -> AUTHORIZATION_HEADER + ": " + BEARER_TOKEN_PREFIX + t)
                   .orElseThrow(IllegalArgumentException::new);
    }

    private Optional<SpiAspspAuthorisationData> obtainAuthorisationData() {
        return doObtainAccessToken(keycloakUsername, keycloakPassword);
    }

    public Optional<SpiAspspAuthorisationData> obtainAuthorisationData(String psuId, String password) {
        return doObtainAccessToken(psuId, password);
    }

    private Optional<SpiAspspAuthorisationData> doObtainAccessToken(String psuId, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", keycloakConfig.getResource());
        params.add("client_secret", keycloakConfig.getCredentials().getSecret());
        params.add("username", psuId);
        params.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<HashMap<String, String>> response = keycloakRestTemplate.exchange(keycloakConfig.getRootPath() + "/protocol/openid-connect/token", HttpMethod.POST, new HttpEntity<>(params, headers),
            new ParameterizedTypeReference<HashMap<String, String>>() {
            });
        if (response.getStatusCode() != HttpStatus.OK) {
            return Optional.empty();
        }
        return Optional.ofNullable(response.getBody())
                   .filter(body -> StringUtils.isNotBlank(body.get(ACCESS_TOKEN)) || StringUtils.isNotBlank(body.get(REFRESH_TOKEN)))
                   .map(body -> new SpiAspspAuthorisationData(psuId, password, body.get(ACCESS_TOKEN), body.get(REFRESH_TOKEN)));
    }
}
