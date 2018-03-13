/*
 * Copyright (C) 2018 adorsys GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.keycloak.extension.clientregistration;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.services.clientregistration.oidc.OIDCClientRegistrationProviderFactory;

public class OIDCClientRegistrationExtendedProviderFactory extends OIDCClientRegistrationProviderFactory
		implements ServerInfoAwareProviderFactory {

	private static final Logger logger = Logger.getLogger(OIDCClientRegistrationExtendedProviderFactory.class);

	@Override
	public OIDCClientRegistrationExtendedProvider create(KeycloakSession session) {
		return new OIDCClientRegistrationExtendedProvider(session);
	}

	@Override
	public void init(Config.Scope config) {
		logger.info(" OIDCClientRegistrationProviderFactoryExtended init");
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
		logger.info(" OIDCClientRegistrationProviderFactoryExtended Postinit");
	}

	@Override
	public Map<String, String> getOperationalInfo() {
		Map<String, String> ret = new LinkedHashMap<>();
		ret.put("version", "1.0");
		ret.put("author", "gmo");
		return ret;
	}

}
