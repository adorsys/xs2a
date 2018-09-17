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

import de.adorsys.aspsp.xs2a.domain.security.AuthorisationData;
import de.adorsys.aspsp.xs2a.spi.config.keycloak.BearerToken;
import de.adorsys.aspsp.xs2a.spi.config.keycloak.KeycloakConfigProperties;
import de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant;
import de.adorsys.aspsp.xs2a.spi.domain.constant.GrantType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeycloakInvokerService {
    private final BearerToken bearerToken;
    private final KeycloakConfigProperties keycloakConfig;
    @Qualifier("keycloakRestTemplate")
    private final RestTemplate keycloakRestTemplate;

    @Value("${keycloak-username}")
    private String keycloakUsername;
    @Value("${keycloak-password}")
    private String keycloakPassword;

    // TODO move the user authorisation logic to AspspConsentData https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/297
    public Optional<AuthorisationData> obtainAuthorisationData(String userName, String password) {
        return doObtainAccessToken(userName, password);
    }

    public Optional<AuthorisationData> obtainAuthorisationData() {
        return doObtainAccessToken(keycloakUsername, keycloakPassword);
    }

    public Optional<AuthorisationData> doUpdateAccessToken(AuthorisationData authorisationData) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", GrantType.REFRESH_TOKEN.getValue());
        params.add("client_id", keycloakConfig.getResource());
        params.add("client_secret", keycloakConfig.getCredentials().getSecret());
        params.add(GrantType.REFRESH_TOKEN.getValue(), authorisationData.getRefreshToken());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<HashMap<String, String>> response = keycloakRestTemplate.exchange(keycloakConfig.getRootPath() + "/protocol/openid-connect/token", HttpMethod.POST, new HttpEntity<>(params, headers),
            new ParameterizedTypeReference<HashMap<String, String>>() {
            });

        return Optional.ofNullable(response.getBody())
                   .map(body -> new AuthorisationData(body.get(AuthorizationConstant.ACCESS_TOKEN)));
    }

    private Optional<AuthorisationData> doObtainAccessToken(String userName, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", GrantType.PASSWORD.getValue());
        params.add("client_id", keycloakConfig.getResource());
        params.add("client_secret", keycloakConfig.getCredentials().getSecret());
        params.add("username", userName);
        params.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<HashMap<String, String>> response = keycloakRestTemplate.exchange(keycloakConfig.getRootPath() + "/protocol/openid-connect/token", HttpMethod.POST, new HttpEntity<>(params, headers),
            new ParameterizedTypeReference<HashMap<String, String>>() {
            });

        return Optional.ofNullable(response.getBody())
                   .map(body -> new AuthorisationData(userName, password, body.get(AuthorizationConstant.ACCESS_TOKEN), body.get(AuthorizationConstant.REFRESH_TOKEN)));
    }
}
