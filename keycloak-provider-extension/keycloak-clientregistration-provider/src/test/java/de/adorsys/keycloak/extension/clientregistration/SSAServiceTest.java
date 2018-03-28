package de.adorsys.keycloak.extension.clientregistration;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

public class SSAServiceTest {

	@Test
	public void ssaGenerate() {

		String[] software_roles = { "PISP", "AISP" };

		try {
			Algorithm algorithm = Algorithm.HMAC256("secret123");
			String ssa = JWT.create().withIssuer("TSP").withClaim("software_id", "65d1f27c-4aea-4549-9c21-60e495a7a86f")
					.withArrayClaim("software_roles", software_roles).sign(algorithm);

			System.out.println(ssa);
		} catch (UnsupportedEncodingException exception) {
			// UTF-8 encoding not supported
		} catch (JWTCreationException exception) {
			// Invalid Signing configuration / Couldn't convert Claims.
		}

	}
}
