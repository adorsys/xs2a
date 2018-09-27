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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentConfigurationPropertiesControllerTest {
    private final String MOCK_SERVER_URL = "http://localhost:28080";
    private final String XS2A_SERVER_URL = "http://localhost:8080";
    private final String ASPSP_PROFILE_SERVER_URL = "http://localhost:48080";
    private final String CONSENT_MANAGEMENT_SERVER_URL = "http://localhost:38080";
    private final String KEYCLOAK_URL = "http://localhost:8081/auth";
    private final String KEYCLOAK_REALM = "xs2a";
    private final String KEYCLOAK_CLIENT_ID = "aspsp-mock";

    @Mock
    private EnvironmentConfigurationProperties configurationProperties;
    @Mock
    private EnvironmentConfigurationProperties.KeycloakConfig keycloakConfig;
    @InjectMocks
    private EnvironmentConfigurationPropertiesController configurationPropertiesController;

    @Before
    public void setUp() {
        when(configurationProperties.getMockServerUrl())
            .thenReturn(MOCK_SERVER_URL);
        when(configurationProperties.getXs2aServerUrl())
            .thenReturn(XS2A_SERVER_URL);
        when(configurationProperties.getAspspProfileServerUrl())
            .thenReturn(ASPSP_PROFILE_SERVER_URL);
        when(configurationProperties.getConsentManagementServerUrl())
            .thenReturn(CONSENT_MANAGEMENT_SERVER_URL);
        when(configurationProperties.getKeycloakConfig())
            .thenReturn(keycloakConfig);
        when(keycloakConfig.getUrl())
            .thenReturn(KEYCLOAK_URL);
        when(keycloakConfig.getRealm())
            .thenReturn(KEYCLOAK_REALM);
        when(keycloakConfig.getClientId())
            .thenReturn(KEYCLOAK_CLIENT_ID);
    }

    @Test
    public void getEnvironmentConfigurationProperties() {
        //Given
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When
        ResponseEntity<EnvironmentConfigurationProperties> configurationPropertiesResponse = configurationPropertiesController.getEnvironmentConfigurationProperties();

        //Then
        assertThat(configurationPropertiesResponse.getStatusCode()).isEqualTo(expectedStatusCode);

        EnvironmentConfigurationProperties actualConfigurationProperties = configurationPropertiesResponse.getBody();

        assertThat(actualConfigurationProperties).isNotNull();
        assertThat(actualConfigurationProperties.getMockServerUrl()).isEqualTo(MOCK_SERVER_URL);
        assertThat(actualConfigurationProperties.getXs2aServerUrl()).isEqualTo(XS2A_SERVER_URL);
        assertThat(actualConfigurationProperties.getAspspProfileServerUrl()).isEqualTo(ASPSP_PROFILE_SERVER_URL);
        assertThat(actualConfigurationProperties.getConsentManagementServerUrl()).isEqualTo(CONSENT_MANAGEMENT_SERVER_URL);

        EnvironmentConfigurationProperties.KeycloakConfig actualKeycloackConfig = actualConfigurationProperties.getKeycloakConfig();

        assertThat(actualKeycloackConfig).isNotNull();
        assertThat(actualKeycloackConfig.getUrl()).isEqualTo(KEYCLOAK_URL);
        assertThat(actualKeycloackConfig.getRealm()).isEqualTo(KEYCLOAK_REALM);
        assertThat(actualKeycloackConfig.getClientId()).isEqualTo(KEYCLOAK_CLIENT_ID);
    }
}
