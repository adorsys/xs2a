package de.adorsys.keycloak.extension.clientregistration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.representations.oidc.OIDCClientRepresentation;

public class OIDCClientRepresentationExtended extends OIDCClientRepresentation {

	// a JSON Web Token (JWT) [RFC7519] that asserts metadata values about the
	// client software as a bundle
    @JsonProperty("software_statement")
	private String softwareStatement;

	public String getSoftwareStatement() {
		return softwareStatement;
	}

	public void setSoftwareStatement(String softwareStatement) {
		this.softwareStatement = softwareStatement;
	}
}
