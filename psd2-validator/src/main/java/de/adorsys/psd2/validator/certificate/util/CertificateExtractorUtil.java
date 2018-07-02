package de.adorsys.psd2.validator.certificate.util;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import com.nimbusds.jose.util.X509CertUtils;

import de.adorsys.psd2.validator.common.PSD2QCStatement;
import de.adorsys.psd2.validator.common.PSD2QCType;
import de.adorsys.psd2.validator.common.RoleOfPSP;
import de.adorsys.psd2.validator.common.RolesOfPSP;

public class CertificateExtractorUtil {

	public static TppCertificateData extract(String encodedCert) throws IOException {

		X509Certificate cert = X509CertUtils.parse(encodedCert);

		List<TppRole> roles = new ArrayList<>();

		TppCertificateData tppCertData = new TppCertificateData();
		tppCertData.setPspName(cert.getSubjectDN().getName());

		PSD2QCType psd2qcType = PSD2QCStatement.psd2QCType(cert);
		RolesOfPSP rolesOfPSP = psd2qcType.getRolesOfPSP();
		RoleOfPSP[] roles2 = rolesOfPSP.getRoles();
		for (RoleOfPSP roleOfPSP : roles2) {
			roles.add(TppRole.valueOf(roleOfPSP.getNormalizedRoleName()));
		}
		tppCertData.setPspRoles(roles);

		tppCertData.setPspAuthorityName(psd2qcType.getnCAName().getString());

		try {
			X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
			String pspAuthorisationNber = IETFUtils
					.valueToString(x500name.getRDNs(BCStyle.ORGANIZATION_IDENTIFIER)[0].getFirst().getValue());
			tppCertData.setPspAuthorzationNumber(pspAuthorisationNber);
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}

		return tppCertData;

	}

}
