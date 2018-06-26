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

import de.adorsys.aspsp.xs2a.config.KeycloakConfigProperties;
import de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant.BEARER_TOKEN_PREFIX;

@Service
public class KeycloackInvokerService {
    @Autowired
    private KeycloakConfigProperties keycloakConfig;
    @Autowired
    @Qualifier("keycloackRestTemplate")
    private RestTemplate keycloackRestTemplate;

    public String obtainAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", "aspsp");
        map.add("password", "zzz");
        map.add("client_id", keycloakConfig.getResource());
        map.add("client_secret", keycloakConfig.getCredentials().getSecret());

        ResponseEntity<HashMap<String, String>> response = keycloackRestTemplate.exchange(keycloakConfig.getRootPath() + "/protocol/openid-connect/token", HttpMethod.POST, new HttpEntity<>(map, headers),
            new ParameterizedTypeReference<HashMap<String, String>>() {
            });

        String accessToken = null;
        if(response.getBody() != null){
            accessToken = AuthorizationConstant.AUTHORIZATION_HEADER + ": " + BEARER_TOKEN_PREFIX + response.getBody()
                                                                                                        .get(AuthorizationConstant.ACCESS_TOKEN);
        }
        return accessToken;
    }
}
