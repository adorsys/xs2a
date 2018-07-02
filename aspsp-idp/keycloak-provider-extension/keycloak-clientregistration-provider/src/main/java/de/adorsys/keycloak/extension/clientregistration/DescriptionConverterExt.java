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

package de.adorsys.keycloak.extension.clientregistration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.clientregistration.ClientRegistrationException;
import org.keycloak.services.clientregistration.oidc.DescriptionConverter;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

class DescriptionConverterExt {

    static ClientRepresentation toInternal(KeycloakSession session,
                                           OIDCClientRepresentationExtended clientOIDCext
                                          ) throws ClientRegistrationException {

        Map<String, String> attributes = new HashMap<>();
        attributes.put("software_statement", clientOIDCext.getSoftwareStatement());

        ClientRepresentation client = DescriptionConverter.toInternal(session, clientOIDCext);
        client.setAttributes(attributes);

        return client;

    }

    static OIDCClientRepresentationExtended toExternalResponse(KeycloakSession session,
                                                               ClientRepresentation client, URI uri
                                                              ) {

        String softStatement = client.getAttributes().get("software_statement");
        OIDCClientRepresentation clientRep = DescriptionConverter.toExternalResponse(session, client, uri);

        OIDCClientRepresentationExtended response = new OIDCClientRepresentationExtended();

        ObjectMapper mapper = new ObjectMapper();
        try {

            String clientRepStr = mapper.writeValueAsString(clientRep);
            response = mapper.readValue(clientRepStr, OIDCClientRepresentationExtended.class);

            response.setSoftwareStatement(softStatement);

            return response;

        } catch (IOException e) {
            ServicesLogger.LOGGER.warn(
                "Failed to convert ClientRepresentation to OIDCClientRepresentationExtended. Exception: {}",
                e.getMessage(),
                e
                                      );
        }
        return response;
    }

}
