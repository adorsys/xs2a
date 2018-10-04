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

package de.adorsys.psd2.validator.certificate.util;

import com.nimbusds.jose.util.X509CertUtils;
import de.adorsys.psd2.validator.certificate.CertificateErrorMsgCode;
import de.adorsys.psd2.validator.common.PSD2QCStatement;
import de.adorsys.psd2.validator.common.PSD2QCType;
import de.adorsys.psd2.validator.common.RoleOfPSP;
import de.adorsys.psd2.validator.common.RolesOfPSP;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CertificateExtractorUtil {

	public static TppCertificateData extract(String encodedCert) throws CertificateValidationException {

		X509Certificate cert = X509CertUtils.parse(encodedCert);

        if(cert == null) {
            log.debug("Error reading certificate ");
            throw new CertificateValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
        }

		List<TppRole> roles = new ArrayList<>();

		TppCertificateData tppCertData = new TppCertificateData();

		PSD2QCType psd2qcType = PSD2QCStatement.psd2QCType(cert);
		RolesOfPSP rolesOfPSP = psd2qcType.getRolesOfPSP();
		RoleOfPSP[] roles2 = rolesOfPSP.getRoles();
		for (RoleOfPSP roleOfPSP : roles2) {
			roles.add(TppRole.valueOf(roleOfPSP.getNormalizedRoleName()));
		}
		tppCertData.setPspRoles(roles);

		tppCertData.setPspAuthorityName(psd2qcType.getnCAName().getString());
		tppCertData.setPspAuthorityId(psd2qcType.getnCAId().getString());

		try {
			X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
			String pspAuthorisationNber = IETFUtils
					.valueToString(x500name.getRDNs(BCStyle.ORGANIZATION_IDENTIFIER)[0].getFirst().getValue());
			tppCertData.setPspAuthorizationNumber(pspAuthorisationNber);
			
			String pspName = IETFUtils
					.valueToString(x500name.getRDNs(BCStyle.CN)[0].getFirst().getValue());
			tppCertData.setPspName(pspName);
		} catch (CertificateEncodingException e) {
			log.debug(e.getMessage());
			throw new CertificateValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
        }
		return tppCertData;

	}

}
