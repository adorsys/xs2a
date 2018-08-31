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

package de.adorsys.aspsp.aspspmockserver.keycloak;

import de.adorsys.aspsp.aspspmockserver.config.KeycloakConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

import static java.util.Collections.singletonList;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {
    private static final String ADMIN_CLI = "admin-cli";
    private static final String MASTER_REALM = "master";
    private static final String USER_ROLE = "user";
    private final KeycloakConfigProperties keycloakConfigProperties;

    @Value("${keycloak-admin-username}")
    private String keycloakAdminUsername;
    @Value("${keycloak-admin-password}")
    private String keycloakAdminPassword;

    public boolean registerClient(String name, String password, String email) {
        Keycloak keycloak = Keycloak.getInstance(keycloakConfigProperties.getAuthServerUrl(), MASTER_REALM,
            keycloakAdminUsername,
            keycloakAdminPassword,
            ADMIN_CLI);

        try (Response response = keycloak.realm(keycloakConfigProperties.getRealm()).users().create(getUserRepresentation(name, password, email))) {
            log.info("Register keycloak client status: {}", response.getStatus());
            return response.getStatus() == 201;
        }
    }

    private UserRepresentation getUserRepresentation(String name, String password, String email) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(name);
        user.setEmail(email);
        user.setCredentials(singletonList(credential));
        user.setRealmRoles(singletonList(USER_ROLE));
        user.setEnabled(true);
        return user;
    }
}
