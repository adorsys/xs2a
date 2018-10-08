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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OIDCClientRegistrationExtendedProviderTest extends KeycloakTestBase {

	private OIDCClientRegistrationExtendedProvider oidcClientRegExtProvider;

	@Before
	public void setUp() {
		init();
		oidcClientRegExtProvider = new OIDCClientRegistrationExtendedProvider(session);
		oidcClientRegExtProvider.setAuth(auth);
		oidcClientRegExtProvider.setEvent(event);
	}

	@Test
	public void testCreateOIDC() {
		/*OIDCClientRepresentationExtended client = getRandomClient();
		Response response = oidcClientRegExtProvider.createOIDC(client);
		assertEquals(response.getStatus(), Status.CREATED);*/
	}

	private OIDCClientRepresentationExtended getRandomClient() {

		OIDCClientRepresentationExtended client = new OIDCClientRepresentationExtended();
		client.setClientName(UUID.randomUUID().toString());
		List<String> redirectUris = new ArrayList<>();
		redirectUris.add("*");
		client.setRedirectUris(redirectUris);
		client.setSoftwareStatement(
				"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzb2Z0d2FyZV9pZCI6IjY1ZDFmMjdjLTRhZWEtNDU0OS05YzIxLTYwZTQ5NWE3YTg2ZiIsImlzcyI6IlRTUCIsInNvZnR3YXJlX3JvbGVzIjpbIlBJU1AiLCJBSVNQIl19.GFFFPlKxSHkRzqajLNA3q41A4ExtXcBCv8xhEI5mph0");

		return client;
	}

}
