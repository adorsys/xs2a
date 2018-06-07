package de.adorsys.keycloak.extension.clientregistration;

import org.keycloak.representations.oidc.OIDCClientRepresentation;

public class OIDCClientRepresentationExtended extends OIDCClientRepresentation {

	// a JSON Web Token (JWT) [RFC7519] that asserts metadata values about the
	// client software as a bundle
	private String softwareStatement;

	public String getSoftwareStatement() {
		return softwareStatement;
	}

	public void setSoftwareStatement(String software_statement) {
		this.softwareStatement = software_statement;
	}

}
