package de.adorsys.keycloak.extension.clientregistration;

import org.jboss.logging.Logger;
import org.keycloak.representations.oidc.OIDCClientRepresentation;

public final class CertificateUtil {
	
	private static final Logger logger = Logger.getLogger(CertificateUtil.class);
	
	
	
	
	public static String extractRole(String certificate){
		
		return "";
	}

	public static void verifiedCertificate(OIDCClientRepresentation clientOIDC) {
		logger.info(clientOIDC.toString());
		
	}

}
