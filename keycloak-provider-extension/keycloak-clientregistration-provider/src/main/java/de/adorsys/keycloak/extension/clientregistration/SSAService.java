package de.adorsys.keycloak.extension.clientregistration;

import java.io.UnsupportedEncodingException;

import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class SSAService {

	// this could throw invalid_software_statement or
	// unapproved_software_statement exception
	// https://tools.ietf.org/html/rfc7591#section-3.2.1
	public static void validate(OIDCClientRepresentationExtended clientOIDC) {

		// ssa must be issue and sign by a trust service provider,
		String software_statement = clientOIDC.getSoftware_statement();

		/*try {
			// we should verify signature (integrity) with TSP public key.
			Algorithm algorithm = Algorithm.HMAC256("secret123");
			JWTVerifier verifier = JWT.require(algorithm).withIssuer("TSP").build(); 
			DecodedJWT jwt = verifier.verify(software_statement);
		} catch (UnsupportedEncodingException exception) {
			// UTF-8 encoding not supported
		} catch (JWTVerificationException exception) {
			// Invalid signature/claims
		}*/
		
	}

}
