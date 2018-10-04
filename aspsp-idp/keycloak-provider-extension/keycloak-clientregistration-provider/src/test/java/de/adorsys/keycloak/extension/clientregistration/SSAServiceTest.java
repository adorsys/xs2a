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

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

public class SSAServiceTest {

	@Test
	public void testValidate() {

	}

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
