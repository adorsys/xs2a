package de.adorsys.keycloak.extension.clientregistration;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.clientregistration.ClientRegistrationException;
import org.keycloak.services.clientregistration.oidc.DescriptionConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DescriptionConverterExt {

	public static ClientRepresentation toInternal(KeycloakSession session,
			OIDCClientRepresentationExtended clientOIDCext) throws ClientRegistrationException {

		Map<String, String> attributes = new HashMap<>();
		attributes.put("software_statement", clientOIDCext.getSoftwareStatement());

		OIDCClientRepresentation clientOIDC = (OIDCClientRepresentation) clientOIDCext;

		ClientRepresentation client = DescriptionConverter.toInternal(session, clientOIDC);
		client.setAttributes(attributes);

		return client;

	}

	public static OIDCClientRepresentationExtended toExternalResponse(KeycloakSession session,
			ClientRepresentation client, URI uri) {

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
			e.printStackTrace();
		}
		return response;
	}

}