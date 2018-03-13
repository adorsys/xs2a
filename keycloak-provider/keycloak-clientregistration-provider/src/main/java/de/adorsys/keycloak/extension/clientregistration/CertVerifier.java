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

import de.adorsys.keycloak.extension.clientregistration.certs.CertValidatorBuilder;
import de.adorsys.keycloak.extension.clientregistration.certs.rules.ExpirationRule;
import org.jboss.logging.Logger;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.clientregistration.ClientRegistrationException;

import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;

public final class CertVerifier {

	private static final Logger logger = Logger.getLogger(CertVerifier.class);

	public static void verify(OIDCClientRepresentation clientOIDC, HttpServletRequest req) throws CertificateException {
		X509Certificate certificate = extractCertificate(req);
		if (certificate != null) {
			try {
				CertValidatorBuilder.getInstance().addRule(new ExpirationRule()) // add more custom rule
				.build().validate(certificate);
			} catch (Exception e) {
				logger.info(e.getMessage());
				throw new ClientRegistrationException(e.getMessage());
			}
		} else {
			throw new ClientRegistrationException("Client certificate Not Found ");
		}

	}

	private static X509Certificate extractCertificate(HttpServletRequest req) throws CertificateException {
		String base64TppCertificate = (String) req.getHeader("TPP-Certificate");

		byte encodedCert[] = Base64.getDecoder().decode(base64TppCertificate);
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(encodedCert);
		X509Certificate c = (X509Certificate) certFactory.generateCertificate(inputStream);
		if (c != null) {

			logger.info("getIssuerDN " + c.getIssuerDN());
			logger.info("getSubjectDN : " + c.getSubjectDN());
			logger.info("getNotAfter : " + c.getNotAfter());
			logger.info("getNotAfter : " + c.getNotAfter());
			logger.info("getSerialNumber : " + c.getSerialNumber());
			logger.info("getSigAlgOID : " + c.getSigAlgOID());
			logger.info("getSigAlgName :" + c.getSigAlgName());

		}
		return c;

	}

}
