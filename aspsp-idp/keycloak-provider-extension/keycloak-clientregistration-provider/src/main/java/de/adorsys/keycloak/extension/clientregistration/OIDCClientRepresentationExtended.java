package de.adorsys.keycloak.extension.clientregistration;

import org.keycloak.representations.oidc.OIDCClientRepresentation;

public class OIDCClientRepresentationExtended extends OIDCClientRepresentation {

	// a JSON Web Token (JWT) [RFC7519] that asserts metadata values about the
	// client software as a bundle
	private String software_statement;

	public String getSoftwareStatement() {
		return software_statement;
	}

	public void setSoftwareStatement(String software_statement) {
		this.software_statement = software_statement;
	}

}
