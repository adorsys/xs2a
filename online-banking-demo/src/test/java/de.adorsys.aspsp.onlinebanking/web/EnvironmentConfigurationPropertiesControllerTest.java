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

package de.adorsys.aspsp.onlinebanking.web;

import de.adorsys.aspsp.onlinebanking.config.EnvironmentConfigurationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EnvironmentConfigurationPropertiesControllerTest {

    @Value("${environment.mockServerUrl}")
    private String mockServerUrl;
    @Value("${environment.xs2aServerUrl}")
    private String xs2aServerUrl;
    @Value("${environment.keycloakConfig.url}")
    private String url;
    @Value("${environment.keycloakConfig.realm}")
    private String realm;
    @Value("${environment.keycloakConfig.clientId}")
    private String clientId;

    @Autowired
    private EnvironmentConfigurationPropertiesController configurationPropertiesController;

    @Test
    public void getEnvironmentConfigurationProperties() {
        //Given
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When
        ResponseEntity<EnvironmentConfigurationProperties> configurationPropertiesResponse = configurationPropertiesController.getEnvironmentConfigurationProperties();

        //Then
        assertThat(configurationPropertiesResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(configurationPropertiesResponse.getBody()).isEqualTo(buildEnvironmentConfigurationProperties());
    }

    private EnvironmentConfigurationProperties buildEnvironmentConfigurationProperties() {
        EnvironmentConfigurationProperties.KeycloakConfig keycloakConfig = new EnvironmentConfigurationProperties.KeycloakConfig();
        keycloakConfig.setUrl(url);
        keycloakConfig.setRealm(realm);
        keycloakConfig.setClientId(clientId);

        EnvironmentConfigurationProperties configurationProperties = new EnvironmentConfigurationProperties();
        configurationProperties.setMockServerUrl(mockServerUrl);
        configurationProperties.setXs2aServerUrl(xs2aServerUrl);
        configurationProperties.setKeycloakConfig(keycloakConfig);
        return configurationProperties;
    }
}
